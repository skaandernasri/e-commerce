from fastapi import HTTPException
from sqlmodel import Session, select
from app.database import engine
from app.models import Utilisateur, Produit, SuiviClient, UtilisateurAnonyme, Variante
from lightfm import LightFM
from lightfm.data import Dataset
import os, pickle
from datetime import datetime
from lightfm.cross_validation import random_train_test_split
from lightfm.evaluation import precision_at_k, recall_at_k, auc_score
from dataclasses import dataclass
from typing import List, Dict, Any, Optional
from uuid import UUID
import random
from itertools import product
import numpy as np

# Import ModelConfig from config module
from app.config import ModelConfig

# Global variables
MODEL_FILE = "lightfm_model.pkl"
model = None
dataset = None
user_features = None
item_features = None
model_config = None

def auto_normalize_scores(scores):
    """
    Normalize raw scores to [0, 1] using a sigmoid function with an adaptive scaling factor k.
    The scaling factor k is automatically estimated from the 90th percentile of the input scores.

    Parameters:
        scores (list or np.ndarray): List of raw interaction scores (e.g. click/view counts).

    Returns:
        normalized_scores (np.ndarray): Scores normalized to [0, 1].
        k (float): Chosen scale factor for sigmoid.
    """
    scores = np.array(scores, dtype=np.float32)
    percentile_90 = np.percentile(scores, 90)
    k = max(percentile_90 / 4, 1)  # évite k=0 ou k trop faible
    normalized_scores = 1 / (1 + np.exp(-scores / k))
    return normalized_scores, k

def compute_user_features(user: Utilisateur):
    features = []

    # Handle genre
    if user.genre:
        features.append(f"genre:{user.genre}")
    else:
        features.append("genre:unknown")

    # Handle date_naissance
    if user.date_naissance:
        age = datetime.now().year - user.date_naissance.year
        age_group = age // 10 * 10
        features.append(f"age_group:{age_group}")
    else:
        features.append("age_group:unknown")

    return features

def compute_product_features(product: Produit, variants: List[Variante]):
    tailles = set()
    couleurs = set()

    for variant in variants:
        tailles.add(variant.size)
        couleurs.add(variant.color)

    features = [
        f"categorie:{product.categorie_id}",
        f"marque:{product.marque}",
    ]
    features.extend([f"taille:{t}" for t in tailles if t])
    features.extend([f"couleur:{c}" for c in couleurs if c])

    return features

def load_data(session: Session):
    users = session.exec(select(Utilisateur)).all()
    anonymous_users = session.exec(select(UtilisateurAnonyme)).all()
    products = session.exec(select(Produit)).all()
    variants = session.exec(select(Variante)).all()
    interactions = session.exec(select(SuiviClient)).all()

    if not products:
        print("❗ No products found.")
    if not interactions:
        print("❗ No interactions found.")

    return users, anonymous_users, products, variants, interactions

def build_dataset(users, anonymous_users, products, variants, interactions):
    dataset = Dataset()
    user_ids = [f"user_{u.id}" for u in users]
    anon_user_ids = [f"anon_{a.id}" for a in anonymous_users]
    all_user_ids = user_ids + anon_user_ids
    product_ids = [str(p.id) for p in products]

    user_features_list = [(f"user_{u.id}", compute_user_features(u)) for u in users]
    anon_user_features_list = [(f"anon_{a.id}", ["anonymous:true"]) for a in anonymous_users]
    all_user_features_list = user_features_list + anon_user_features_list

    # Créer un dictionnaire produit_id -> liste de variantes
    variant_map: Dict[int, List[Variante]] = {}
    for v in variants:
        variant_map.setdefault(v.product_id, []).append(v)

    # Construire les features des produits
    product_features_list = []
    for p in products:
        related_variants = variant_map.get(p.id, [])
        features = compute_product_features(p, related_variants)
        product_features_list.append((str(p.id), features))

    dataset.fit(
        users=all_user_ids,
        items=product_ids,
        user_features=set(f for _, feats in all_user_features_list for f in feats),
        item_features=set(f for _, feats in product_features_list for f in feats)
    )
    raw_scores = [i.score for i in interactions]
    normalized_scores, k = auto_normalize_scores(raw_scores)
    interactions_data = []
    for i, norm_score in zip(interactions, normalized_scores):
        if i.utilisateur_id is not None:
            user_key = f"user_{i.utilisateur_id}"
        elif i.utilisateur_anonyme_id is not None:
            user_key = f"anon_{i.utilisateur_anonyme_id}"
        else:
            continue
        interactions_data.append((user_key, str(i.produit_id), float(norm_score)))

    interaction_matrix, _ = dataset.build_interactions(interactions_data)
    user_features = dataset.build_user_features(all_user_features_list)
    item_features = dataset.build_item_features(product_features_list)

    return dataset, interaction_matrix, user_features, item_features

def evaluate_model(model, test_matrix, user_features, item_features):
    # Convert numpy floats to Python floats and handle NaN/inf values
    def safe_float(value):
        if np.isnan(value) or np.isinf(value):
            return 0.0  # or None if you prefer
        return float(value)

    try:
        precision = precision_at_k(model, test_matrix, k=5,
                                   user_features=user_features,
                                   item_features=item_features).mean()
        recall = recall_at_k(model, test_matrix, k=5,
                             user_features=user_features,
                             item_features=item_features).mean()
        auc = auc_score(model, test_matrix,
                        user_features=user_features,
                        item_features=item_features).mean()

        return {
            "precision@5": safe_float(precision),
            "recall@5": safe_float(recall),
            "auc": safe_float(auc)
        }
    except Exception as e:
        print(f"Error evaluating model: {str(e)}")
        return {
            "precision@5": 0.0,
            "recall@5": 0.0,
            "auc": 0.0,
            "error": str(e)
        }

def train_model(
        session: Session,
        config: ModelConfig = None,
        tune: bool = False,
        param_grid: Dict[str, List[Any]] = None,
        n_trials: int = 20
):
    global model, dataset, user_features, item_features, model_config

    if config is None:
        config = ModelConfig()

    users, anonymous_users, products, variants, interactions = load_data(session)

    if not products or not (users or anonymous_users) or not interactions:
        print("⚠️ Not enough data to train the model. Skipping training.")
        return None, None, None, None

    dataset, interaction_matrix, u_features, i_features = build_dataset(
        users, anonymous_users, products, variants, interactions
    )

    train_matrix, test_matrix = random_train_test_split(interaction_matrix, test_percentage=0.2)

    if tune and param_grid:
        best_score = -1
        best_model = None
        best_config = config
        best_metrics = {}

        # Generate parameter combinations
        if n_trials >= len(list(product(*param_grid.values()))):
            param_combinations = product(*param_grid.values())
        else:
            param_combinations = []
            param_keys = list(param_grid.keys())
            for _ in range(n_trials):
                params = [random.choice(param_grid[k]) for k in param_keys]
                param_combinations.append(params)

        for values in param_combinations:
            current_params = dict(zip(param_grid.keys(), values))
            current_config = ModelConfig(**{**vars(config), **current_params})

            print(f"Testing config: {current_params}")

            current_model = LightFM(
                loss=current_config.loss,
                no_components=current_config.no_components,
                learning_rate=current_config.learning_rate,
                item_alpha=current_config.item_alpha,
                user_alpha=current_config.user_alpha,
                max_sampled=current_config.max_sampled
            )

            current_model.fit(
                train_matrix,
                user_features=u_features,
                item_features=i_features,
                epochs=current_config.epochs,
                num_threads=current_config.num_threads
            )

            metrics = evaluate_model(current_model, test_matrix, u_features, i_features)
            current_score = metrics["auc"]

            if current_score > best_score:
                best_score = current_score
                best_model = current_model
                best_config = current_config
                best_metrics = metrics
                print(f"New best config: {current_params} with AUC: {current_score:.4f}")

        model = best_model
        model_config = best_config
        print(f"\nBest configuration found:")
        print(f"Params: {vars(best_config)}")
        print(f"Metrics: {best_metrics}")
    else:
        model = LightFM(
            loss=config.loss,
            no_components=config.no_components,
            learning_rate=config.learning_rate,
            item_alpha=config.item_alpha,
            user_alpha=config.user_alpha,
            max_sampled=config.max_sampled
        )
        model.fit(
            train_matrix,
            user_features=u_features,
            item_features=i_features,
            epochs=config.epochs,
            num_threads=config.num_threads
        )
        model_config = config

    # Save the model
    with open(MODEL_FILE, "wb") as f:
        pickle.dump((model, dataset, u_features, i_features, model_config), f)

    return model, dataset, u_features, i_features

def train_model_endpoint(
        session: Session,
        loss: Optional[str] = None,
        no_components: Optional[int] = None,
        learning_rate: Optional[float] = None,
        epochs: Optional[int] = None
):
    global model, dataset, user_features, item_features, model_config

    # Update only specified parameters
    config_params = vars(model_config) if model_config else {}
    if loss: config_params["loss"] = loss
    if no_components: config_params["no_components"] = no_components
    if learning_rate: config_params["learning_rate"] = learning_rate
    if epochs: config_params["epochs"] = epochs

    config = ModelConfig(**config_params)

    model, dataset, user_features, item_features = train_model(
        session,
        config=config
    )
    if model is None:
        return {"message": "Training skipped due to missing data."}
    return {"message": "Model trained with custom parameters", "config": vars(config)}

def tune_model(
        session: Session,
        epochs: int = 10,
        n_trials: int = 27
):
    param_grid = {
        "loss": ["logistic"],
        "learning_rate": [0.001, 0.005, 0.01],
        "no_components": [32, 64, 128],
        "item_alpha": [0.0, 0.001, 0.01],
    }

    model, dataset, user_features, item_features = train_model(
        session,
        tune=True,
        param_grid=param_grid,
        n_trials=n_trials,
        config=ModelConfig(epochs=epochs)
    )
    if model is None:
        return {"message": "Training skipped due to missing data."}

    return {"message": "Model tuning completed", "best_config": vars(model_config)}

def recommend_products(
        session: Session,
        user_id: Optional[int] = None,
        anonymous_user_session_id: Optional[UUID] = None,
        num_recs: int = 5,
):
    global model, dataset, user_features, item_features, model_config

    # Handle model not ready
    if model is None or dataset is None:
        return {
            "user_key": None,
            "recommended_products": [],
            "model_config": vars(model_config) if model_config else None,
            "message": "Model not ready"
        }

    try:
        # Determine user key
        if user_id:
            user = session.get(Utilisateur, user_id)
            if not user:
                return {
                    "user_key": None,
                    "recommended_products": [],
                    "message": "User not found"
                }
            user_key = f"user_{user_id}"
        elif anonymous_user_session_id:
            anon_user = session.exec(
                select(UtilisateurAnonyme).where(UtilisateurAnonyme.session_token == anonymous_user_session_id)
            ).one_or_none()
            if not anon_user:
                return {
                    "user_key": None,
                    "recommended_products": [],
                    "message": "Anonymous user not found"
                }
            user_key = f"anon_{anon_user.id}"
        else:
            return {
                "user_key": None,
                "recommended_products": [],
                "message": "User ID or Anonymous User ID required"
            }

        # Try to get the index for the user
        try:
            user_idx = dataset.mapping()[0][user_key]
        except KeyError:
            return {
                "user_key": user_key,
                "recommended_products": [],
                "message": "User not in training dataset"
            }

        # Get product list
        products = session.exec(select(Produit)).all()
        product_ids = [str(p.id) for p in products]

        if not product_ids:
            return {
                "user_key": user_key,
                "recommended_products": [],
                "message": "No products available in database"
            }

        # Predict scores
        scores = model.predict(
            user_ids=user_idx,
            item_ids=np.arange(len(product_ids)),
            user_features=user_features,
            item_features=item_features,
            num_threads=4
        )

        top_indices = np.argsort(-scores)[:num_recs]
        recommended_product_ids = [int(product_ids[i]) for i in top_indices]

        return {
            "user_key": user_key,
            "recommended_products": recommended_product_ids,
            "model_config": vars(model_config) if model_config else None
        }

    except Exception as e:
        return {
            "user_key": user_key if 'user_key' in locals() else None,
            "recommended_products": [],
            "message": f"Recommendation failed: {str(e)}"
        }

def get_model_metrics(session: Session):
    global model, dataset, user_features, item_features

    if model is None or dataset is None:
        raise HTTPException(status_code=503, detail="Model not ready")

    try:
        users, anonymous_users, products, variants, interactions = load_data(session)

        if not products or not interactions:
            return {
                "metrics": {
                    "error": "Not enough data for evaluation",
                    "precision@5": 0.0,
                    "recall@5": 0.0,
                    "auc": 0.0
                },
                "config": vars(model_config) if model_config else None
            }

        _, interaction_matrix, _, _ = build_dataset(users, anonymous_users, products, variants, interactions)
        _, test = random_train_test_split(interaction_matrix, test_percentage=0.2)

        metrics = evaluate_model(model, test, user_features, item_features)

        return {
            "metrics": metrics,
            "config": vars(model_config) if model_config else None
        }
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={
                "error": f"Evaluation failed: {str(e)}",
                "metrics": {
                    "precision@5": 0.0,
                    "recall@5": 0.0,
                    "auc": 0.0
                }
            }
        )
def create_model():
    global model, dataset, user_features, item_features, model_config

    with Session(engine) as session:
        if os.path.exists(MODEL_FILE):
            try:
                with open(MODEL_FILE, "rb") as f:
                    model, dataset, user_features, item_features, model_config = pickle.load(f)
                print("✅ Model loaded from file.")
            except (AttributeError, pickle.UnpicklingError) as e:
                print(f"⚠️ Failed to load model: {e}")
                print("⚠️ Attempting to train a new model...")
                os.remove(MODEL_FILE)  # Delete corrupted file
                model, dataset, user_features, item_features = train_model(session)
                if model is not None:
                    model_config = ModelConfig()
                    print("✅ Model trained and saved.")
                else:
                    print("⚠️ Model training skipped due to missing data.")
                    model_config = None
        else:
            print("⚠️ Model not found. Attempting to train...")
            model, dataset, user_features, item_features = train_model(session)
            if model is not None:
                model_config = ModelConfig()
                print("✅ Model trained and saved.")
            else:
                print("⚠️ Model training skipped due to missing data.")
                model_config = None
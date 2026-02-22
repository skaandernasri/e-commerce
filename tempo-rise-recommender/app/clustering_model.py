# clustering.py
from sklearn.cluster import KMeans
import pandas as pd
from sklearn.preprocessing import StandardScaler
from sqlalchemy.orm import Session
from app.models import Utilisateur, Commande, Paiement  # adapt paths
from datetime import datetime
from sklearn.mixture import GaussianMixture
from sklearn.metrics import silhouette_score, calinski_harabasz_score, davies_bouldin_score
import logging

user_clusters = {}
MAX_RECENCY_DAYS = 9999
logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.INFO)

def get_user_features(db: Session):
    users = db.query(Utilisateur).all()
    user_data = []

    for user in users:
        commandes = db.query(Commande).filter(Commande.utilisateur_id == user.id).all()
        paiements = db.query(Paiement).filter(
            Paiement.commande_id.in_([c.id for c in commandes]),
            Paiement.statut == 'COMPLETED'  # only completed
        ).all()

        total_spent = sum(p.amount for p in paiements)
        order_count = len(commandes)
        avg_order_value = total_spent / order_count if order_count else 0

        # Calculate Recency (days since last order)
        if commandes:
            last_order_date = max(c.date_commande for c in commandes if c.date_commande is not None)
            recency_days = (datetime.now() - last_order_date).days
        else:
            recency_days = None  # or some large number, e.g. 9999

        # Calculate Frequency (orders per month)
        if commandes:
            first_order_date = min(c.date_commande for c in commandes if c.date_commande is not None)
            period_months = max((datetime.now() - first_order_date).days / 30, 1)
            frequency = order_count / period_months
        else:
            frequency = 0
        user_data.append({
            "user_id": user.id,
            "total_spent": total_spent,
            "order_count": order_count,
            "avg_order_value": avg_order_value,
            "recency_days": recency_days if recency_days is not None else MAX_RECENCY_DAYS,
            "frequency": frequency
        })

    return pd.DataFrame(user_data)

def cluster_users_from_features(df: pd.DataFrame, n_clusters: int = 3):
    features = df[["total_spent", "avg_order_value", "recency_days", "frequency"]]

    features = StandardScaler().fit_transform(features)

    unique_points = np.unique(features, axis=0)
    actual_n_clusters = min(n_clusters, len(unique_points))

    if actual_n_clusters < n_clusters:
        logger.warning(f"KMeans: Adjusted n_clusters from {n_clusters} to {actual_n_clusters} due to duplicate data.")

    kmeans = KMeans(n_clusters=actual_n_clusters, random_state=42)
    kmeans_labels = kmeans.fit_predict(features)

    metrics = {
        "silhouette_score": silhouette_score(features, kmeans_labels) if len(set(kmeans_labels)) > 1 else None,
        "calinski_harabasz_score": calinski_harabasz_score(features, kmeans_labels) if len(set(kmeans_labels)) > 1 else None,
        "davis_bouldin_index": davies_bouldin_score(features,kmeans_labels) if len(set(kmeans_labels)) > 1 else None
    }

    return kmeans_labels, metrics

def cluster_users_gmm(df: pd.DataFrame, n_clusters: int = 3):
    features = df[["total_spent", "avg_order_value", "recency_days", "frequency"]]

    features = StandardScaler().fit_transform(features)

    unique_points = np.unique(features, axis=0)
    actual_n_clusters = min(n_clusters, len(unique_points))

    if actual_n_clusters < n_clusters:
        logger.warning(f"GMM: Adjusted n_clusters from {n_clusters} to {actual_n_clusters} due to duplicate data.")

    gmm = GaussianMixture(n_components=actual_n_clusters, random_state=42)
    gmm_labels = gmm.fit_predict(features)

    metrics = {
        "silhouette_score": silhouette_score(features, gmm_labels) if len(set(gmm_labels)) > 1 else None,
        "calinski_harabasz_score": calinski_harabasz_score(features, gmm_labels) if len(set(gmm_labels)) > 1 else None,
        "davis_bouldin_index": davies_bouldin_score(features,gmm_labels) if len(set(gmm_labels)) > 1 else None
    }

    return gmm_labels, metrics

def evaluate_clustering(features_scaled, labels):
    sil_score = silhouette_score(features_scaled, labels)
    ch_score = calinski_harabasz_score(features_scaled, labels)
    db_score = davies_bouldin_score(features_scaled, labels)
    return {
        "silhouette_score": sil_score,
        "calinski_harabasz_score": ch_score,
        "davis_bouldin_index": db_score
    }
def compare_clustering(db: Session, n_clusters: int = 3):
    df = get_user_features(db)

    if df.empty:
        return {"message": "No user data available"}

    kmeans_labels, kmeans_metrics = cluster_users_from_features(df, n_clusters)
    gmm_labels, gmm_metrics = cluster_users_gmm(df, n_clusters)

    result = {
        "kmeans": {
            "clusters": dict(zip(df["user_id"], kmeans_labels)),
            "metrics": kmeans_metrics
        },
        "gmm": {
            "clusters": dict(zip(df["user_id"], gmm_labels)),
            "metrics": gmm_metrics
        }
    }
    return convert_numpy_types(result)

import numpy as np

def convert_numpy_types(obj):
    if isinstance(obj, dict):
        return {convert_numpy_types(k): convert_numpy_types(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [convert_numpy_types(i) for i in obj]
    elif isinstance(obj, (np.integer, np.int32, np.int64)):
        return int(obj)
    elif isinstance(obj, (np.floating, np.float32, np.float64)):
        return float(obj)
    else:
        return obj



from fastapi import FastAPI, Depends,HTTPException
from sqlmodel import Session
from contextlib import asynccontextmanager
from app.database import get_session
from app.clustering_model import cluster_users_from_features, compare_clustering, get_user_features, cluster_users_gmm
from app.lightfm_model import (
    train_model_endpoint,
    tune_model,
    recommend_products,
    get_model_metrics,
    model_config,
    create_model
)
from typing import Optional
from uuid import UUID

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize the model when the app starts
    try:
        create_model()
    except Exception as e:
        print(f"⚠️ Failed to initialize model: {str(e)}")
        # You might want to handle this differently in production
        # For example, you could exit the application or run in a degraded mode
    yield

app = FastAPI(lifespan=lifespan)

@app.get("/model/train")
def train_model_endpoint_api(
        session: Session = Depends(get_session),
        loss: Optional[str] = None,
        no_components: Optional[int] = None,
        learning_rate: Optional[float] = None,
        epochs: Optional[int] = None
):
    return train_model_endpoint(session, loss, no_components, learning_rate, epochs)

@app.get("/model/tune")
def tune_model_endpoint_api(
        session: Session = Depends(get_session),
        epochs: int = 10,
        n_trials: int = 20
):
    return tune_model(session, epochs, n_trials)

@app.get("/recommendations")
def get_recommendations(
        session: Session = Depends(get_session),
        user_id: Optional[int] = None,
        anonymous_user_session_id: Optional[UUID] = None,
        num_recs: int = 5
):
    return recommend_products(session, user_id, anonymous_user_session_id, num_recs)

@app.get("/model/metrics")
def get_model_metrics_api(session: Session = Depends(get_session)):
    try:
        metrics = get_model_metrics(session)
        return {
            "success": True,
            "data": metrics
        }
    except HTTPException as e:
        # This will catch the 503 and 500 errors we raised
        raise e
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={
                "success": False,
                "error": f"Unexpected error: {str(e)}",
                "metrics": {
                    "precision@5": 0.0,
                    "recall@5": 0.0,
                    "auc": 0.0
                }
            }
        )

@app.get("/model/config")
def get_model_config():
    if model_config is None:
        return {"message": "Model not configured", "config": None}
    return model_config.to_dict()

@app.get("/model/cluster/kmeans")
def get_clusters_kmeans(db: Session = Depends(get_session), n_clusters: int = 3):
    df = get_user_features(db)
    if df.empty:
        return {"message": "No user data available"}

    labels, _ = cluster_users_from_features(df, n_clusters)

    return {
        "n_clusters": int(n_clusters),
        "user_cluster_map": [
            {"user_id": int(user_id), "cluster": int(cluster)}
            for user_id, cluster in zip(df["user_id"], labels)
        ]
    }

@app.get("/model/cluster/gmm")
def get_clusters_gmm(db: Session = Depends(get_session), n_clusters: int = 3):
    df = get_user_features(db)
    if df.empty:
        return {"message": "No user data available"}

    labels, _ = cluster_users_gmm(df, n_clusters)

    return {
        "n_clusters": int(n_clusters),
        "user_cluster_map": [
            {"user_id": int(user_id), "cluster": int(cluster)}
            for user_id, cluster in zip(df["user_id"], labels)
        ]
    }

@app.get("/model/cluster/compare")
def compare_clustering_api(db: Session = Depends(get_session), n_clusters: int = 3):
    return compare_clustering(db, n_clusters)
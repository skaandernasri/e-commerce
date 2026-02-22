import os
from dotenv import load_dotenv

from pathlib import Path

from dataclasses import dataclass

@dataclass
class ModelConfig:
    loss: str = 'logistic'
    no_components: int = 32
    learning_rate: float = 0.005
    item_alpha: float = 0.0
    user_alpha: float = 0.0
    max_sampled: int = 10
    epochs: int = 30
    num_threads: int = 2
    def to_dict(self):
        return {
            "loss": self.loss,
            "no_components": self.no_components,
            "learning_rate": self.learning_rate,
            "item_alpha": self.item_alpha,
            "user_alpha": self.user_alpha,
            "max_sampled": self.max_sampled,
            "epochs": self.epochs,
            "num_threads": self.num_threads
        }

env_path = Path(__file__).parent.parent / '.env'
load_dotenv(dotenv_path=env_path)

class Settings:
    PROJECT_NAME:str = "temporise"
    PROJECT_VERSION: str = "1.0.0"

    POSTGRES_USER : str = os.getenv("POSTGRES_USER","postgres")
    POSTGRES_PASSWORD = os.getenv("POSTGRES_PASSWORD","postgres")
    POSTGRES_SERVER : str = os.getenv("POSTGRES_SERVER","localhost")
    POSTGRES_PORT : str = os.getenv("POSTGRES_PORT",5432) # default postgres port is 5432
    POSTGRES_DB : str = os.getenv("POSTGRES_DB","temporise")
    DATABASE_URL = f"postgresql://{POSTGRES_USER}:{POSTGRES_PASSWORD}@{POSTGRES_SERVER}:{POSTGRES_PORT}/{POSTGRES_DB}"
settings = Settings()
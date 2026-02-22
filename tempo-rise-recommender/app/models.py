from typing import Optional
from datetime import date
from sqlmodel import SQLModel, Field, Column, Float
from uuid import UUID

class Utilisateur(SQLModel, table=True):
    __tablename__ = "utilisateur"
    id: Optional[int] = Field(default=None, primary_key=True)
    genre: str
    date_naissance: date

class Produit(SQLModel, table=True):
    __tablename__ = "produit"
    id: Optional[int] = Field(default=None, primary_key=True)
    categorie_id: int
    marque: str

class SuiviClient(SQLModel, table=True):
    __tablename__ = "suivi_client"
    id: Optional[int] = Field(default=None, primary_key=True)
    utilisateur_id: Optional[int] = Field(default=None, foreign_key="utilisateur.id")
    utilisateur_anonyme_id: Optional[int] = Field(default=None, foreign_key="utilisateuranonyme.id")
    produit_id: int = Field(foreign_key="produit.id")
    type_action: str
    score: float = Field(sa_column=Column(Float(precision=2)))
    date: date
class UtilisateurAnonyme(SQLModel, table=True):
    __tablename__ = "utilisateuranonyme"
    id: Optional[int] = Field(default=None, primary_key=True)
    session_token: UUID = Field(unique=True)

class Variante(SQLModel, table=True):
    __tablename__ = "variant"
    id: Optional[int] = Field(default=None, primary_key=True)
    product_id: int = Field(foreign_key="produit.id")
    size: str
    color: str

class Commande(SQLModel, table=True):
    __tablename__ = "commande"
    id: Optional[int] = Field(default=None, primary_key=True)
    utilisateur_id: Optional[int] = Field(default=None, foreign_key="utilisateur.id")
    date_commande: date
    total: float

class Paiement(SQLModel, table=True):
    __tablename__ = "paiement"
    id: Optional[int] = Field(default=None, primary_key=True)
    commande_id: Optional[int] = Field(default=None, foreign_key="commande.id")
    statut: str
    amount: float

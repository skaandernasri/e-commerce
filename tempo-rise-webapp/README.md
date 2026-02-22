# 🛍️ E-Commerce Frontend

Ce projet est le frontend d'un site e-commerce développé avec **Angular 19.1.x**, **TailwindCSS**, et un système de **thèmes dynamiques** sélectionnables par l'utilisateur.  

## 🚀 Fonctionnalités principales

- 🌐 **Site responsive** (compatible mobile, tablette et desktop)  
- 🎨 **Thèmes dynamiques** (choix du thème en temps réel via l'interface admin)  
- 🔐 **Espace administrateur sécurisé** (gestion des accès avec autorisations)  
- 🛒 **Gestion du panier** (navigation fluide entre les pages)  
- 📍 **Routing précis** (chaque bouton navigue vers la page correspondante)  

---

## 🏗️ **Architecture du projet**

📂 **src/**  
 ├── 📁 **app/** _(_Dossier principal de l'application Angular_)_  
 │   ├── 📁 **components/** _(_Composants réutilisables comme Header, Footer, etc._)_  
 │   ├── 📁 **pages/** _(_Pages principales du site e-commerce_)_  
 │   │   ├── 📁 **home/** _(_Page d'accueil_)_  
 │   │   ├── 📁 **product/** _(_Détails des produits_)_  
 │   │   ├── 📁 **cart/** _(_Panier utilisateur_)_  
 │   │   ├── 📁 **admin/** _(_Section Admin, avec un onglet pour les thèmes_)_  
 │   ├── 📁 **services/** _(_Services Angular pour la gestion des données et thèmes_)_  
 │   ├── 📁 **guards/** _(_Protection des routes Admin avec `AdminGuard`_)_  
 │   ├── 📁 **models/** _(_Interfaces et modèles de données_)_  
 │   ├── 📄 **app.module.ts** _(_Module principal de l'application_)_  
 │   ├── 📄 **app-routing.module.ts** _(_Configuration des routes_)_  
 │   ├── 📄 **main.ts** _(_Fichier d'entrée de l'application_)_  
 │  
 ├── 📁 **assets/** _(_Images, icônes et fichiers statiques_)_  
 ├── 📄 **tailwind.config.js** _(_Configuration de TailwindCSS_)_  
 ├── 📄 **angular.json** _(_Configuration globale du projet_)_  
 ├── 📄 **package.json** _(_Dépendances et scripts de l'application_)_  

---

## 🛠️ **Installation et exécution**

### 1️⃣ **Cloner le projet**  
```sh
git clone https://github.com/votre-repo/ecommerce-frontend.git
cd ecommerce-frontend

2️⃣ Installer les dépendances

npm install

3️⃣ Lancer le serveur Angular

ng serve

📌 L'application sera accessible à l'adresse http://localhost:4200
🎨 Gestion des thèmes dynamiques

    L'utilisateur admin peut sélectionner un thème parmi 3 disponibles :
        🌞 Clair
        🌙 Sombre
        🎨 Personnalisé

    Le thème choisi est appliqué immédiatement et sauvegardé en localStorage.

    La sélection se fait via un onglet "Thèmes" dans le dashboard admin.

    L'accès à cet onglet est restreint aux administrateurs via un AdminGuard.

🔐 Gestion des accès et navigation

    Admin : Un bouton permet d'accéder au dashboard admin.
    Client : Un bouton permet d'accéder à l'interface client.
    Bouton "Cart" : Navigation directe vers le panier utilisateur.
    Les routes Admin sont protégées et nécessitent une authentification.

🏗️ Technologies utilisées

    Angular 19.1.x (Framework frontend)
    TailwindCSS (Style et responsive design)
    Angular Router (Gestion des routes et navigation)
    LocalStorage (Sauvegarde du thème choisi)

🛠️ Améliorations futures

✅ Ajouter une palette de couleurs personnalisée pour le thème "Custom".
✅ Intégrer une authentification utilisateur complète avec JWT.
✅ Ajouter une progressive web app (PWA) pour un mode offline.
📜 Licence

Ce projet est sous licence MIT. Vous êtes libre de le modifier et l'utiliser comme bon vous semble.
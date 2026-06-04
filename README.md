# 🧠 Projet IA - Détection Signal / Image (ISEN - Groupe 1)

Ce dépôt contient le code source intégral de notre projet d'Intelligence Artificielle (Niveaux 1, 2 et 3) visant à classifier des images de chats, de chiens et d'animaux sauvages. 

Le cœur de ce projet repose sur la création et l'entraînement d'un réseau de neurones artificiels **"from scratch" (sans aucune bibliothèque externe de Machine Learning comme TensorFlow ou OpenCV)**. Tout le traitement du signal (FFT, espaces colorimétriques) et l'interface graphique (Swing) sont codés en **Java natif (Vanilla Java)**.

---

## ⚠️ 1. Prérequis & Architecture
Avant de compiler ou d'exécuter le projet, assurez-vous d'avoir téléchargé et décompressé le jeu de données dans un dossier nommé `dataset_animaux/` à la racine du projet. L'arborescence doit être la suivante :
```text
Projet_IA_Groupe1/
├── dataset_animaux/
│   ├── train/ (contenant les images d'entraînement)
│   └── test/  (contenant les images de test inédites)
├── neurone/   (contenant iNeurone.java, Neurone.java, etc.)
├── MenuGraphique.java
├── Image.java
├── TestRGB.java
└── ...
```

*Note : Le dossier `dataset_animaux/` et les fichiers de poids synaptiques (`cerveau_*.txt`) sont ignorés par Git via le `.gitignore` pour ne pas surcharger le dépôt.*

---

## 🛠️ 2. Compilation Globale

Puisque le projet ne requiert aucune dépendance externe, la compilation se fait avec les outils standards du JDK. Ouvrez votre terminal à la racine du projet et exécutez :

```bash
javac *.java neurone\*.java
```

*(Si vous êtes sur Linux/Mac, utilisez un slash : `javac *.java neurone/*.java`)*

---

## 🚀 3. L'Interface Graphique (Dashboard IA Pro - L'Intégrale)

Nous avons développé une interface graphique robuste permettant de piloter l'intégralité du projet, de générer les modèles et de tester les inférences en temps réel.

**Exécution (Windows) :**

```bash
java -Xmx4G -cp ".;neurone" MenuGraphique
```

*(Note : L'argument `-Xmx4G` alloue 4 Go de RAM à Java. Cela est indispensable pour le traitement des matrices d'images en couleurs et des transformées de Fourier sur 20 000 images).*

### Fonctionnalités de l'Interface :

* **🚀 L'Entraînement Séquentiel (L'Usine) :** Permet de lancer l'entraînement en file d'attente de toutes les extensions (Base, RGB, TSL, Miroir, FFT, Multi-classes) avec nettoyage automatique de la RAM (`System.gc()`) entre chaque modèle pour éviter la surcharge.
* **🛑 Le Kill Switch (Arrêt d'Urgence) :** Un bouton stop sécurisé utilisant un pattern d'injection de `RuntimeException` ("Pilule Empoisonnée") pour foudroyer instantanément un apprentissage bloqué sans corrompre les fichiers de sauvegarde `.txt`.
* **🤝 Ensemble Learning (Fusion) :** Permet de fusionner les probabilités de deux cerveaux distincts (ex: RGB + TSL) en temps réel pour améliorer la prédiction finale lors de l'inférence.
* **🎲 Inférence / Animation :** Testez une image ciblée depuis votre disque dur, ou lancez une rafale de 10 images aléatoires inédites pour évaluer les modèles visuellement.

---

## 🧬 4. Exécution Individuelle des Scripts Scientifiques (En ligne de commande)

Si vous souhaitez isoler l'exécution d'un modèle spécifique hors de l'interface graphique pour analyser ses logs détaillés dans la console, voici les commandes associées :

### A. Modèle de Base (Niveaux 1 & 2 - Niveaux de gris)

Charge les images, les normalise, entraîne un classifieur binaire (Chat vs Reste) et évalue son score.

```bash
java -Xmx4G -cp ".;neurone" MainProjet
```

### B. Extensions Colorimétriques (Niveau 3)

* **Espace RGB (Rouge, Vert, Bleu) :** Entraîne le réseau sur les 3 canaux (12 288 synapses au lieu de 4 096) pour une convergence plus rapide.
```bash
java -Xmx4G -cp ".;neurone" TestRGB
```

* **Espace TSL (Teinte, Saturation, Luminosité) :** Convertit mathématiquement le signal visuel. *Démontre empiriquement la limite des réseaux linéaires face à des données circulaires (la Teinte).*
```bash
java -Xmx4G -cp ".;neurone" TestTSL
```

### C. Analyse Fréquentielle Spatiale (FFT)

Aplatit le signal spatial en un signal 1D et applique une Transformée de Fourier Rapide complexe pour extraire les signatures fréquentielles (utilisation intensive du Multi-Threading).

```bash
java -Xmx4G -cp ".;neurone" TestFFT
```

### D. Data Augmentation (Filtre Miroir)

Double la taille du jeu de données (40 000 images) en générant des symétries axiales pour contrer l'overfitting.

```bash
java -Xmx4G -cp ".;neurone" TestMiroir
```

### E. Classification Multi-Classes (One-vs-All)

Entraîne 3 neurones Sigmoïdes en parallèle pour scinder les décisions mathématiques et classer les images en 3 catégories distinctes (Chats, Chiens, Animaux Sauvages).

```bash
java -Xmx4G -cp ".;neurone" TestMultiClasses
```

### F. Crash-Tests et Évaluations Statistiques

* **Matrice de Confusion :** Génère des tableaux croisés dynamiques (2x2 pour le modèle binaire, 3x3 pour le multi-classes) mettant en évidence les faux positifs et faux négatifs.
```bash
java -cp ".;neurone" TestMatriceConfusion
```

* **Sans Normalisation :** Provoque sciemment un "Vanishing Gradient" (saturation de la Sigmoïde) pour prouver l'importance d'un signal borné entre 0.0 et 1.0.
```bash
java -Xmx4G -cp ".;neurone" TestSansNormalisation
```

* **Sans Mélange (Catastrophic Forgetting) :** Démontre l'effondrement de l'IA lorsque la condition *i.i.d* (indépendantes et identiquement distribuées) n'est pas respectée lors de la descente de gradient stochastique.
```bash
java -Xmx4G -cp ".;neurone" TestSansMelange
```

---

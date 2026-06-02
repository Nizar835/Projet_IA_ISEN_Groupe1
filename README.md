# Projet IA - Détection Chiens / Chats (ISEN - Groupe 1)

Ce dépôt contient le code source du projet d'Intelligence Artificielle (Niveaux 1, 2 et 3) visant à différencier des images de chiens et de chats grâce à un réseau de neurones "from scratch" en Java.

## ⚠️ Prérequis très important
Avant de lancer quoi que ce soit, assurez-vous d'avoir téléchargé et décompressé le dossier `dataset_animaux/` à la racine du projet (au même niveau que ce README).  
*Note : Ce dossier est ignoré par Git (via le `.gitignore`) pour ne pas surcharger le dépôt.*

---

## 🚀 1. Lancer le Programme Principal (Niveaux 1 & 2)

Le fichier `MainProjet.java` est le chef d'orchestre. Il charge les images, les normalise, entraîne le réseau binaire, esquive les images corrompues, et lance une démonstration.

**Compilation :**
```bash
javac Image.java MainProjet.java neurone\*.java
```

**Exécution :**
```bash
java -cp ".;neurone" MainProjet
```

---

## 🧬 2. Lancer les Extensions (Niveau 3)

⚠️ **IMPORTANT (RAM) :** Les scripts suivants manipulent beaucoup de données (jusqu'à 20 000 images ou des images RGB 3x plus lourdes). Nous utilisons l'argument `-Xmx4G` lors de l'exécution pour autoriser Java à utiliser jusqu'à 4 Go de mémoire vive afin d'éviter les crashs systèmes (`OutOfMemoryError`).

### A. Test de l'apprentissage en Couleurs (RGB)

Entraîne le réseau sur les images en couleurs (12 288 synapses au lieu de 4 096).  
**Compilation :** `javac Image.java TestRGB.java neurone\*.java`  
**Exécution :** `java -Xmx4G -cp ".;neurone" TestRGB`

### B. Test de l'Augmentation de Données (Miroir)

Double la taille du dataset (20 000 images) en appliquant un effet miroir horizontal sur chaque image.  
**Compilation :** `javac Image.java FiltreImage.java TestMiroir.java neurone\*.java`  
**Exécution :** `java -Xmx4G -cp ".;neurone" TestMiroir`

### C. Test de la Classification Multi-Classes (3 Catégories)

Entraîne 3 neurones en parallèle pour classer les images de test entre CHAT, CHIEN et WILD.  
**Compilation :** `javac Image.java TestMultiClasses.java neurone\*.java`  
**Exécution :** `java -Xmx4G -cp ".;neurone" TestMultiClasses`

### D. Matrice de Confusion (Analyse des erreurs)

Génère un tableau croisé 3x3 pour voir quelles catégories l'IA a tendance à confondre.  
**Compilation :** `javac Image.java TestMatriceConfusion.java neurone\*.java`  
**Exécution :** `java -Xmx4G -cp ".;neurone" TestMatriceConfusion`

### E. Test d'Hyper-Paramètres (Optimisation de l'ETA)

Cherche la valeur mathématique optimale de mise à jour des poids synaptiques pour apprendre plus vite sans stagner.  
**Compilation :** `javac Image.java TestHyperParametres.java neurone\*.java`  
**Exécution :** `java -cp ".;neurone" TestHyperParametres`

---
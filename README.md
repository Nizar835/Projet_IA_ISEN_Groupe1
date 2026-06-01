# Projet IA - Détection Chiens / Chats (ISEN - Groupe 1)

Ce dépôt contient le code source du projet d'Intelligence Artificielle (Niveaux 1, 2 et 3) visant à différencier des images de chiens et de chats grâce à un réseau de neurones "from scratch" en Java.

## ⚠️ Prérequis très important
Avant de lancer quoi que ce soit, assurez-vous d'avoir téléchargé et décompressé le dossier `dataset_animaux/` à la racine du projet (au même niveau que ce README). 
*Note : Ce dossier est ignoré par Git (via le `.gitignore`) pour ne pas surcharger le dépôt.*

---

## 🚀 1. Lancer le Programme Principal (Niveaux 1 & 2)

Le fichier `MainProjet.java` est le chef d'orchestre. Il charge les images, les normalise, entraîne le réseau, esquive les images corrompues, et lance une démonstration de test.

**1. Compilation :**
Ouvrez votre terminal (PowerShell / CMD) à la racine du projet et tapez :
```bash
javac Image.java MainProjet.java neurone\*.java
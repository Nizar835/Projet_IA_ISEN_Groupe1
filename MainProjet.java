import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MainProjet 
{
	public static void main(String[] args) 
	{
		System.out.println("--- DÉMARRAGE DE LA CHAÎNE DE TRAITEMENT ---");
		
		// --- 1. LECTURE ET LABELLISATION ---
		// Assure-toi que le dossier 'dataset_animaux' est bien à la racine de ton projet
		String dossierTrain = "dataset_animaux/train/";
		List<String> cheminsFichiers = Image.listeFichiers(dossierTrain);
		
		if (cheminsFichiers == null || cheminsFichiers.isEmpty()) {
			System.out.println("Erreur : Aucune image trouvée. Vérifie le dossier dataset_animaux.");
			return;
		}

		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Lecture, labellisation et normalisation de " + cheminsFichiers.size() + " images...");

		for (String chemin : cheminsFichiers) 
		{
			// Labellisation : 1 si c'est un chat (actif), 0 si ce n'est pas un chat (inactif)
			int labelAttendu = chemin.contains("cat") ? 1 : 0;
			
			// Chargement de l'image en niveaux de gris (true)
			Image img = new Image(chemin, labelAttendu, true);
			
			// --- 2. NORMALISATION ---
			int[] pixelsBruts = img.donnees();
			float[] pixelsNormalises = new float[pixelsBruts.length];
			
			// Diviser par 255 pour avoir des valeurs entre 0 et 1
			for (int i = 0; i < pixelsBruts.length; i++) {
				pixelsNormalises[i] = pixelsBruts[i] / 255.0f;
			}
			
			listeEntrees.add(pixelsNormalises);
			listeResultats.add((float) labelAttendu);
		}
		
		// --- 3. MÉLANGE DES DONNÉES ---
		System.out.println("Mélange des données d'entraînement...");
		
		// Création d'une liste d'index (0, 1, 2, ..., N-1)
		List<Integer> indexList = new ArrayList<>();
		for (int i = 0; i < listeEntrees.size(); i++) {
			indexList.add(i);
		}
		
		// Mélange aléatoire des index
		Collections.shuffle(indexList);
		
		// Création de nouvelles listes pour stocker les données dans le nouvel ordre
		List<float[]> entreesMelangees = new ArrayList<>();
		List<Float> resultatsMelanges = new ArrayList<>();
		
		for (int index : indexList) {
			entreesMelangees.add(listeEntrees.get(index));
			resultatsMelanges.add(listeResultats.get(index));
		}
		
		// Remplacement des anciennes listes par les nouvelles bien mélangées
		listeEntrees = entreesMelangees;
		listeResultats = resultatsMelanges;
		
		System.out.println("Mélange terminé ! Les labels correspondent toujours aux bonnes images.");
		
		// --- 4. ENTRAÎNEMENT DU NEURONE ---
		System.out.println("Conversion des données et instanciation du réseau...");
		
		// Conversion de la Liste d'entrées en tableau 2D natif
		float[][] entreesArray = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++) {
			entreesArray[i] = listeEntrees.get(i);
		}
		
		// Conversion de la Liste de résultats en tableau 1D natif
		float[] resultatsArray = new float[listeResultats.size()];
		for (int i = 0; i < listeResultats.size(); i++) {
			resultatsArray[i] = listeResultats.get(i);
		}
		
		// Le nombre de synapses du neurone doit correspondre exactement au nombre de pixels d'une image
		int nbEntreesNeurone = entreesArray[0].length;
		
		// On crée le neurone avec la fonction Sigmoïde
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		
		// Définition de la limite d'erreur. 
		final float MSElimite = 0.05f; 
		
		System.out.println("Début de l'apprentissage supervisé sur " + entreesArray.length + " images...");
		System.out.println("Cette étape peut prendre du temps (surveillez la MSE dans la console).");
		
		// Lancement de la descente de gradient
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		
		System.out.println(">>> Apprentissage terminé avec succès ! <<<");

        // --- 6. PHASE DE TEST (ÉVALUATION DU MODÈLE) ---
		System.out.println("\n--- DÉBUT DE LA PHASE DE TEST ---");
		
		// Le dossier contenant les images de test (à ne jamais utiliser pour l'entraînement !)
		String dossierTest = "dataset_animaux/test/";
		List<String> fichiersTest = Image.listeFichiers(dossierTest);
		
		int bonnesReponses = 0;
		int totalTest = fichiersTest.size();
		
		System.out.println("Évaluation sur " + totalTest + " nouvelles images...");

		for (String chemin : fichiersTest) 
		{
			// 1. Labellisation (1 = chat, 0 = chien/inconnu)
			int labelAttendu = chemin.contains("cat") ? 1 : 0;
			
			// 2. Lecture et Normalisation (exactement comme pour l'entraînement)
			Image imgTest = new Image(chemin, labelAttendu, true);
			int[] pixelsBrutsTest = imgTest.donnees();
			float[] pixelsNormalisesTest = new float[pixelsBrutsTest.length];
			
			for (int i = 0; i < pixelsBrutsTest.length; i++) {
				pixelsNormalisesTest[i] = pixelsBrutsTest[i] / 255.0f;
			}
			
			// 3. Prédiction par le neurone entraîné
			neurone.metAJour(pixelsNormalisesTest);
			float predictionBrute = neurone.sortie();
			
			// Si la sortie est > 0.5, le neurone pense que c'est un chat (1), sinon un chien (0)
			int labelPredit = (predictionBrute > 0.5f) ? 1 : 0;
			
			if (labelPredit == labelAttendu) {
				bonnesReponses++;
			}
		}
		
		// Calcul et affichage du score de réussite
		float pourcentageReussite = ((float) bonnesReponses / totalTest) * 100;
		System.out.printf(">>> Résultat du test : %d bonnes réponses sur %d (soit %.2f %% de précision) <<<\n", 
							bonnesReponses, totalTest, pourcentageReussite);
	}
}
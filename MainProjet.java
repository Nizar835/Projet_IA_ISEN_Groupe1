import java.util.List;
import java.util.ArrayList;

public class MainProjet 
{
	public static void main(String[] args) 
	{
		System.out.println("--- DÉMARRAGE DE LA CHAÎNE DE TRAITEMENT ---");
		
		// 1. Définir le chemin vers le dossier d'entraînement
		String dossierTrain = "dataset_animaux/train/";
		
		// Récupérer la liste de tous les chemins d'images
		List<String> cheminsFichiers = Image.listeFichiers(dossierTrain);
		
		if (cheminsFichiers == null || cheminsFichiers.isEmpty()) {
			System.out.println("Erreur : Aucune image trouvée. Vérifie le dossier dataset_animaux.");
			return;
		}

		// Préparation des listes dynamiques pour stocker les entrées (pixels) et résultats attendus (labels)
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Lecture, labellisation et normalisation de " + cheminsFichiers.size() + " images...");

		// 2. Parcourir chaque fichier
		for (String chemin : cheminsFichiers) 
		{
			// Labellisation : 1 si c'est un chat, 0 si ce n'est pas un chat
			int labelAttendu = chemin.contains("cat") ? 1 : 0;
			
			// Chargement de l'image en niveaux de gris (true)
			Image img = new Image(chemin, labelAttendu, true);
			
			// Récupération des pixels bruts (de 0 à 255)
			int[] pixelsBruts = img.donnees();
			float[] pixelsNormalises = new float[pixelsBruts.length];
			
			// 3. Normalisation : diviser par 255 pour avoir des valeurs entre 0 et 1
			for (int i = 0; i < pixelsBruts.length; i++) {
				pixelsNormalises[i] = pixelsBruts[i] / 255.0f;
			}
			
			// Ajout aux listes
			listeEntrees.add(pixelsNormalises);
			listeResultats.add((float) labelAttendu);
		}
		
		System.out.println("Données prêtes !");
		
		// Il restera à mélanger ces listes (Étape 4) avant de les donner au neurone...
	}
}
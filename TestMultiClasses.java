import java.util.List;
import java.util.ArrayList;

public class TestMultiClasses 
{
	public static void main(String[] args) 
	{
		System.out.println("=== EXTENSION NIVEAU 3 : CLASSIFICATION MULTI-CLASSES (3 CATÉGORIES) ===");
		
		// 1. CHARGEMENT ET MÉLANGE DES DONNÉES D'ENTRAÎNEMENT
		System.out.println("Lecture du dataset d'entraînement...");
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		
		if (imagesTrain == null || imagesTrain.isEmpty()) {
			System.out.println("Erreur : Aucune image trouvée.");
			return;
		}
		System.out.println("Mélange des données...");
		Image.melange(imagesTrain);

		// Préparation des tableaux pour l'apprentissage
		List<float[]> listeEntrees = new ArrayList<>();
		List<Integer> listeLabels = new ArrayList<>();

		for (Image img : imagesTrain) {
			if (img.donnees() == null) continue; // Bouclier anti-crash
			listeEntrees.add(img.donneesNormalisees());
			listeLabels.add(img.label()); // On garde le label brut (0=Chat, 1=Chien, 2=Wild)
		}

		float[][] entreesArray = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++) {
			entreesArray[i] = listeEntrees.get(i);
		}

		int nbImages = entreesArray.length;
		int nbEntreesNeurone = entreesArray[0].length;

		// 2. CRÉATION ET ENTRAÎNEMENT DES 3 NEURONES SPÉCIALISTES
		// Pour chaque neurone, on crée son tableau de consignes (1 s'il doit s'activer, 0 sinon)
		float[] consignesChat = new float[nbImages];
		float[] consignesChien = new float[nbImages];
		float[] consignesWild = new float[nbImages];

		for (int i = 0; i < nbImages; i++) {
			int labelBrut = listeLabels.get(i);
			consignesChat[i]  = (labelBrut == 0) ? 1.0f : 0.0f; // S'active uniquement sur les chats
			consignesChien[i] = (labelBrut == 1) ? 1.0f : 0.0f; // S'active uniquement sur les chiens
			consignesWild[i]  = (labelBrut == 2) ? 1.0f : 0.0f; // S'active uniquement sur les sauvages
		}

		final float MSElimite = 0.18f; // Seuil tolérant pour le test rapide

		System.out.println("\n--- Entraînement du Neurone Spécialiste CHAT ---");
		iNeurone neuroneChat = new NeuroneSigmoide(nbEntreesNeurone);
		neuroneChat.apprentissage(entreesArray, consignesChat, MSElimite);

		System.out.println("\n--- Entraînement du Neurone Spécialiste CHIEN ---");
		iNeurone neuroneChien = new NeuroneSigmoide(nbEntreesNeurone);
		neuroneChien.apprentissage(entreesArray, consignesChien, MSElimite);

		System.out.println("\n--- Entraînement du Neurone Spécialiste WILD ---");
		iNeurone neuroneWild = new NeuroneSigmoide(nbEntreesNeurone);
		neuroneWild.apprentissage(entreesArray, consignesWild, MSElimite);

		// 3. ÉVALUATION COMPLÈTE SUR LE JEU DE TEST (3 200 IMAGES)
		System.out.println("\n--- ÉVALUATION GLOBALE SUR LE JEU DE TEST ---");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest != null && !imagesTest.isEmpty()) {
			int totalSains = 0;
			int TotalBonnesReponses = 0;
			
			int chatsBienPredits = 0, totalChats = 0;
			int chiensBienPredits = 0, totalChiens = 0;
			int wildBienPredits = 0, totalWild = 0;

			for (Image imgTest : imagesTest) {
				if (imgTest.donnees() == null) continue;
				totalSains++;

				float[] pixels = imgTest.donneesNormalisees();
				int vraiLabel = imgTest.label();

				// On demande l'avis aux 3 neurones en même temps
				neuroneChat.metAJour(pixels);
				float probaChat = neuroneChat.sortie();

				neuroneChien.metAJour(pixels);
				float probaChien = neuroneChien.sortie();

				neuroneWild.metAJour(pixels);
				float probaWild = neuroneWild.sortie();

				// Le système choisit la catégorie qui a obtenu le plus fort pourcentage
				int labelPredit = 0;
				float maxProba = probaChat;

				if (probaChien > maxProba) {
					maxProba = probaChien;
					labelPredit = 1;
				}
				if (probaWild > maxProba) {
					maxProba = probaWild;
					labelPredit = 2;
				}

				// Statistiques par catégorie
				if (vraiLabel == 0) { totalChats++; if (labelPredit == 0) chatsBienPredits++; }
				if (vraiLabel == 1) { totalChiens++; if (labelPredit == 1) chiensBienPredits++; }
				if (vraiLabel == 2) { totalWild++; if (labelPredit == 2) wildBienPredits++; }

				if (labelPredit == vraiLabel) {
					TotalBonnesReponses++;
				}
			}

			// Affichage des scores détaillés pour le rapport
			System.out.println("\n================ RÉSULTATS DÉTAILLÉS ================");
			System.out.printf("Précision CHATS   : %.2f %% (%d/%d)\n", ((float)chatsBienPredits/totalChats)*100, chatsBienPredits, totalChats);
			System.out.printf("Précision CHIENS  : %.2f %% (%d/%d)\n", ((float)chiensBienPredits/totalChiens)*100, chiensBienPredits, totalChiens);
			System.out.printf("Précision WILD    : %.2f %% (%d/%d)\n", ((float)wildBienPredits/totalWild)*100, wildBienPredits, totalWild);
			System.out.println("-----------------------------------------------------");
			System.out.printf(">>> SCORE GLOBAL DU SYSTÈME MULTI-CLASSES : %.2f %% (%d/%d) <<<\n", 
								((float)TotalBonnesReponses / totalSains) * 100, TotalBonnesReponses, totalSains);
			System.out.println("=====================================================");
		}
	}
}
import java.util.List;
import java.util.ArrayList;

public class TestMatriceConfusion 
{
	public static void main(String[] args) 
	{
		System.out.println("=== EXTENSION BONUS : GÉNÉRATION DE LA MATRICE DE CONFUSION ===");
		
		// 1. CHARGEMENT ET ENTRAÎNEMENT RAPIDE
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		if (imagesTrain == null || imagesTrain.isEmpty()) return;
		Image.melange(imagesTrain);

		List<float[]> listeEntrees = new ArrayList<>();
		List<Integer> listeLabels = new ArrayList<>();
		for (Image img : imagesTrain) {
			if (img.donnees() != null) {
				listeEntrees.add(img.donneesNormalisees());
				listeLabels.add(img.label());
			}
		}

		float[][] entrees = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++) entrees[i] = listeEntrees.get(i);
		
		int nbImages = entrees.length;
		float[] consignesChat = new float[nbImages];
		float[] consignesChien = new float[nbImages];
		float[] consignesWild = new float[nbImages];

		for (int i = 0; i < nbImages; i++) {
			int lbl = listeLabels.get(i);
			consignesChat[i]  = (lbl == 0) ? 1.0f : 0.0f;
			consignesChien[i] = (lbl == 1) ? 1.0f : 0.0f;
			consignesWild[i]  = (lbl == 2) ? 1.0f : 0.0f;
		}

		System.out.println("Entraînement des 3 neurones en cours (MSE limite : 0.08)...");
		final float MSE = 0.2f;
		int synapses = entrees[0].length;
		
		iNeurone neuroneChat = new NeuroneSigmoide(synapses);
		neuroneChat.apprentissage(entrees, consignesChat, MSE);
		
		iNeurone neuroneChien = new NeuroneSigmoide(synapses);
		neuroneChien.apprentissage(entrees, consignesChien, MSE);
		
		iNeurone neuroneWild = new NeuroneSigmoide(synapses);
		neuroneWild.apprentissage(entrees, consignesWild, MSE);

		// 2. CRÉATION DE LA MATRICE DE CONFUSION
		System.out.println("\nCalcul de la matrice sur le jeu de test...");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		// Tableau 3x3 : Ligne = Vraie catégorie, Colonne = Prédiction de l'IA
		int[][] matrice = new int[3][3];

		for (Image imgTest : imagesTest) {
			if (imgTest.donnees() == null) continue;
			
			float[] pixels = imgTest.donneesNormalisees();
			int vraiLabel = imgTest.label(); // 0=Chat, 1=Chien, 2=Wild
			
			neuroneChat.metAJour(pixels);
			neuroneChien.metAJour(pixels);
			neuroneWild.metAJour(pixels);
			
			float maxProba = neuroneChat.sortie();
			int labelPredit = 0;
			
			if (neuroneChien.sortie() > maxProba) {
				maxProba = neuroneChien.sortie();
				labelPredit = 1;
			}
			if (neuroneWild.sortie() > maxProba) {
				labelPredit = 2;
			}
			
			// On incrémente la case correspondante dans la matrice
			if (vraiLabel >= 0 && vraiLabel <= 2) {
				matrice[vraiLabel][labelPredit]++;
			}
		}

		// 3. AFFICHAGE DE LA MATRICE (Formatage pour le terminal)
		System.out.println("\n================ MATRICE DE CONFUSION ================");
		System.out.println("                 | PRÉDIT CHAT | PRÉDIT CHIEN| PRÉDIT WILD |");
		System.out.println("-----------------|-------------|-------------|-------------|");
		System.out.printf(" VRAI CHAT (0)   | %11d | %11d | %11d |\n", matrice[0][0], matrice[0][1], matrice[0][2]);
		System.out.printf(" VRAI CHIEN(1)   | %11d | %11d | %11d |\n", matrice[1][0], matrice[1][1], matrice[1][2]);
		System.out.printf(" VRAI WILD (2)   | %11d | %11d | %11d |\n", matrice[2][0], matrice[2][1], matrice[2][2]);
		System.out.println("======================================================");
		System.out.println("Lecture : La diagonale représente les bonnes réponses.");
		System.out.println("Les autres cases montrent quelles espèces l'IA confond.");
	}
}
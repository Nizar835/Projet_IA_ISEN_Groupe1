import java.util.List;
import java.io.File;

public class TestMatriceConfusion 
{
	public static void main(String[] args) 
	{
		System.out.println("=== ÉVALUATION SCIENTIFIQUE : MATRICES DE CONFUSION ===");
		System.out.println("Chargement des cerveaux depuis le disque dur...");
		
		int synapses = 10000; 
		try {
		    List<Image> sample = Image.chargeDataset("dataset_animaux/test/", true);
		    if (sample != null && !sample.isEmpty()) {
		        synapses = sample.get(0).donnees().length;
		    }
		} catch (Exception e) {}

		// --- 1. CHARGEMENT DU MODÈLE BINAIRE ---
		iNeurone neuroneBinaire = new NeuroneSigmoide(synapses);
		if (new File("cerveau_binaire.txt").exists()) {
			neuroneBinaire.chargement("cerveau_binaire.txt");
		} else {
			System.out.println("Attention : cerveau_binaire.txt introuvable.");
		}

		// --- 2. CHARGEMENT DES MODÈLES MULTI-CLASSES ---
		iNeurone neuroneChat = new NeuroneSigmoide(synapses);
		iNeurone neuroneChien = new NeuroneSigmoide(synapses);
		iNeurone neuroneWild = new NeuroneSigmoide(synapses);
		
		if (new File("cerveau_chat.txt").exists()) {
			neuroneChat.chargement("cerveau_chat.txt");
			neuroneChien.chargement("cerveau_chien.txt");
			neuroneWild.chargement("cerveau_wild.txt");
		}

		System.out.println("\nCalcul des matrices sur le jeu de test...");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest == null || imagesTest.isEmpty()) {
		    System.out.println("Erreur : Aucun dataset de test trouvé.");
		    return;
		}

		// Initialisation des tableaux
		int[][] matriceBinaire = new int[2][2]; // 0=Chat, 1=Non-Chat
		int[][] matriceMulti = new int[3][3];   // 0=Chat, 1=Chien, 2=Wild

		for (Image imgTest : imagesTest) {
			if (imgTest.donnees() == null) continue;
			
			float[] pixels = imgTest.donneesNormalisees();
			int vraiLabelBrut = imgTest.label(); 
			
			// ==========================================
			// ÉVALUATION DU MODÈLE BINAIRE (BASE)
			// ==========================================
			neuroneBinaire.metAJour(pixels);
			float probaBinaire = neuroneBinaire.sortie();
			
			int vraiLabelBin = (vraiLabelBrut == 0) ? 0 : 1; 
			int predLabelBin = (probaBinaire >= 0.5f) ? 0 : 1;
			matriceBinaire[vraiLabelBin][predLabelBin]++;

			// ==========================================
			// ÉVALUATION DU MODÈLE MULTI-CLASSES
			// ==========================================
			neuroneChat.metAJour(pixels);
			neuroneChien.metAJour(pixels);
			neuroneWild.metAJour(pixels);
			
			float maxProba = neuroneChat.sortie();
			int predLabelMulti = 0;
			
			if (neuroneChien.sortie() > maxProba) {
				maxProba = neuroneChien.sortie();
				predLabelMulti = 1;
			}
			if (neuroneWild.sortie() > maxProba) {
				predLabelMulti = 2;
			}
			
			if (vraiLabelBrut >= 0 && vraiLabelBrut <= 2) {
				matriceMulti[vraiLabelBrut][predLabelMulti]++;
			}
		}

		// --- CALCUL DES POURCENTAGES ---
		int totalBinaire = matriceBinaire[0][0] + matriceBinaire[0][1] + matriceBinaire[1][0] + matriceBinaire[1][1];
		float precisionBinaire = ((float)(matriceBinaire[0][0] + matriceBinaire[1][1]) / totalBinaire) * 100.0f;

		int totalMulti = 0;
		int bonnesMulti = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				totalMulti += matriceMulti[i][j];
				if (i == j) bonnesMulti += matriceMulti[i][j]; // La diagonale
			}
		}
		float precisionMulti = ((float)bonnesMulti / totalMulti) * 100.0f;

		// --- AFFICHAGE MATRICE BINAIRE ---
		System.out.println("\n================ 1. MATRICE DE CONFUSION (MODÈLE DE BASE) ================");
		System.out.println("                 | PRÉDIT CHAT (0) | PRÉDIT NON-CHAT (1) |");
		System.out.println("-----------------|-----------------|---------------------|");
		System.out.printf(" VRAI CHAT (0)   | %15d | %19d | <-- Faux Négatifs (Chats ratés)\n", matriceBinaire[0][0], matriceBinaire[0][1]);
		System.out.printf(" VRAI NON-CHAT(1)| %15d | %19d | \n", matriceBinaire[1][0], matriceBinaire[1][1]);
		System.out.println("                   ^ Faux Positifs (Pris pour des chats)");
		System.out.println("--------------------------------------------------------------------------");
		System.out.printf(">>> PRÉCISION GLOBALE BINAIRE : %.2f %% <<<\n", precisionBinaire);

		// --- AFFICHAGE MATRICE MULTI-CLASSES ---
		System.out.println("\n================ 2. MATRICE DE CONFUSION (MULTI-CLASSES) =================");
		System.out.println("                 | PRÉDIT CHAT | PRÉDIT CHIEN| PRÉDIT WILD |");
		System.out.println("-----------------|-------------|-------------|-------------|");
		System.out.printf(" VRAI CHAT (0)   | %11d | %11d | %11d |\n", matriceMulti[0][0], matriceMulti[0][1], matriceMulti[0][2]);
		System.out.printf(" VRAI CHIEN(1)   | %11d | %11d | %11d |\n", matriceMulti[1][0], matriceMulti[1][1], matriceMulti[1][2]);
		System.out.printf(" VRAI WILD (2)   | %11d | %11d | %11d |\n", matriceMulti[2][0], matriceMulti[2][1], matriceMulti[2][2]);
		System.out.println("==========================================================================");
		System.out.printf(">>> PRÉCISION GLOBALE MULTI-CLASSES : %.2f %% <<<\n", precisionMulti);
		System.out.println("==========================================================================");
	}
}
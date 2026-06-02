import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class TestTSL 
{
	public static void main(String[] args) 
	{
		System.out.println("=== EXTENSION NIVEAU 3 : COULEUR TSL (Teinte, Saturation, Luminosité) ===");
		
		// 1. LECTURE DES NOMS DE FICHIERS D'ENTRAÎNEMENT
		System.out.println("Lecture du dataset d'entraînement...");
		String dossierTrain = "dataset_animaux/train/";
		
		// On réutilise la méthode statique de votre classe Image
		List<String> fichiersTrain = Image.listeFichiers(dossierTrain); 
		
		if (fichiersTrain == null || fichiersTrain.isEmpty()) {
			System.out.println("Erreur : Dossier d'entraînement introuvable.");
			return;
		}

		System.out.println("Mélange des fichiers...");
		Collections.shuffle(fichiersTrain);
		
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Conversion matricielle des images en espace TSL...");

		int index = 0;
		for (String chemin : fichiersTrain) 
		{
			try {
				// Lecture de l'image originale (en couleur pure)
				BufferedImage bimg = ImageIO.read(new File(chemin));
				if (bimg == null) continue;

				int largeur = bimg.getWidth();
				int hauteur = bimg.getHeight();
				
				// Un pixel TSL = 3 valeurs flottantes
				float[] pixelsTSL = new float[largeur * hauteur * 3];
				int pos = 0;
				
				for (int y = 0; y < hauteur; y++) {
					for (int x = 0; x < largeur; x++) {
						// Extraction du pixel RGB
						int rgb = bimg.getRGB(x, y);
						Color couleur = new Color(rgb, true);
						
						int r = couleur.getRed();
						int g = couleur.getGreen();
						int b = couleur.getBlue();
						
						// --- LA MAGIE DES MATHS : Conversion RGB vers TSL (HSB) ---
						float[] tsl = new float[3];
						Color.RGBtoHSB(r, g, b, tsl);
						
						// tsl[0] = Teinte (de 0.0 à 1.0)
						// tsl[1] = Saturation (de 0.0 à 1.0)
						// tsl[2] = Luminosité (de 0.0 à 1.0)
						
						pixelsTSL[pos] = tsl[0];
						pixelsTSL[pos+1] = tsl[1];
						pixelsTSL[pos+2] = tsl[2];
						pos += 3;
					}
				}
				
				// Détermination du label : Chat = 1.0, Reste = 0.0
				float labelPourNeurone = chemin.contains("cat") ? 1.0f : 0.0f;
				
				listeEntrees.add(pixelsTSL);
				listeResultats.add(labelPourNeurone);
				
				index++;
				// Petit affichage pour faire patienter, car la conversion est lourde
				if (index % 5000 == 0) {
					System.out.println(index + " images converties...");
				}
				
			} catch (Exception e) {
				// Bouclier anti-crash : on ignore les fichiers système corrompus
			}
		}
		
		System.out.println("Toutes les images sont converties ! Taille du dataset : " + listeEntrees.size());

		// 2. PRÉPARATION DES MATRICES POUR LE RÉSEAU
		float[][] entreesArray = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++) {
			entreesArray[i] = listeEntrees.get(i);
		}
		
		float[] resultatsArray = new float[listeResultats.size()];
		for (int i = 0; i < listeResultats.size(); i++) {
			resultatsArray[i] = listeResultats.get(i);
		}
		
		// 3. ENTRAÎNEMENT DU NEURONE
		int nbEntreesNeurone = entreesArray[0].length; // Ce sera 3 fois plus grand que le gris !
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.15f; 
		
		System.out.println("Début de l'apprentissage (Attention, c'est très lourd : il y a 3 canaux de couleur !)");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		System.out.println(">>> Apprentissage TSL terminé avec succès ! <<<");

		// 4. SAUVEGARDE
		neurone.sauvegarde("cerveau_tsl.txt");
		System.out.println("Cerveau sauvegardé sous le nom 'cerveau_tsl.txt'.");
	}
}
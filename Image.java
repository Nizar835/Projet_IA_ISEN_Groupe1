import java.io.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.*;
import java.nio.file.*;
import java.util.stream.*;

public class Image
{
    // Modifié en public pour être accessible depuis testNeurone si besoin
    static public int LabelChat = 0; 
    static public int LabelChien = 1;
    static public int LabelWild = 2;
    static public int LabelInconnu = 3;
    
    private int label = -1;
    private int largeur = 0;
    private int hauteur = 0;
    private int[] donnees = null; // image aplatie en concaténant les lignes

    public int label() {return label;}
    public int largeur() {return largeur;}
    public int hauteur() {return hauteur;}
    public int taille() {return donnees.length;} // 1 canal (gris) ou 3 canaux (RGB)
    public int[] donnees() {return donnees;}

    public boolean estEnNiveauxDeGris() {
        return taille() == largeur() * hauteur();
    }

    // Normalise les pixels de 0-255 vers 0.0-1.0 pour le neurone
    public float[] donneesNormalisees() {
        if (this.donnees == null) return new float[0]; // Protection anti-crash
        
        float[] normalise = new float[donnees.length];
        for (int i = 0; i < donnees.length; i++) {
            normalise[i] = donnees[i] / 255.0f;
        }
        return normalise;
    }

    public void afficheMetadonnees() {
        String type = estEnNiveauxDeGris() ? "grayscale" : " couleurs";
        System.out.printf("Image (%s): label=%d, largeur=%d, hauteur=%d, taille=%d\n",
            type, label(), largeur(), hauteur(), taille());
    }

    public Image(final String cheminImage, int label, boolean niveauxDeGris) {
        try {
            final BufferedImage img = ImageIO.read(new File(cheminImage));
            this.label = label;
            largeur = img.getWidth(null);
            hauteur = img.getHeight(null);
            final int taille = niveauxDeGris ? hauteur*largeur : 3*hauteur*largeur;
            donnees = new int[taille];
            
            for (int i = 0; i < hauteur; ++i) {
                for (int j = 0; j < largeur; ++j) {
                    final long rgb = img.getRGB(j, i);
                    final int r = (int)((rgb>>16)&255); // Isoler la composante rouge
                    final int g = (int)((rgb>>8)&255);  // Isoler la composante verte
                    final int b = (int)((rgb)&255);     // Isoler la composante bleue
                    final int index = i * largeur + j;
                    
                    if (niveauxDeGris) {
                        final float gris = 0.2125f * r + 0.7154f * g + 0.0721f * b; // RGB -> niveaux de gris
                        donnees[index] = (int) Math.max(0, Math.min(255, gris));
                    }
                    else {
                        donnees[3*index+0] = r;
                        donnees[3*index+1] = g;
                        donnees[3*index+2] = b;
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.printf("Image non trouvée ou non lisible: %s\n", cheminImage);
        }
    }

    // Charge toutes les images d'un dossier avec leur label automatique
    public static List<Image> chargeDataset(String repertoire, boolean niveauxDeGris) {
        List<Image> images = new ArrayList<>();
        List<String> fichiers = listeFichiers(repertoire);
        
        if (fichiers != null) {
            for (String chemin : fichiers) {
                int label = chemin.contains("cat") ? LabelChat :
                            chemin.contains("dog") ? LabelChien :
                            chemin.contains("wild") ? LabelWild : LabelInconnu;
                images.add(new Image(chemin, label, niveauxDeGris));
            }
        }
        return images;
    }

    // Mélange aléatoirement la liste d'images
    public static void melange(List<Image> images) {
        Collections.shuffle(images);
    }

    public static List<String> listeFichiers(String repertoire) {
        List<String> cheminsFichiers = null;
        try {
            cheminsFichiers = Files.walk(Paths.get(repertoire)) 
                .filter(Files::isRegularFile)                   
                .map(Path::toAbsolutePath)                      
                .map(Path::toString)                            
                .collect(Collectors.toList());                  
        } catch (Exception e) {
            System.err.println("Dossier introuvable ou vide : " + repertoire);
        }
        return cheminsFichiers;
    }

    public static void main (String[] args)
    {
        List<Image> images = chargeDataset("dataset_animaux/train/", true);
        if (images != null && !images.isEmpty()) {
            melange(images);
            System.out.println("Nombre d'images chargées : " + images.size());
            images.get(0).afficheMetadonnees();
        } else {
            System.out.println("Aucune image trouvée pour le test de la classe.");
        }
    }
}
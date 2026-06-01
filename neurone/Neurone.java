import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public abstract class Neurone implements iNeurone
{
    // Coefficient de mise à jour des poids (Taux d'apprentissage),
    // commun (static) à tous les neurones. Il définit la vitesse à laquelle 
    // le neurone modifie ses certitudes. S'il est trop grand, le neurone diverge.
    private static float eta = 0.0001f;
    
    // Accesseur en écriture seule, permettant de modifier
    // eta pour tous les neurones pendant l'exécution
    public static void fixeCoefApprentissage(final float nouvelEta) {eta = nouvelEta;}
    
    // Tolérance immuable (final) et générique (car commune à tous les neurones
    // par le mot-clé static) permettant d'accepter la sortie d'un neurone comme valable
    public static final float ToleranceSortie = 1.e-2f;
    
    // Tableau des poids synaptiques d'un neurone (l'importance accordée à chaque pixel)
    private float[] synapses;
    
    // Biais associé aux poids synaptiques d'un neurone (le seuil de déclenchement)
    private float biais;
    
    // Valeur de sortie d'un neurone (à "Not A Number" par défaut)
    private float etatInterne = Float.NaN;
    
    // Fonction d'activation d'un neurone ; à modifier par héritage,
    // c'est d'ailleurs le but ici du qualificateur abstract, qui dit que cette
    // méthode n'est pas implémentée => à faire dans un ou plusieurs classes filles
    // activation est protected car elle n'a pas à être vue de l'extérieur,
    // mais doit être redéfinie dans les classes filles
    protected abstract float activation(final float valeur);

    // Constructeur d'un neurone
    public Neurone(final int nbEntrees)
    {
        synapses = new float[nbEntrees];
        // On initialise tous les poids de manière aléatoire (entre -1 et 1)
        for (int i = 0; i < nbEntrees; ++i)
            synapses[i] = (float)(Math.random()*2.-1.);
        // On initialise le biais de manière aléatoire (entre -1 et 1)
        biais = (float)(Math.random()*2.-1.);
    }

    // Accesseur pour la valeur de sortie
    public float sortie() {return etatInterne;}
    
    // Donne accès en lecture-écriture aux valeurs des poids synaptiques
    public float[] synapses() {return synapses;}
    // Donne accès en lecture à la valeur du biais
    public float biais() {return biais;}
    // Donne accès en écriture à la valeur du biais
    public void fixeBiais(final float nouveauBiais) {biais = nouveauBiais;}
    
    // Calcule la valeur de sortie en fonction des entrées, des poids synaptiques,
    // du biais et de la fonction d'activation
    public void metAJour(final float[] entrees)
    {
        // On démarre en extrayant le biais
        float somme = biais();
        
        // Puis on ajoute les produits entrée-poids synaptique (Somme pondérée)
        for (int i = 0; i < synapses().length; ++i)
            somme += entrees[i]*synapses()[i];
        
        // On fixe la sortie du neurone relativement à la fonction d'activation
        etatInterne = activation(somme);
    }
    
    // =========================================================================
    // EXPLICATION DE L'ALGORITHME D'APPRENTISSAGE (NIVEAU 1)
    // =========================================================================
    // Cette méthode implémente un apprentissage supervisé basé sur la méthode 
    // de la descente de gradient (Règle de Widrow-Hoff ou règle du Delta).
    // Le but est d'ajuster les poids et le biais pour minimiser l'erreur de prédiction.
    // =========================================================================
    public void apprentissage(final float[][] entrees, final float[] resultats, final float MSElimite)
    {
        double mse = 0.; // Erreur quadratique moyenne (Mean Squared Error)
        int iter = 0;    // Compteur d'époques (itérations sur tout le jeu de données)
        
        do
        {
            mse = 0.; // Réinitialisation de l'erreur au début de chaque époque
            
            // Boucle sur l'intégralité du dataset d'entraînement
            for (int i = 0; i < entrees.length; ++i)
            {
                final float[] entree = entrees[i];
                
                // 1. PHASE DE PRÉDICTION (Feedforward)
                // Le neurone tente de deviner la sortie avec ses poids actuels
                metAJour(entree); 
                
                // 2. CALCUL DE L'ERREUR (Le Delta)
                // Écart entre la vérité terrain (résultat attendu) et la prédiction
                final float delta = resultats[i] - sortie(); 
                
                // On ajoute le carré de cette erreur pour le calcul de la MSE.
                // Le carré garantit une valeur positive et pénalise fortement les grosses erreurs.
                mse += delta * delta; 
                
                // 3. CORRECTION DES POIDS SYNAPTIQUES (Descente de gradient)
                // Si la prédiction est bonne (delta proche de 0), on modifie très peu.
                // Si elle est mauvaise, on ajuste le poids proportionnellement à l'entrée et à l'erreur.
                // Formule : Nouveau Poids = Ancien Poids + (Entrée * TauxApprentissage * Erreur)
                for (int j = 0; j < entree.length; ++j)
                    synapses()[j] += entree[j] * eta * delta;
                
                // 4. CORRECTION DU BIAIS
                // Le biais s'ajuste comme un poids synaptique dont l'entrée serait toujours de 1.
                // Formule : Nouveau Biais = Ancien Biais + (1 * TauxApprentissage * Erreur)
                fixeBiais(biais() + eta * delta);
            }
            
            // 5. CALCUL DE LA MOYENNE DES ERREURS
            // On divise la somme des erreurs quadratiques par le nombre d'images analysées
            mse /= entrees.length;
            
            System.out.printf("Itération %d, mse:  %.6f\n", iter, mse);
            iter += 1;
        }
        // 6. CONDITION D'ARRÊT
        // L'algorithme continue de boucler tant que l'erreur moyenne globale 
        // n'est pas passée en dessous du seuil de tolérance (MSElimite).
        while (mse > MSElimite);
    }

    public void sauvegarde(String chemin) // optionel
    {
        try
        {
            FileWriter writer = new FileWriter(chemin);
            for (float x : synapses)
            {
                writer.write(String.valueOf(x) + "\n");
            }
            writer.write(String.valueOf(biais) + "\n");
            writer.close();
            System.out.println("Sauvegarde réussie dans le fichier: " + chemin);
        }
        catch (IOException e)
        {
            System.out.println("Impossible de sauvegarder le neurone dans: " + chemin);
            e.printStackTrace();
        }
    }

    public void chargement(String chemin) // optionel
    {
        try(BufferedReader br = new BufferedReader(new FileReader(chemin)))
        {
            // On remplit chaque poids synaptique avec une valeur par ligne
            for (int i = 0; i < synapses.length; ++i)
            {
                synapses[i] = Float.valueOf(br.readLine());
            }
            // La dernière valeur lue sert de biais
            biais = Float.valueOf(br.readLine());
            System.out.println("Chargement réussi depuis le fichier: " + chemin);
        }
        catch (Exception e)
        {
            System.out.println("Impossible de charger le neurone depuis: " + chemin);
            e.printStackTrace();
        }
    }
}
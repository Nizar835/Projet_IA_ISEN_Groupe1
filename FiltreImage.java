public class FiltreImage 
{
	// Méthode qui prend les pixels d'une image et les inverse de gauche à droite
	public static float[] miroirGris(float[] pixels, int largeur, int hauteur) 
	{
		float[] pixelsMiroir = new float[pixels.length];
		
		for (int y = 0; y < hauteur; y++) {
			for (int x = 0; x < largeur; x++) {
				int indexOriginal = y * largeur + x;
				int indexInverse = y * largeur + (largeur - 1 - x);
				
				// On place le pixel original à sa nouvelle position inversée
				pixelsMiroir[indexInverse] = pixels[indexOriginal];
			}
		}
		return pixelsMiroir;
	}
}
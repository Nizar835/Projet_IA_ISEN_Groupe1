public class NeuroneReLU extends Neurone
{
	// Fonction d'activation ReLU (Rectified Linear Unit)
	// Renvoie 0 si la somme interne est négative, sinon renvoie la valeur brute
	protected float activation(final float valeur) 
	{
		return Math.max(0.0f, valeur);
	}
	
	// Constructeur 
	public NeuroneReLU(final int nbEntrees) 
	{
		super(nbEntrees);
	}
}
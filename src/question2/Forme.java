package question2;

public abstract class Forme extends GraphicalElement {
	
	public abstract double perimetre();
	
	public double aireLateralePrismeDroit(double h) {
		return perimetre() * h;
	}
}

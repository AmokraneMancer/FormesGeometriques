package question2;

public class Vector {
	private double[] coordonnes;
	
	public Vector(double[] coordonnes) {
		int size = coordonnes.length;
		this.coordonnes = new double[size];
		for(int i = 0 ; i < size ; i++) {
			this.coordonnes[i] = coordonnes[i]; 
		}
	}
	
	public double getX() {
		return this.coordonnes[0];
	}
	
	public double getY() {
		return this.coordonnes[1];
	}
	
	public Vector opposite() {
		return new Vector(new double[] {-1 * this.getX(), -1 * this.getY()});
	}
	
	public Vector getVectorForAnimation() {
		double max = this.getMax();	
		double x1 = this.getX() / max ;
		double y1 = this.getY() / max ;
		
		return new Vector(new double[] {x1, y1});
	}
	
	public double getMax() {
		return Math.max(Math.abs(this.getX()), Math.abs(this.getY()));
	}
	
}

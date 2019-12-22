package question2;

import interaction.Painter;

public class Polygon extends Forme {
	protected Point[] sommets;
	private Segment[] segments;
	
	public Polygon(Point[] sommets) {
		int size = sommets.length;
		this.sommets = new Point[size];
		for(int i = 0 ; i < size ; i++) {
			this.sommets[i] = sommets[i]; 
		}
		
		this.segments = new Segment[size];
		for(int i = 0; i < size ; i++) {
			if(i != size - 1)
				segments[i] = new Segment(this.sommets[i], this.sommets[i+1]);
			else
				segments[i] = new Segment(this.sommets[i], this.sommets[0]);
		}
	}
	
	protected Polygon(int nbrSommets) {
		this.sommets = new Point[nbrSommets];
	}
	
	public Point getSommet(int i) {
		if(this.sommets[i] != null)
			return sommets[i];
		return null;
	}
	
	public double perimetre() {
		int size = this.sommets.length;
		double perimetre = 0;
		int i = 0;
		for(i = 0 ; i < size ; i++) {
			
			if(i+1 == size)
				break;
			else
				perimetre += sommets[i].distanceA(sommets[i+1]);
		}
		perimetre += this.sommets[i].distanceA(this.sommets[0]);
		
		return perimetre;
	}
	
	public String toString() {
		String s = "[";
		int size = sommets.length;
		
		for(int i = 0; i < size ; i++) {
			s += "(" + this.sommets[i].getX() + "," + this.sommets[i].getY() + ")";
			if(i == size - 1)
				break;
			s += ",";
		}		
		s += "]";
		return s;
	}
	
	public void draw(Painter painter) {
		for(int i = 0; i < this.segments.length ; i++) {
			this.segments[i].draw(painter);
		}
	}
	
	public void translate(Vector vect) {
		for(int i = 0; i < this.sommets.length ; i++) {
			this.sommets[i].translate(vect);
		}
		
		// on reconstruit les segments pour un eventuel appel de draw()
		for(int i = 0; i < this.sommets.length ; i++) {
			if(i != this.sommets.length - 1)
				segments[i] = new Segment(this.sommets[i], this.sommets[i+1]);
			else
				segments[i] = new Segment(this.sommets[i], this.sommets[0]);
		}
	}
	
	public void rotate(double alpha) {
		for(int i = 0; i < this.sommets.length ; i++) {
			this.sommets[i].rotate(alpha);
			//this.segments[i].translate(vect);
		}
		
		// on reconstruit les segments pour un eventuel appel de draw()
		for(int i = 0; i < this.sommets.length ; i++) {
			if(i != this.sommets.length - 1)
				segments[i] = new Segment(this.sommets[i], this.sommets[i+1]);
			else
				segments[i] = new Segment(this.sommets[i], this.sommets[0]);
		}
	}
}

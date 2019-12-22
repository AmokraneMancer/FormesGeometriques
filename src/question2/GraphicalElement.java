package question2;

import interaction.Painter;

public abstract class GraphicalElement {
	
	public void draw(Painter painter) {
		
	}
	
	public abstract void translate(Vector vect);
	public abstract void rotate(double angle);
	
	public void rotate(Point centre, double angle) {
		Vector v = centre.getCoordinates();
		Vector vOppsoite = v.opposite();
		
		this.translate(vOppsoite);
		this.rotate(angle);
		this.translate(v);
	}
}

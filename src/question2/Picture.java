package question2;

import interaction.Painter;
public class Picture {

	private GraphicalElement[] elements;
	private Painter painter;
	
	public Picture(int height, int width, GraphicalElement[] elements) {
		this.painter = new Painter(width, height);
		this.elements = new GraphicalElement[elements.length];
		
		for(int i = 0 ; i < elements.length ; i++)
			this.elements[i] = elements[i];
	}
	
	public void display() {
		for(int i = 0 ; i < elements.length ; i++)
			this.elements[i].draw(painter);
	}
	
	public void translateElements(Vector vect) {	
		for(int i = 0 ; i < elements.length ; i++) 
			this.elements[i].translate(vect);	

		painter.clear();
	}
	
	public void rotateElements(double alpha) {
		for(int i = 0 ; i < elements.length ; i++) 
			this.elements[i].rotate(alpha);	
		painter.clear();
	}
	
	public void rotateElements(Point centre, double angle) {
		for(int i = 0 ; i < elements.length ; i++) 
			this.elements[i].rotate(centre, angle);	
		painter.clear();
	}
	
	
	public Painter getPainter() {
		return this.painter;
	}
}

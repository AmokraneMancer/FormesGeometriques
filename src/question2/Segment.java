package question2;

import interaction.Painter;

public class Segment extends GraphicalElement{
	private Point p1;
	private Point p2;
	
	public Segment(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public void draw(Painter painter) {
		double max = Math.max(Math.abs(this.p1.getX() - this.p2.getX()), Math.abs(this.p1.getY() - this.p2.getY()));
		double diffX = (p2.getX() - p1.getX()) / max;
		double diffY = (p2.getY() - p1.getY()) / max;
		
		double posX = this.p1.getX() + 0.5;
		double posY = this.p1.getY() + 0.5;
		
		for(int i = 0 ; i < (int) max ; i++) {
			painter.setPixel((int) posX, (int) posY, true);
			posX +=  diffX;
			posY +=  diffY;
		}		
	}
	
	public void translate(Vector vect) {
		this.p1.translate(vect);
		this.p2.translate(vect);
	}
	
	public void rotate (double angle) {
		this.p1.rotate(angle);
		this.p2.rotate(angle);
	}
}

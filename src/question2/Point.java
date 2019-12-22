package question2;

import interaction.Painter;

public class Point extends GraphicalElement {
	private double x;
	private double y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double distanceA(Point p) {
		return Math.sqrt(Math.pow(this.x - p.getX(), 2)  +  Math.pow(this.y - p.getY(), 2));
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	public void draw(Painter painter) {
		painter.setPixel((int) this.x, (int) this.y , true);
	}
	
	public void translate(Vector vect) {
		this.x += vect.getX();
		this.y += vect.getY();	
	}
	
	public void rotate(double angle) {
		double x1 = this.x;
		double y1 = this.y;
		this.x = x1*Math.cos(angle) - y1*Math.sin(angle);
		this.y = x1*Math.sin(angle) + y1*Math.cos(angle);
	}
	
	public Vector getCoordinates() {
		return new Vector(new double[] {this.x, this.y});
	}
	
}

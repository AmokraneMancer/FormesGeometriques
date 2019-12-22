package question2;

public class Rectangle extends Quadrilatere {
	
	public Rectangle(double xMin, double xMax, double yMin, double yMax) {
		super(new Point(xMin, yMin), new Point(xMin, yMax), new Point(xMax, yMax), new Point(xMax, yMin));
	}
}

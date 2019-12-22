package question2;

public class Triangle extends Polygon {
	
	public Triangle(Point a, Point b, Point c) {
		super(new Point[] {a, b ,c});
	}
	
	public boolean estTriangleRectangle() {
		Point a = this.sommets[0];
		Point b = this.sommets[1];
		Point c = this.sommets[2];
		// vérifier si le produit scalaire entre chaque 2 arretes vaut 0
		return (a.getX() - b.getX())*(a.getX() - c.getX()) + (a.getY() - b.getY())*(a.getY() - c.getY()) == 0
				|| (a.getX() - c.getX())*(b.getX() - c.getX()) + (a.getY() - b.getY())*(b.getY() - c.getY()) == 0
				|| (a.getX() - b.getX())*(b.getX() - c.getX()) + (a.getY() - b.getY())*(b.getY() - c.getY()) == 0;
	}

}

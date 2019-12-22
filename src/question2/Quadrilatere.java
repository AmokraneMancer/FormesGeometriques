package question2;

public class Quadrilatere extends Polygon {
	
	public Quadrilatere(Point a, Point b, Point c, Point d) {
		super(new Point[] {a, b, c, d});
	}
	
	public boolean estParallelogramme() {
		Point a = this.sommets[0];
		Point b = this.sommets[1];
		Point c = this.sommets[2];
		Point d = this.sommets[3];
		// vérifier si deux segments opposés sont égaux selon les ordres possibles de points
		return (a.distanceA(b) == d.distanceA(c) && a.distanceA(d) == b.distanceA(c)) ||
				(a.distanceA(d) == c.distanceA(b) && a.distanceA(c) == d.distanceA(b)) ||
				(a.distanceA(c) == b.distanceA(d) && a.distanceA(b) == c.distanceA(d));
	}
}

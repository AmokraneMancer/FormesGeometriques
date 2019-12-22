package question2;


import interaction.Painter;

public class Main {
	public static void main(String[] args) {
		
		Point p1 = new Point(50, 50);
		Point p2 = new Point(80, 70);
		Point p3 = new Point(90, 50);
		Point p4 = new Point(50, 30);
		Point p5 = new Point(90, 30);
		Point p6 = new Point(68, 30);	
		Point p7 = new Point(300,300);
		
		Segment s1 = new Segment(new Point(100,100), new Point(200,100)); // segment verticalde 100 pixels
		Segment s2 = new Segment(new Point(150, 125), new Point(180, 140)); // segment oblique, à peu près au centre d'une fenêtre 300*300
		Polygon p = new Polygon(new Point[] {p1, p2, p3, p5});
		Cercle c = new Cercle(p6, 30);
		Picture picture = new Picture(600, 600, new GraphicalElement[] {p,c,s1,s2});
		

		Vector v = new Vector(new double[] {100,250});
		
		picture.translateElements(v);
		
		/*for(int i = 0 ; i< (int)v.getMax() ; i++) {		
			Painter.delay(10);
			picture.translateElements(v.getVectorForAnimation());
			picture.display();
		}*/
		
		while(true) {
			Painter.delay(30);
			picture.rotateElements(p7, 0.02);
			picture.display();
			p7.draw(picture.getPainter());
		}
		
	}
}
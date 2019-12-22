package question2;

import interaction.Painter;

public class Cercle extends Forme {
	private Point centre;
	private double r;
	
	public Cercle(Point c, double r) {
		this.centre = c;
		this.r = r;
	}
	
	public double perimetre() {
		return 2 * Math.PI * this.r;
	}
	
	//Algorithme de dessin du cercle point médian
	public void draw(Painter painter) {
		int x = (int)this.r, y = 0; 
		double x_centre = this.centre.getX();
		double y_centre = this.centre.getY();
        // on dessine le premier point 
        // après translation
        painter.setPixel((int)(x + x_centre)  
                ,(int)(y + y_centre) , true); 
      
        // si r > 0, on affiche un seul point 
        if (r > 0){
            painter.setPixel((int)(x + x_centre), (int)(-y + y_centre) , true);                  
            painter.setPixel((int)(y + x_centre),(int) (x + y_centre) , true);                    
            painter.setPixel((int) (-y + x_centre),(int) (x + y_centre) ,true); 
        } 

        int P = 1 - (int)r; 
        while (x > y) { 
            y++; 
            // point médian est sur le périmètre ou dans le cercle
            if (P <= 0) 
                P = P + 2 * y + 1;
            // point médian est dehors le périmètre
            else { 
                x--; 
                P = P + 2 * y - 2 * x + 1; 
            } 
            // si tout les points sont affichés
            if (x < y) 
                break;
            // Impression du point généré et de sa réflexion 
            // dans les autres octants après translation
            painter.setPixel((int)(x + x_centre),(int)(y + y_centre) , true);                       
            painter.setPixel((int) (-x + x_centre),(int) (y + y_centre), true);                       
            painter.setPixel((int) (x + x_centre) ,(int) (-y + y_centre) , true);                       
            painter.setPixel((int) (-x + x_centre),(int)(-y + y_centre) , true); 
            // si le point affiché est sur la ligne
            // x = y alors les points du périmètre
            // sont tous affichés sinon..
            if (x != y) { 
            	painter.setPixel((int) (y + x_centre),(int) (x + y_centre) , true);  
            	painter.setPixel((int) (-y + x_centre),(int) (x + y_centre), true);        
            	painter.setPixel((int) (y + x_centre),(int) (-x + y_centre) , true);               
            	painter.setPixel((int) (-y + x_centre),(int)(-x + y_centre) , true); 
            } 
        } 
    }

	public void translate(Vector vect) {
		this.centre.translate(vect);
	}
	
	public void rotate(double angle) {
		this.centre.rotate(angle);
	}
}

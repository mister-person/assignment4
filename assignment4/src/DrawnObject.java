import java.awt.Color;
import java.awt.Graphics;

public interface DrawnObject {

	public abstract void addPoint(DrawnPoint point);
	
	public abstract boolean inObject(double x, double y);
	
	public abstract void draw(Graphics g);
	
	public abstract void drawOutline(Graphics g, Color c);
	
}

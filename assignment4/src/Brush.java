import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public interface Brush {

	public abstract void addPoint(DrawnPoint point);
	
	public abstract boolean inObject(double x, double y);
	
	public abstract Rectangle getBounds();
	
	public abstract void draw(Graphics g);
	
	public abstract void drawOutline(Graphics g, Color c);
	
	public abstract void transform(AffineTransform transform);
	
}

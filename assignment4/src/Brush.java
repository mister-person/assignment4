import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public interface Brush {

	public abstract void addPoint(DrawnPoint point);
	
	public abstract boolean containsPoint(double x, double y);
	
	public abstract Rectangle getBounds();
	
	public abstract void draw(Graphics2D g);
	
	public abstract void drawOutline(Graphics2D g);
	
	public abstract void transform(AffineTransform transform);
	
}

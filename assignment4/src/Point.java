import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Point extends Point2D.Double {
	
	public Point(double x, double y) {
		super(x, y);
	}
	
	public Point add(Point p) {
		return new Point(x + p.x, y + p.y);
	}
	
	public Point flip() {
		return new Point(-x, -y);
	}
	
	public Point transform(AffineTransform t) {
		
		return new Point(0, 0);
	}
	
	public Point scale(double scale) {
		return new Point(scale*x, scale*y);
	}
	
	public Point copy() {
		return new Point(x, y);
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}

}

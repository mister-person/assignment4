import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class FillAreaBrush implements Brush {

	private ArrayList<DrawnPoint> pointlist = new ArrayList<>();

	@Override
	public void addPoint(DrawnPoint point) {
		pointlist.add(point);
	}

	@Override
	public boolean inObject(double x, double y) {
		return getPolygon().contains(x, y);
	}

	@Override
	public Rectangle getBounds() {
		Rectangle bounds = new Rectangle();

		if(!pointlist.isEmpty()) {
			bounds.setRect(pointlist.get(0).x, pointlist.get(0).y, 0, 0);
			for(int i = 0; i < pointlist.size(); i++) {
				bounds.add(pointlist.get(i));
			}
		}

		return bounds;
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;

		if(pointlist.size() > 0) {
			g2.setColor(pointlist.get(0).getColor());
		}
		g2.fill(getPolygon());

	}

	@Override
	public void drawOutline(Graphics g, Color c) {
		Graphics2D g2 = (Graphics2D)g;

		g2.setColor(c);

		g2.draw(getPolygon());
	}

	@Override
	public void transform(AffineTransform transform) {
		ArrayList<DrawnPoint> newPointlist = new ArrayList<>();
		for(DrawnPoint p:pointlist) {
			DrawnPoint newPoint = p.copy();
			transform.transform(p, newPoint);
			newPointlist.add(newPoint);
		}

		pointlist = newPointlist;
	}

	private Shape getPolygon() {

		Path2D.Double path = new Path2D.Double(Path2D.WIND_NON_ZERO, pointlist.size());

		if(pointlist.size() > 0) {
			path.moveTo(pointlist.get(0).x, pointlist.get(0).y);
		}
		for(int i = 1; i < pointlist.size(); i++) {
			path.lineTo(pointlist.get(i).x, pointlist.get(i).y);
		}

		return path;
	}
}

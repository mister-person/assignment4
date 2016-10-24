import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CirclesLineBrush implements Brush {
	private ArrayList<DrawnPoint> pointlist = new ArrayList<>();
	
	@Override
	public void addPoint(DrawnPoint point) {
		pointlist.add(point);
	}

	@Override
	public boolean inObject(double x, double y) {
		for(DrawnPoint p:pointlist) {
			if(Point2D.distance(x, y, p.x, p.y) < p.getSize()/2) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Rectangle getBounds() {
		Rectangle bounds = new Rectangle();
		
		if(!pointlist.isEmpty()) {
			DrawnPoint point = pointlist.get(0);
			bounds.setRect(point.x, point.y, 0, 0);
			for(int i = 0; i < pointlist.size(); i++) {
				point = pointlist.get(i);
				bounds.add(new Rectangle2D.Double(point.x - point.getSize()/2, point.y - point.getSize()/2, point.getSize(), point.getSize()));
			}
		}
		
		
		return bounds;
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		for(DrawnPoint p:pointlist) {
			g2.setColor(p.getColor());
			g2.fillOval((int)(p.x - p.getSize()/2), (int)(p.y - p.getSize()/2), p.getSize(), p.getSize());
		}
	}

	@Override
	public void drawOutline(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		for(DrawnPoint p:pointlist) {
			g2.drawOval((int)(p.x - p.getSize()/2), (int)(p.y - p.getSize()/2), p.getSize(), p.getSize());
		}
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

}

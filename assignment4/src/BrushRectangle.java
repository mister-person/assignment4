import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public class BrushRectangle implements Brush {

	private DrawnPoint startingPoint;
	private DrawnPoint endingPoint;

	@Override
	public void addPoint(DrawnPoint point) {
		if(startingPoint == null) {
			startingPoint = point;
		}
		endingPoint = point;
	}

	@Override
	public boolean containsPoint(double x, double y) {
		return getRect().contains(x, y);
	}

	@Override
	public Rectangle getBounds() {
		return getRect();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(startingPoint.getColor());
		g.fill(getRect());
	}

	@Override
	public void drawOutline(Graphics2D g) {
		g.draw(getRect());
	}

	@Override
	public void transform(AffineTransform transform) {
		transform.transform(startingPoint, startingPoint);
		transform.transform(endingPoint, endingPoint);
	}
	
	private Rectangle getRect() {
		if(startingPoint != null) {
			Rectangle rect = new Rectangle((int)startingPoint.x, (int)startingPoint.y, 0, 0);
			rect.add(endingPoint);
			return rect;
		}
		return new Rectangle();
	}

}

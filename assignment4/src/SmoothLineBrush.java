import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class SmoothLineBrush implements Brush {

	private ArrayList<DrawnPoint> pointlist = new ArrayList<>();
	private Path2D.Double polygon;
	private boolean polygonUpdated = false;
	private int size;
	private int[] color;

	public SmoothLineBrush() {
	}

	@Override
	public void addPoint(DrawnPoint point) {
		pointlist.add(point);
		polygonUpdated = false;
	}

	@Override
	public void draw(Graphics g) {
		//apparently you can do this
		Graphics2D g2 = (Graphics2D)g;
		
		if(pointlist.size() > 0) {
			g2.setColor(pointlist.get(0).getColor());
		}
		g2.fill(getPolygon());
	}

	@Override
	public void drawOutline(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
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
		polygonUpdated = false;
	}

	@Override
	public boolean inObject(double x, double y) {
		return getPolygon().contains(x, y);
	}
	
	@Override
	public Rectangle getBounds() {
		return getPolygon().getBounds();
	}

	private Shape getPolygon() {
		if(polygonUpdated) {
			return polygon;
		}
		ArrayList<DrawnPoint> cpointlist = cullAdjacentPoints();
		
		Path2D.Double path = new Path2D.Double(Path2D.WIND_NON_ZERO, cpointlist.size()*4);

		if(cpointlist.size() >= 2) {
			Point[] points = new Point[cpointlist.size()*4 - 4];
			DrawnPoint point, nextPoint; 
			DrawnPoint prevPoint = null;

			for(int i = 0; i < cpointlist.size(); i++) {
				point = cpointlist.get(i);

				if(i + 1 < cpointlist.size()) {
					nextPoint = cpointlist.get(i + 1);

					//if(point.getSize() != 1 || nextPoint.getSize() != 1) {

					Point normVector = getNormVector(point, nextPoint);

					//rotate normalized vector by 90 degrees, multiply it by size, and add it to polygon
					points[i*2 + 0] = new Point((normVector.y*point.getSize()*.48) + point.intxPos(), -(normVector.x*point.getSize()*.48) + point.intyPos());

					//same as above but going 90 degrees the other direction and on the other end of the polygon
					points[points.length - i*2 - 1] = new Point(-(normVector.y*point.getSize()*.48) + point.intxPos(), (normVector.x*point.getSize()*.48) + point.intyPos());

					//now same thing for points at nextPoint
					points[points.length - i*2 - 2] = new Point(-(normVector.y*nextPoint.getSize()*.48) + nextPoint.intxPos(), (normVector.x*nextPoint.getSize()*.48) + nextPoint.intyPos());

					points[i*2 + 1] = new Point((normVector.y*nextPoint.getSize()*.48) + nextPoint.intxPos(), -(normVector.x*nextPoint.getSize()*.48) + nextPoint.intyPos());
					
					prevPoint = point;
				}
			}

			double size = cpointlist.get(cpointlist.size() - 1).getSize() * .6;
			
			path.moveTo(points[0].x, points[0].y);
			
			int i;
			//first half of points
			for(i = 1; i < points.length/2; i++) {
				if(i%2 == 0) {
					//path.lineTo(curvePoints[i/2].x + pointlist.get(i/2).x + 20, curvePoints[i/2].y + pointlist.get(i/2).y + 20);
					approxCircle(path, cpointlist.get(i/2), cpointlist.get(i/2-1), cpointlist.get(i/2+1), points[i - 1], points[i]);
				}
				path.lineTo(points[i].x, points[i].y);
			}
			
			//curve around at the end, approximate a circle with Bezier curve
			Point normVector = getNormVector(cpointlist.get(cpointlist.size() - 2), cpointlist.get(cpointlist.size() - 1));
			path.curveTo(normVector.x*size + points[i - 1].x, normVector.y*size + points[i - 1].y, normVector.x*size + points[i].x, normVector.y*size + points[i].y, points[i].x, points[i].y);

			//second half of points
			for(; i < points.length; i++) {
				if(i%2 == 0 && i >= cpointlist.size()*2) {
					int index = (cpointlist.size() - (i/2 - cpointlist.size())) - 2;
					approxCircle(path, cpointlist.get(index), cpointlist.get(index+1), cpointlist.get(index-1), points[i - 1], points[i]);
				}
				path.lineTo(points[i].x, points[i].y);
			}
			
			//curve around at the beginning
			normVector = getNormVector(cpointlist.get(1), cpointlist.get(0));
			path.curveTo(normVector.x*size + points[i - 1].x, normVector.y*size + points[i - 1].y, normVector.x*size + points[0].x, normVector.y*size + points[0].y, points[0].x, points[0].y);
			
		}
		else if(pointlist.size() > 0){
			DrawnPoint point = pointlist.get(0);
			return new Ellipse2D.Double(point.x-point.getSize()/2, point.y - point.getSize()/2, point.getSize(), point.getSize());
		}

		polygonUpdated = true;
		polygon = path;
		return path;
	}
	
	//http://stackoverflow.com/questions/1734745/how-to-create-circle-with-bézier-curves
	private void approxCircle(Path2D path, Point center, Point lastPoint, Point nextPoint, Point curveFrom, Point curveTo) {
		Point curvePoint = getNormVector(center, nextPoint).add(getNormVector(center, lastPoint));
		if(!(curvePoint.x == 0 && curvePoint.y == 0)) {
			Point fromDelta = curveFrom.add(center.flip());
			//Point toDelta = curveTo.add(center.flip());
			
			//check if we're on the outside of a curve
			if(angleBetweenVectors(fromDelta, curvePoint) >= Math.PI/2 - .01) {
				//curvePoints[i] = getNormVector(new Point(0, 0), curvePoint);
				doApproxCircle(path, center, curveFrom, curveTo);
				//path.lineTo(bezier1.x, bezier1.y);
				//path.lineTo(bezier2.x, bezier2.y);
			}
		}
	}
	
	private void doApproxCircle(Path2D path, Point center, Point curveFrom, Point curveTo) {
		Point fromDelta = curveFrom.add(center.flip());
		Point toDelta = curveTo.add(center.flip());
		
		Point fromTangent = new Point(-fromDelta.y, fromDelta.x);
		
		Point toTangent = new Point(toDelta.y, -toDelta.x);
		
		double scale = Math.tan(angleBetweenVectors(fromDelta, toDelta)/4)*4/3;

		Point bezier1 = curveFrom.add(fromTangent.scale(scale));
		Point bezier2 = curveTo.add(toTangent.scale(scale));
		
		path.curveTo(bezier1.x, bezier1.y, bezier2.x, bezier2.y, curveTo.x, curveTo.y);
	}
	
	private ArrayList<DrawnPoint> cullAdjacentPoints() {
		ArrayList<DrawnPoint> newPointlist = new ArrayList<DrawnPoint>();
		Point lastPoint = pointlist.get(0);
		for(int i = 1; i < pointlist.size(); i++) {
			if(!(Math.abs(lastPoint.x - pointlist.get(i).x) <= 1.1 && Math.abs(lastPoint.y - pointlist.get(i).y) <= 1.1)) {
				newPointlist.add(pointlist.get(i - 1));
				lastPoint = pointlist.get(i);
			}
			else if(i == 1){
				newPointlist.add(pointlist.get(i - 1));
			}
		}
		newPointlist.add(pointlist.get(pointlist.size() - 1));
		//newPointlist.add(pointlist.get(pointlist.size() - 1));
		return newPointlist;
	}
	
	private Point getNormVector(Point p1, Point p2) {
		double diffX = p2.x - p1.x;
		double diffY = p2.y - p1.y;

		//normalize vector by dividing them by the distance
		double dist = Math.sqrt(diffX * diffX + diffY * diffY);
		return new Point(diffX/dist, diffY/dist);
	}
	
	private double angleBetweenVectors(Point p1, Point p2) {
		p1 = getNormVector(new Point(0, 0), p1);
		p2 = getNormVector(new Point(0, 0), p2);
		return Math.acos(p1.x*p2.x + p1.y*p2.y);
	}

}

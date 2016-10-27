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

	public SmoothLineBrush() {
	}

	@Override
	public void addPoint(DrawnPoint point) {
		pointlist.add(point);
		polygonUpdated = false;
	}

	@Override
	public void draw(Graphics2D g) {
		if(pointlist.size() > 0) {
			g.setColor(pointlist.get(0).getColor());
		}
		g.fill(getPolygon());
	}

	@Override
	public void drawOutline(Graphics2D g) {
		g.draw(getPolygon());
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
	public boolean containsPoint(double x, double y) {
		return getPolygon().contains(x, y);
	}
	
	@Override
	public Rectangle getBounds() {
		return getPolygon().getBounds();
	}

	private Shape getPolygon() {
		//don't recompute polygon if it hasn't been changed.
		if(polygonUpdated) {
			return polygon;
		}
		//get rid of points that are right next to each other because it messes up the algorithm
		ArrayList<DrawnPoint> cpointlist = cullAdjacentPoints();
		
		//WIND_NON_ZERO so there aren't tons of holes in the polygon
		Path2D.Double path = new Path2D.Double(Path2D.WIND_NON_ZERO, cpointlist.size()*4);

		//there has to be at least 2 points to start drawing a line through them
		if(cpointlist.size() >= 2) {
			Point[] points = new Point[cpointlist.size()*4 - 4];
			DrawnPoint point, nextPoint; 

			//this loop goes through each pair of adjacent points and computes the corners rectangle of width point.getSize to connect them,
			//and adds those points to an array, with one side of the rectangle at the beginning of the array and the other at the end so the
			//array makes the vertices of a polygon that connects every point smoothly.
			for(int i = 0; i < cpointlist.size(); i++) {
				point = cpointlist.get(i);

				if(i + 1 < cpointlist.size()) {
					nextPoint = cpointlist.get(i + 1);

					//get direction from point to nextPoint
					Point normVector = getNormVector(point, nextPoint);

					//rotate normalized vector by 90 degrees, multiply it by size, and add it to polygon
					points[i*2 + 0] = new Point((normVector.y*point.getSize()*.48) + point.x, -(normVector.x*point.getSize()*.48) + point.y);

					//same as above but going 90 degrees the other direction and on the other end of the polygon
					points[points.length - i*2 - 1] = new Point(-(normVector.y*point.getSize()*.48) + point.x, (normVector.x*point.getSize()*.48) + point.y);

					//now same thing for points at nextPoint
					points[points.length - i*2 - 2] = new Point(-(normVector.y*nextPoint.getSize()*.48) + nextPoint.x, (normVector.x*nextPoint.getSize()*.48) + nextPoint.y);

					points[i*2 + 1] = new Point((normVector.y*nextPoint.getSize()*.48) + nextPoint.x, -(normVector.x*nextPoint.getSize()*.48) + nextPoint.y);
				}
			}

			//now add all the points from the array to a path object
			path.moveTo(points[0].x, points[0].y);
			
			int i;
			//first half of points
			for(i = 1; i < points.length/2; i++) {
				if(i%2 == 0) {
					//the rectangles have gaps in them, so this fills the gaps with curves instead of lines
					approxCircle(path, cpointlist.get(i/2), cpointlist.get(i/2-1), cpointlist.get(i/2+1), points[i - 1], points[i]);
				}
				path.lineTo(points[i].x, points[i].y);
			}
			
			//curve around at the end, approximate a circle with Bezier curve
			double size = cpointlist.get(cpointlist.size() - 1).getSize() * .6;
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
			size = cpointlist.get(0).getSize() * .6;
			normVector = getNormVector(cpointlist.get(1), cpointlist.get(0));
			path.curveTo(normVector.x*size + points[i - 1].x, normVector.y*size + points[i - 1].y, normVector.x*size + points[0].x, normVector.y*size + points[0].y, points[0].x, points[0].y);
			
		}
		else if(pointlist.size() > 0){
			//if there's only one point just make a circle
			DrawnPoint point = pointlist.get(0);
			return new Ellipse2D.Double(point.x-point.getSize()/2, point.y - point.getSize()/2, point.getSize(), point.getSize());
		}

		polygonUpdated = true;
		polygon = path;
		return path;
	}
	
	private void approxCircle(Path2D path, Point center, Point lastPoint, Point nextPoint, Point curveFrom, Point curveTo) {
		Point curvePoint = getNormVector(center, nextPoint).add(getNormVector(center, lastPoint));
		if(!(curvePoint.x == 0 && curvePoint.y == 0)) {
			Point fromDelta = curveFrom.add(center.flip());
			
			//check if we're on the outside of a curve, where a gap would be, only draw curve if there would be a gap
			if(angleBetweenVectors(fromDelta, curvePoint) >= Math.PI/2 - .01) {
				doApproxCircle(path, center, curveFrom, curveTo);
			}
		}
	}
	
	private void doApproxCircle(Path2D path, Point center, Point curveFrom, Point curveTo) {
		//complicated vector stuff to approximate a circle with bezier curves
		//http://stackoverflow.com/questions/1734745/how-to-create-circle-with-bézier-curves
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

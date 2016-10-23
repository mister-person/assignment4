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

	//detect where two lines intersect http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
	//could probably be optimized
	public static double[] intersect(double px, double py, double px2, double py2, double qx, double qy, double qx2, double qy2) {
		double rx = px2 - px;
		double ry = py2 - py;
		double sx = qx2 - qx;
		double sy = qy2 - qy;
		double c = rx*sy - ry*sx;
		//t = (q − p) × s / (r × s)
		//double t = cross(qx - px, qy - py, sx, sy)/c;
		double t = ((qx - px)*sy - (qy - py)*sx)/c;
		//u = (q − p) × r / (r × s)
		//double u = cross(qx - px, qy - py, rx, ry)/c;
		double u = ((qx - px)*ry - (qy - py)*rx)/c;
		if(c == 0) {
			if(((qx - px)*sy - (qy - py)*sx) == 0) {
				//lines are colinear
				//System.out.println("colinear");
				if((px < qx && px2 > qx)) {
					System.out.println("1 " + c);
					return new double[]{qx, qy, 1};
				}
				if((px < qx2 && px2 > qx2)) {
					System.out.println("2 " + c);
					return new double[]{qx2, qy2, 1};
				}
				if((px > qx && px2 < qx)) {
					System.out.println("3 " + c);
					return new double[]{qx, qy, -1};
				}
				if((px > qx2 && px2 < qx2)) {
					System.out.println("4 " + c);
					return new double[]{qx2, qy2, -1};
				}
			}
			else {
				return null;
			}
		}
		if(t >= 0 && t <= 1 && u >= 0 && u <= 1 && c > 0) {
			if(c > 0) {
				return new double[]{px + t*rx, py + t*ry, 1};
			}

			else {
				return new double[]{px + t*rx, py + t*ry, -1};
			}
		}
		return null;

	}

}

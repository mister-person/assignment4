import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainFrame extends JFrame {

	private JPanel panel;
	private ArrayList<ArrayList<Point>> pointLists = new ArrayList<>();
	private double thicknessMultiplier = 1;
	private final double angleThicknessMultiplier = .1;
	private int size = 2;

	public MainFrame() {
		super("window");
		setLayout(new FlowLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setSize(600, 600);


		JSlider slider = new JSlider();
		add(slider);
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				size = ((JSlider)e.getSource()).getValue()/7;

				if(panel != null) {
					panel.repaint();
				}
			}
		});

		JButton clearButton = new JButton("Clear");
		add(clearButton);
		clearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				pointLists = new ArrayList<>();
				if(panel != null) {
					panel.repaint();
				}
			}
		});

		panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				for(ArrayList<Point> pointList:pointLists) {
					int[] xPoints = null;
					int[] yPoints = null;
					xPoints = new int[pointList.size()*4 - 4];
					yPoints = new int[pointList.size()*4 - 4];
					boolean first = true;
					for(int i = 0; i < pointList.size(); i++) {
						Point point = pointList.get(i);

						if(i + 1 < pointList.size()) {
							Point nextPoint = pointList.get(i + 1);

							//if(point.getSize() != 1 || nextPoint.getSize() != 1) {

							int diffX = nextPoint.getxPos() - point.getxPos();
							int diffY = nextPoint.getyPos() - point.getyPos();

							//normalize vector by dividing them by the distance
							double normVectorX = diffX/Math.sqrt(diffX * diffX + diffY * diffY);
							double normVectorY = diffY/Math.sqrt(diffX * diffX + diffY * diffY);

							//rotate normalized vector by 90 degrees, multiply it by size, and add it to polygon
							xPoints[i*2 + 0] = (int)(normVectorY*point.getSize()*.95) + point.getxPos();
							yPoints[i*2 + 0] = -(int)(normVectorX*point.getSize()*.95) + point.getyPos();

							//same as above but going 90 degrees the other direction and going to the other end of the polygon
							xPoints[xPoints.length - i*2 - 1] = -(int)(normVectorY*point.getSize()*.95) + point.getxPos();
							yPoints[yPoints.length - i*2 - 1] = (int)(normVectorX*point.getSize()*.95) + point.getyPos();


							//now same thing for points at nextPoint
							xPoints[xPoints.length - i*2 - 2] = -(int)(normVectorY*nextPoint.getSize()*.95) + nextPoint.getxPos();
							yPoints[xPoints.length - i*2 - 2] = (int)(normVectorX*nextPoint.getSize()*.95) + nextPoint.getyPos();

							xPoints[i*2 + 1] = (int)(normVectorY*nextPoint.getSize()*.95) + nextPoint.getxPos();
							yPoints[i*2 + 1] = -(int)(normVectorX*nextPoint.getSize()*.95) + nextPoint.getyPos();
							//}
						}
						/*
						Point point = pointList.get(i);
						g.setColor(new Color(point.getColor()[0], point.getColor()[1], point.getColor()[2], point.getColor()[3]));


						if(point.getSize() != 1 && point.getColor()[3] == 255) {
							g.fillOval(point.getxPos() - point.getSize(), point.getyPos() - point.getSize(), point.getSize()*2 - 1, point.getSize()*2 - 1);
						}

						//draw line from point to next point except for last point.
						if(i + 1 < pointList.size()) {
							Point nextPoint = pointList.get(i + 1);

							if(point.getSize() != 1 || nextPoint.getSize() != 1) {

								int diffX = nextPoint.getxPos() - point.getxPos();
								int diffY = nextPoint.getyPos() - point.getyPos();

								//normalize vector by dividing them by the distance
								double normVectorX = diffX/Math.sqrt(diffX * diffX + diffY * diffY);
								double normVectorY = diffY/Math.sqrt(diffX * diffX + diffY * diffY);

								//if this is the first point generate the corners, otherwise just use corners from last point.

								//rotate normalized vector by 90 degrees, multiply it by size, and add it to point to get corner of rectangle.
								xPoints[0] = (int)(normVectorY*point.getSize()*.95) + point.getxPos();
								yPoints[0] = -(int)(normVectorX*point.getSize()*.95) + point.getyPos();

								//same as above but going 90 degrees the other direction.
								xPoints[1] = -(int)(normVectorY*point.getSize()*.95) + point.getxPos();
								yPoints[1] = (int)(normVectorX*point.getSize()*.95) + point.getyPos();

								if(!first) {
									//g.setColor(new Color(point.getColor()[0] + 30, point.getColor()[1], point.getColor()[2]));
									g.fillPolygon(xPoints, yPoints, 4);
								}
								else {
									first = false;
								}
								/*}
								else {
									xPoints[0] = xPoints[3];
									yPoints[0] = yPoints[3];

									xPoints[1] = xPoints[2];
									yPoints[1] = yPoints[2];
								}/

								//now same thing for corners of rectangle at nextPoint
								xPoints[2] = -(int)(normVectorY*nextPoint.getSize()*.95) + nextPoint.getxPos();
								yPoints[2] = (int)(normVectorX*nextPoint.getSize()*.95) + nextPoint.getyPos();

								xPoints[3] = (int)(normVectorY*nextPoint.getSize()*.95) + nextPoint.getxPos();
								yPoints[3] = -(int)(normVectorX*nextPoint.getSize()*.95) + nextPoint.getyPos();

								//g.setColor(new Color(point.getColor()[0], point.getColor()[1], point.getColor()[2]));
								g.fillPolygon(xPoints, yPoints, 4);
							}
							else {
								g.drawLine(point.getxPos(), point.getyPos(), nextPoint.getxPos(), nextPoint.getyPos());
							}
						}*/
					}


					//Polygon poly = new Polygon(xPoints, yPoints, xPoints.length);

					ArrayList<Integer> xCon = new ArrayList<Integer>();
					ArrayList<Integer> yCon = new ArrayList<Integer>();

					int direction = 1;
					for(int i = 0; i < xPoints.length - 1 && i >= 0; i += direction) {
						if(xPoints[i] == xPoints[i+1] && yPoints[i] == yPoints[i+1]) {
							continue;
						}
						g.setColor(new Color(255, 0, 0));
						g.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
						for(int j = 0; j < i - 2; j++) {
							if(xPoints[j] == xPoints[j+1] && yPoints[j] == yPoints[j+1]) {
								continue;
							}
							double[] crossing = intersect(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1], xPoints[j], yPoints[j], xPoints[j+1], yPoints[j+1]);
							//System.out.println(xPoints[j] + " " + yPoints[j] + " " +  xPoints[j + 1] + " " + yPoints[j + 1]);
							if(crossing != null) {
								{
									if(crossing[2] == 1) {
										direction = -direction;
										i = j;
										xCon.add((int)crossing[0]);
										yCon.add((int)crossing[1]);
										break;
									}
									else if(crossing[2] == -1) {
										direction = -direction;
										i = j;
										xCon.add((int)crossing[0]);
										yCon.add((int)crossing[1]);
										break;
									}
								}
								
								//g.drawLine((int)crossing[0] - 5, (int)crossing[1] - 5, xPoints[i], yPoints[i]);
								//g.drawLine((int)crossing[0] - 5, (int)crossing[1] - 5, xPoints[j], yPoints[j]);
								g.setColor(new Color(255, 0, 0));
								g.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
								g.setColor(new Color(0, 255, 0));
								g.drawLine(xPoints[j], yPoints[j], xPoints[j+1], yPoints[j+1]);
								g.setColor(new Color(0, 0, 255));
								g.fillOval((int)crossing[0] - 5, (int)crossing[1] - 5, 10, 10);
							}
						}
						if(true) {
							xCon.add(xPoints[i]);
							yCon.add(yPoints[i]);
						}
					}

					int[] finalXPoints = new int[xCon.size()];
					int[] finalYPoints = new int[yCon.size()];
					for(int i = 0; i < xCon.size(); i++) {
						finalXPoints[i] = xCon.get(i);
						finalYPoints[i] = yCon.get(i);
					}

					//g.setXORMode(new Color(0, 0, 0, 255)); 
					g.setColor(pointList.get(0).getColor());
					g.fillPolygon(new Polygon(finalXPoints, finalYPoints,  /*
							xPoints.length));
						//*/xCon.size()));
					g.setColor(new Color(0, 0, 255, 100));
					g.drawPolygon(xPoints, yPoints, xPoints.length);
				}
			}
		};
		panel.setLayout(new FlowLayout());
		CanvasMouseListener listener = new CanvasMouseListener();
		panel.addMouseListener(listener);
		panel.addMouseMotionListener(listener);
		add(panel);
		panel.setVisible(true);
		panel.setBackground(new Color(1f, 1, 1));
		panel.setPreferredSize(new Dimension(500, 500));
		//System.out.println(Arrays.toString(intersect(0, 0, 1, 2, 1, 0, 0, 1)));

	}

	//detect where two lines intersect http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
	//could probably be optimized
	@SuppressWarnings("unused")
	private double[] intersect(double px, double py, double px2, double py2, double qx, double qy, double qx2, double qy2) {
		double rx = px2 - px;
		double ry = py2 - py;
		double sx = qx2 - qx;
		double sy = qy2 - qy;
		double c = cross(rx, ry, sx, sy);
		if(c == 0) {
			if(cross(qx - px, qy - py, sx, sy) == 0) {
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
		//t = (q − p) × s / (r × s)
		double t = cross(qx - px, qy - py, sx, sy)/c;
		//u = (q − p) × r / (r × s)
		double u = cross(qx - px, qy - py, rx, ry)/c;
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

	private double cross(double vx, double vy, double wx, double wy) {
		return (vx*wy) - vy*wx;
	}

	private class CanvasMouseListener implements MouseListener, MouseMotionListener{
		private int lastMouseX;
		private int lastMouseY;
		private int[] color = new int[]{50, 50, 70, 100};

		@Override
		public void mousePressed(MouseEvent e) {
			//mouseDragged(e);
			//if(true)return;
			pointLists.add(new ArrayList<>());
			pointLists.get(pointLists.size() - 1).add(new Point(e.getX(), e.getY(), size, color));
			panel.updateUI();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(pointLists.isEmpty()) {
				pointLists.add(new ArrayList<>());
			}
			if(size == 8) {
				size = 15;
			}
			else if(size == 15){
				size = 8;
			}
			//size = (int) (Math.sqrt(Math.pow((e.getX() - lastMouseX), 2) + Math.pow((e.getY() - lastMouseY), 2))/10) + 2;
			pointLists.get(pointLists.size() - 1).add(new Point(e.getX(), e.getY(), size, color));
			panel.updateUI();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

	}

}

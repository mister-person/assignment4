import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

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
				System.out.println(size);

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
					xPoints = new int[4];
					yPoints = new int[4];
					boolean first = true;
					for(int i = 0; i < pointList.size(); i++) {
						Point point = pointList.get(i);
						g.setColor(new Color(point.getColor()[0], point.getColor()[1], point.getColor()[2]));

						if(point.getSize() != 1) {
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
								}*/

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
						}
					}
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
		
		
	}

	private class CanvasMouseListener implements MouseListener, MouseMotionListener{
		private int lastMouseX;
		private int lastMouseY;
		
		@Override
		public void mousePressed(MouseEvent e) {
			pointLists.add(new ArrayList<>());
			pointLists.get(pointLists.size() - 1).add(new Point(e.getX(), e.getY(), size));
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
			pointLists.get(pointLists.size() - 1).add(new Point(e.getX(), e.getY(), size));
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

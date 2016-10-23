import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private JPanel canvasPanel;
	//private ArrayList<DrawnObject> pointLists = new ArrayList<>();
	private ArrayList<Layer> layers = new ArrayList<>();
	private LayerListModel listModel = new LayerListModel();
	private JList<String> list;
	private int currentLayer = 0;
	
	private DrawnObject currentDrawnObject;
	private int size = 20;
	private int mouseMode = 0;
	private int brush = 0;
	private int selectedObject = -1;
	private int[] color = new int[]{0, 0, 0};
	private int[] customColor = new int[]{0, 0, 0};
	private int alpha = 255;

	private static final int MOUSE_DRAW = 0;
	private static final int MOUSE_SELECT = 1;

	public MainFrame() {
		super("window");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		JPanel rightPanel = new JPanel();
		add(rightPanel, BorderLayout.EAST);
		rightPanel.setPreferredSize(new Dimension(200, 500));
		
		layers.add(new Layer());
		
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				currentLayer = list.getSelectedIndex();
				if(canvasPanel != null) {
					canvasPanel.repaint();
				}
				selectedObject = -1;
			}
		});
		list.setSelectedIndex(0);
		
		JScrollPane layerPane = new JScrollPane(list);
		rightPanel.add(layerPane);
		layerPane.setPreferredSize(new Dimension(200, 300));

		JButton addLayer = new JButton("Add Layer");
		rightPanel.add(addLayer);
		addLayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.addElement();
				//currentLayer = layers.size() - 1;
				list.repaint();
				canvasPanel.repaint();
			}
		});

		JButton removeLayer = new JButton("Remove Layer");
		rightPanel.add(removeLayer);
		removeLayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(layers.size() > 1) {
					listModel.removeElement();
				}
				list.repaint();
				canvasPanel.repaint();
			}
		});

		JButton moveLayerUp = new JButton("Move Layer Up");
		rightPanel.add(moveLayerUp);
		moveLayerUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.moveElementUp();
				list.repaint();
				canvasPanel.repaint();
			}
		});

		JButton moveLayerDown = new JButton("Move Layer Down");
		rightPanel.add(moveLayerDown);
		moveLayerDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.moveElementDown();
				list.repaint();
				canvasPanel.repaint();
			}
		});

		
		
		JPanel leftPanel = new JPanel();
		add(leftPanel, BorderLayout.WEST);
		leftPanel.setPreferredSize(new Dimension(200, 500));
		
		JButton clearButton = new JButton("Clear");
		leftPanel.add(clearButton);
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.clear();
				if(canvasPanel != null) {
					canvasPanel.repaint();
				}
			}
		});

		JButton mouseModeButton = new JButton("Draw");
		leftPanel.add(mouseModeButton);
		mouseModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mouseMode = (mouseMode + 1)%2;
				if(mouseMode == MOUSE_DRAW) {
					mouseModeButton.setText("Draw");
				}
				else if(mouseMode == MOUSE_SELECT) {
					mouseModeButton.setText("Select");
				}
				selectedObject = -1;
			}
		});
		
		JComboBox<String> brushes = new JComboBox<>(new String[]{"Circles", "Smooth Line", "Fill Area"});
		leftPanel.add(brushes);
		brushes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				brush = ((JComboBox<?>)e.getSource()).getSelectedIndex();
			}
		});

		leftPanel.add(new JLabel("Transparency:"));
		JSlider alphaSlider = new JSlider(0, 255, 0);
		leftPanel.add(alphaSlider);
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				alpha = 255-((JSlider)e.getSource()).getValue();
			}
		});
		
		JButton redButton = new JButton("Red");
		leftPanel.add(redButton);
		redButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{255, 0, 0};}
		});
		redButton.setForeground(new Color(255, 0, 0));

		JButton greenButton = new JButton("Green");
		leftPanel.add(greenButton);
		greenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{0, 255, 0};}
		});
		greenButton.setForeground(new Color(0, 255, 0));

		JButton blueButton = new JButton("Blue");
		leftPanel.add(blueButton);
		blueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{0, 0, 255};}
		});
		blueButton.setForeground(new Color(0, 0, 255));

		JButton blackButton = new JButton("Black");
		leftPanel.add(blackButton);
		blackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{0, 0, 0};}
		});
		blackButton.setForeground(new Color(0, 0, 0));

		JButton whiteButton = new JButton("White");
		leftPanel.add(whiteButton);
		whiteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{255, 255, 255};}
		});
		whiteButton.setForeground(new Color(180, 180, 180));
		
		JButton customButton = new JButton("Custom");
		leftPanel.add(customButton);
		customButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = customColor;}
		});
		customButton.setForeground(new Color(0, 0, 0));

		leftPanel.add(new JLabel("Custom Red:"));
		JSlider customRed = new JSlider(0, 255, 0);
		leftPanel.add(customRed);
		customRed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[0] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		leftPanel.add(new JLabel("Custom Green:"));
		JSlider customGreen = new JSlider(0, 255, 0);
		leftPanel.add(customGreen);
		customGreen.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[1] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		leftPanel.add(new JLabel("Custom Blue:"));
		JSlider customBlue = new JSlider(0, 255, 0);
		leftPanel.add(customBlue);
		customBlue.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[2] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});

		canvasPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				for(int i = layers.size() - 1; i >= 0; i--) {
					Layer layer = layers.get(i);
					if(layer.visible) {
						for(int j = 0; j < layer.pointlist.size(); j++) {
							layer.pointlist.get(j).draw(g);
							if(currentLayer == i && selectedObject == j) {
								layer.pointlist.get(j).drawOutline(g, new Color(255, 0, 0));
							}
						}
					}
				}
			}
		};
		canvasPanel.setLayout(new FlowLayout());
		CanvasMouseListener listener = new CanvasMouseListener();
		canvasPanel.addMouseListener(listener);
		canvasPanel.addMouseMotionListener(listener);
		add(canvasPanel, BorderLayout.CENTER);
		canvasPanel.setBackground(new Color(1f, 1, 1));
		canvasPanel.setVisible(true);
		canvasPanel.setPreferredSize(new Dimension(500, 500));
		//System.out.println(Arrays.toString(intersect(0, 0, 1, 2, 1, 0, 0, 1)));
		canvasPanel.repaint();

		setSize(900, 600);
		setVisible(true);
	}

	private class CanvasMouseListener extends MouseAdapter{
		private int lastMouseX;
		private int lastMouseY;
		
		private void newDrawnObject() {
			if(brush == 0) {
				currentDrawnObject = new CirclesLine();
			}
			else if(brush == 1) {
				currentDrawnObject = new SmoothLine();
			}
			else if(brush == 2) {
				currentDrawnObject = new FillArea();
			}
			layers.get(currentLayer()).pointlist.add(currentDrawnObject);
			System.out.println(currentLayer() + " " + layers);
		}
		
		private void selectObject(double x, double y) {
			for(int i = layers.get(currentLayer()).pointlist.size() - 1; i >= 0; i--) {
				selectedObject = -1;
				if(layers.get(currentLayer()).pointlist.get(i).inObject(x, y)) {
					selectedObject = i;
					break;
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			//mouseDragged(e);
			//if(true)return;
			if(mouseMode == MOUSE_DRAW) {
				newDrawnObject();
				currentDrawnObject.addPoint(new DrawnPoint(e.getX(), e.getY(), size, getColor()));
			}
			else if(mouseMode == MOUSE_SELECT) {
				selectObject(e.getX(), e.getY());
			}
			canvasPanel.updateUI();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(mouseMode == MOUSE_SELECT) {
				selectObject(e.getX(), e.getY());
			}
			canvasPanel.updateUI();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(mouseMode == MOUSE_DRAW) {
				if(layers.get(currentLayer()).pointlist.isEmpty()) {
					newDrawnObject();
				}
				if(size == 8) {
					size = 15;
				}
				else if(size == 15){
					size = 8;
				}
				//size = (int) (Math.sqrt(Math.pow((e.getX() - lastMouseX), 2) + Math.pow((e.getY() - lastMouseY), 2))/10) + 2;
				currentDrawnObject.addPoint(new DrawnPoint(e.getX(), e.getY(), size, getColor()));
			}
			else if(mouseMode == MOUSE_SELECT) {
				if(selectedObject != -1) {
					if(layers.get(currentLayer()).pointlist.get(selectedObject).inObject(lastMouseX, lastMouseY)) {
						layers.get(currentLayer()).pointlist.get(selectedObject).transform(AffineTransform.getTranslateInstance(e.getX() - lastMouseX, e.getY() - lastMouseY));
					}
				}
			}
			canvasPanel.updateUI();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

	}
	
	private Color getColor() {
		return new Color(color[0], color[1], color[2], alpha);
	}
	
	private int currentLayer() {
		return currentLayer;
	}
	
	private void setCurrentLayer(int layer) {
		if(layer >= layers.size()) {
			layer = layers.size() - 1;
		}
		if(layer < 0) {
			layer = 0;
		}
		currentLayer = layer;
		list.setSelectedIndex(layer);
		canvasPanel.repaint();
		selectedObject = -1;
	}
	
	private class LayerListModel extends AbstractListModel<String> {
		@Override
		public int getSize() {
			return layers.size();
		}

		@Override
		public String getElementAt(int index) {
			//return index < layers.size() ? layers.get(index) : null;
			return layers.get(index).name;
		}
		
		public void moveElementUp() {
			if(currentLayer() > 0) {
				//swap current layer and one above it
				Layer current = layers.get(currentLayer());
				layers.set(currentLayer(), layers.get(currentLayer() - 1));
				layers.set(currentLayer() - 1, current);
				fireContentsChanged(this, currentLayer() - 1, currentLayer());
				setCurrentLayer(currentLayer() - 1);
			}
		}
		
		public void moveElementDown() {
			if(currentLayer() < layers.size() - 1) {
				//swap current layer and one above it
				Layer current = layers.get(currentLayer());
				layers.set(currentLayer(), layers.get(currentLayer() + 1));
				layers.set(currentLayer() + 1, current);
				fireContentsChanged(this, currentLayer(), currentLayer() + 1);
				setCurrentLayer(currentLayer() + 1);
			}
		}

		public void addElement() {
			layers.add(new Layer());
			fireIntervalAdded(this, layers.size() - 1, layers.size() - 1);
			setCurrentLayer(layers.size() - 1);
		}

		public void removeElement() {
			if(layers.size() > 1) {
				layers.remove(currentLayer());
				fireIntervalRemoved(this, currentLayer(), currentLayer());
				setCurrentLayer(currentLayer() - 1);
			}
		}
		
		public void clear() {
			int size = layers.size() - 1;
			layers.clear();
			fireIntervalRemoved(this, 0, size);
			addElement();
		}
	}
	
	private class Layer {
		public ArrayList<DrawnObject> pointlist = new ArrayList<>();
		public boolean visible = true;
		public String name;
		
		public Layer() {
			this.name = "Layer " + (layers.size() + 1);
		}
		
		public String toString() {
			return name + " " + pointlist;
		}
	}

}

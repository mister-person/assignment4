import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private JPanel canvasPanel;

	private ArrayList<Layer> layers = new ArrayList<>();
	private LayerListModel listModel = new LayerListModel();
	private JList<Layer> layerJList;
	private int currentLayer = 0;
	private JCheckBox visibleBox;
	private JTextField nameField; 

	private int mouseMode = 0;
	private ArrayList<Integer> selectedObjects = new ArrayList<>();
	private JPanel selectionSettings;
	private Point selectionOrigion;
	private int lastMouseX;
	private int lastMouseY;

	//brush settings
	private JPanel brushSettings;
	private Brush currentDrawnObject;
	private int size = 20;
	private int brush = 0;
	private int[] color = new int[]{0, 0, 0};
	private int[] customColor = new int[]{0, 0, 0};
	private int alpha = 255;


	private static final int MOUSE_DRAW = 0;
	private static final int MOUSE_SELECT = 1;

	public MainFrame() {
		super("window");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		//layers
		JPanel rightPanel = new JPanel();
		add(rightPanel, BorderLayout.EAST);
		rightPanel.setPreferredSize(new Dimension(200, 500));

		layers.add(new Layer());

		layerJList = new JList<Layer>(listModel);
		layerJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		layerJList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				setCurrentLayer(layerJList.getSelectedIndex());
				if(canvasPanel != null) {
					canvasPanel.repaint();
				}
				selectedObjects.clear();
			}
		});

		JScrollPane layerPane = new JScrollPane(layerJList);
		rightPanel.add(layerPane);
		layerPane.setPreferredSize(new Dimension(200, 300));

		nameField = new JTextField();
		nameField.setPreferredSize(new Dimension(100, 20));
		rightPanel.add(nameField);
		nameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				update(e); 
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				update(e);
			}

			private void update(DocumentEvent e) {
				layers.get(currentLayer()).name = nameField.getText();
				layerJList.repaint();
				canvasPanel.repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) { }
		});

		visibleBox = new JCheckBox("visible");
		visibleBox.getModel().setSelected(true);
		rightPanel.add(visibleBox);
		visibleBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layers.get(currentLayer).visible = ((JCheckBox)e.getSource()).getModel().isSelected();
				layerJList.repaint();
				canvasPanel.repaint();
			}
		});

		JButton addLayer = new JButton("Add Layer");
		rightPanel.add(addLayer);
		addLayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.addElement();
				//currentLayer = layers.size() - 1;
				layerJList.repaint();
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
				layerJList.repaint();
				canvasPanel.repaint();
			}
		});

		JButton moveLayerUp = new JButton("Move Layer Up");
		rightPanel.add(moveLayerUp);
		moveLayerUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.moveElementUp();
				layerJList.repaint();
				canvasPanel.repaint();
			}
		});

		JButton moveLayerDown = new JButton("Move Layer Down");
		rightPanel.add(moveLayerDown);
		moveLayerDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.moveElementDown();
				layerJList.repaint();
				canvasPanel.repaint();
			}
		});
		//end layers

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

		JButton mouseModeButton = new JButton("Select");
		leftPanel.add(mouseModeButton);
		mouseModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mouseMode = (mouseMode + 1)%2;
				if(mouseMode == MOUSE_DRAW) {
					mouseModeButton.setText("Select");
					brushSettings.setVisible(true);
					selectionSettings.setVisible(false);
				}
				else if(mouseMode == MOUSE_SELECT) {
					mouseModeButton.setText("Draw");
					brushSettings.setVisible(false);
					selectionSettings.setVisible(true);
				}
				selectedObjects.clear();
			}
		});

		//brush settings
		brushSettings = new JPanel();
		leftPanel.add(brushSettings);
		brushSettings.setPreferredSize(new Dimension(200, 500));

		JComboBox<String> brushes = new JComboBox<>(new String[]{"Circles", "Smooth Line", "Fill Area"});
		brushSettings.add(brushes);
		brushes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				brush = ((JComboBox<?>)e.getSource()).getSelectedIndex();
			}
		});

		brushSettings.add(new JLabel("Transparency:"));
		JSlider alphaSlider = new JSlider(0, 255, 0);
		brushSettings.add(alphaSlider);
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				alpha = 255-((JSlider)e.getSource()).getValue();
			}
		});

		JButton redButton = new JButton("Red");
		brushSettings.add(redButton);
		redButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{255, 0, 0};}
		});
		redButton.setForeground(new Color(255, 0, 0));

		JButton greenButton = new JButton("Green");
		brushSettings.add(greenButton);
		greenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{0, 255, 0};}
		});
		greenButton.setForeground(new Color(0, 255, 0));

		JButton blueButton = new JButton("Blue");
		brushSettings.add(blueButton);
		blueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{0, 0, 255};}
		});
		blueButton.setForeground(new Color(0, 0, 255));

		JButton blackButton = new JButton("Black");
		brushSettings.add(blackButton);
		blackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{0, 0, 0};}
		});
		blackButton.setForeground(new Color(0, 0, 0));

		JButton whiteButton = new JButton("White");
		brushSettings.add(whiteButton);
		whiteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = new int[]{255, 255, 255};}
		});
		whiteButton.setForeground(new Color(180, 180, 180));

		JButton customButton = new JButton("Custom");
		brushSettings.add(customButton);
		customButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {color = customColor;}
		});
		customButton.setForeground(new Color(0, 0, 0));

		brushSettings.add(new JLabel("Custom Red:"));
		JSlider customRed = new JSlider(0, 255, 0);
		brushSettings.add(customRed);
		customRed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[0] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		brushSettings.add(new JLabel("Custom Green:"));
		JSlider customGreen = new JSlider(0, 255, 0);
		brushSettings.add(customGreen);
		customGreen.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[1] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		brushSettings.add(new JLabel("Custom Blue:"));
		JSlider customBlue = new JSlider(0, 255, 0);
		brushSettings.add(customBlue);
		customBlue.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[2] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		//end brush settings

		//begin selection settings
		selectionSettings = new JPanel();
		leftPanel.add(selectionSettings);
		selectionSettings.setPreferredSize(new Dimension(200, 500));
		selectionSettings.setVisible(false);

		JButton deleteButton = new JButton("Delete");
		selectionSettings.add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedObjects.isEmpty()) {
					layers.get(currentLayer()).removeAll(selectedObjects);
					selectedObjects.clear();
					canvasPanel.repaint();
				}
			}
		});
		
		JButton upLayerButton = new JButton("Move Up Layer");
		selectionSettings.add(upLayerButton);
		upLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedObjects.isEmpty()) {
					if(currentLayer() >= 1) {
						//sort objects so they get moved in the correct order
						Collections.sort(selectedObjects);
						for(int i = 0; i < selectedObjects.size(); i++) {
							int index = selectedObjects.get(i);
							layers.get(currentLayer() - 1).shapelist.add(layers.get(currentLayer()).shapelist.get(index));
						}
						layers.get(currentLayer()).removeAll(selectedObjects);
						setCurrentLayer(currentLayer() - 1);
					}
					canvasPanel.repaint();
				}
			}
		});
		
		JButton downLayerButton = new JButton("Move Down Layer");
		selectionSettings.add(downLayerButton);
		downLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedObjects.isEmpty()) {
					if(currentLayer() < layers.size() - 1) {
						Collections.sort(selectedObjects);
						for(int i = 0; i < selectedObjects.size(); i++) {
							int index = selectedObjects.get(i);
							layers.get(currentLayer() + 1).shapelist.add(layers.get(currentLayer()).shapelist.get(index));
						}
						layers.get(currentLayer()).removeAll(selectedObjects);
						setCurrentLayer(currentLayer() + 1);
					}
					canvasPanel.repaint();
				}
			}
		});
		//end selection settings

		//drawing canvas
		canvasPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				for(int i = layers.size() - 1; i >= 0; i--) {
					Layer layer = layers.get(i);
					if(!layer.visible) {
						continue;//fewer indentations
					}
					for(int j = 0; j < layer.shapelist.size(); j++) {
						layer.shapelist.get(j).draw(g);
						if(currentLayer == i && selectedObjects.contains(j)) {
							layer.shapelist.get(j).drawOutline(g, new Color(255, 0, 0));
							Rectangle rect = layer.shapelist.get(j).getBounds();
							g.drawRect(rect.x, rect.y, rect.width, rect.height);
						}
					}
				}
				if(selectionOrigion != null) {
					Rectangle selection = new Rectangle(lastMouseX, lastMouseY, 0, 0);
					selection.add(selectionOrigion);
					g.setColor(new Color(255, 0, 0));
					g.drawRect(selection.x, selection.y, selection.width, selection.height);
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
		canvasPanel.repaint();

		//set selected layer to 0. it's way down here so it doesn't throw NullPointerException because stuff hasn't been initialized.  
		layerJList.setSelectedIndex(0);

		setSize(900, 600);
		setVisible(true);
	}

	private class CanvasMouseListener extends MouseAdapter{

		private void newDrawnObject() {
			if(brush == 0) {
				currentDrawnObject = new CirclesLineBrush();
			}
			else if(brush == 1) {
				currentDrawnObject = new SmoothLineBrush();
			}
			else if(brush == 2) {
				currentDrawnObject = new FillAreaBrush();
			}
			layers.get(currentLayer()).shapelist.add(currentDrawnObject);
		}

		private void selectObject(double x, double y, MouseEvent e) {
			for(int i = layers.get(currentLayer()).shapelist.size() - 1; i >= 0; i--) {
				if(layers.get(currentLayer()).shapelist.get(i).inObject(x, y)) {
					if(!selectedObjects.contains(i)) {
						if(!shouldMultiSelect(e)) {
							selectedObjects.clear();
						}
						selectedObjects.add(i);
					}
					return;
				}
			}
			if(!shouldMultiSelect(e)) {
				selectedObjects.clear();
			}
		}

		private void selectObjects(Rectangle selection, MouseEvent e) {
			if(!shouldMultiSelect(e)) {
				selectedObjects.clear();
			}
			for(int i = layers.get(currentLayer()).shapelist.size() - 1; i >= 0; i--) {
				if(layers.get(currentLayer()).shapelist.get(i).getBounds().intersects(selection)) {
					if(!selectedObjects.contains(i)) {
						selectedObjects.add(i);
					}
				}
			}
		}
		
		private boolean isMouseInSelection(MouseEvent e) {
			for(int i:selectedObjects) {
				if(layers.get(currentLayer()).shapelist.get(i).inObject(lastMouseX, lastMouseY)) {
					return true;
				}
			}
			return false;
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
				selectObject(e.getX(), e.getY(), e);
				if(selectedObjects.isEmpty()) {
					selectionOrigion = new Point(e.getX(), e.getY());
				}
				else {
					selectionOrigion = null;
				}
			}
			canvasPanel.updateUI();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(mouseMode == MOUSE_SELECT) {
				selectObject(e.getX(), e.getY(), e);
			}
			canvasPanel.updateUI();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(mouseMode == MOUSE_DRAW) {
				if(layers.get(currentLayer()).shapelist.isEmpty()) {
					newDrawnObject();
				}
				//size = (int) (Math.sqrt(Math.pow((e.getX() - lastMouseX), 2) + Math.pow((e.getY() - lastMouseY), 2))/10) + 2;
				currentDrawnObject.addPoint(new DrawnPoint(e.getX(), e.getY(), size, getColor()));
			}
			else if(mouseMode == MOUSE_SELECT) {
				if(selectionOrigion != null) {
					Rectangle selection = new Rectangle(e.getX(), e.getY(), 0, 0);
					selection.add(selectionOrigion);
					selectObjects(selection, e);
				}
				if(!selectedObjects.isEmpty() && selectionOrigion == null) {
					if(isMouseInSelection(e)) {
						for(int object:selectedObjects) {
							layers.get(currentLayer()).shapelist.get(object).transform(AffineTransform.getTranslateInstance(e.getX() - lastMouseX, e.getY() - lastMouseY));
						}
					}
				}
			}
			canvasPanel.updateUI();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			selectionOrigion = null;
			canvasPanel.updateUI();
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
		layerJList.setSelectedIndex(layer);
		visibleBox.getModel().setSelected(layers.get(currentLayer()).visible);
		nameField.setText(layers.get(currentLayer()).name);
		canvasPanel.repaint();
		selectedObjects.clear();
	}

	private class LayerListModel extends AbstractListModel<Layer> {
		@Override
		public int getSize() {
			return layers.size();
		}

		@Override
		public Layer getElementAt(int index) {
			//return index < layers.size() ? layers.get(index) : null;
			return layers.get(index);
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
	
	//copied from javax.swing.plaf.basic.BasicGraphicsUtils.isMenuShortcutKeyDown()
	static boolean shouldMultiSelect(InputEvent event) {
        return (event.getModifiers() &
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
    }

	private class Layer {
		public ArrayList<Brush> shapelist = new ArrayList<>();
		public boolean visible = true;
		public String name;

		public Layer() {
			this.name = "Layer " + (layers.size() + 1);
		}
		
		public void removeAll(List<Integer> indices) {
			Collections.sort(indices, Collections.reverseOrder());
			for(int i = 0; i < indices.size(); i++) {
				int index = indices.get(i);
				shapelist.remove(index);
			}
		}

		public String toString() {
			return name + (visible ? "" : "(invisible)");
		}
	}

}

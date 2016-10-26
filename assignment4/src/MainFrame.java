import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	private JPanel canvasPanel;

	//ArrayList of layers. Layer contains ArrayList of brushes, Brush contains ArrayList of points.
	private ArrayList<Layer> layers = new ArrayList<>();
	private LayerListModel listModel = new LayerListModel();
	private JList<Layer> layerJList;
	private int currentLayer = 0;
	private JCheckBox visibleBox;
	private JTextField nameField; 

	private ArrayList<Integer> selectedShapes = new ArrayList<>();
	private JLabel selectSomethingLabel;
	private JPanel selectionSettings;
	private Point selectionRectOrigion;
	private int lastMouseX;
	private int lastMouseY;

	//brush settings
	private BrushSettingsPanel brushSettings;
	private Brush currentDrawnShape;

	private int mouseMode = 0;
	public static final int MOUSE_DRAW = 0;
	public static final int MOUSE_SELECT = 1;

	public MainFrame() {
		super("window");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		//layers
		JPanel rightPanel = new JPanel();
		add(rightPanel, BorderLayout.EAST);
		rightPanel.setPreferredSize(new Dimension(200, 500));

		//start with one layer
		layers.add(new Layer());

		layerJList = new JList<Layer>(listModel);
		layerJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		layerJList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				setCurrentLayer(layerJList.getSelectedIndex());
				if(canvasPanel != null) {
					repaint();
				}
				//unselect everything if you move to a new layer
				selectedShapes.clear();
			}
		});
		
		rightPanel.add(new JLabel("Layers"));

		JScrollPane layerPane = new JScrollPane(layerJList);
		rightPanel.add(layerPane);
		layerPane.setPreferredSize(new Dimension(200, 300));

		//the text in this text field should always be the same as the current layer's name.
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

			//change current layer name to whatever was typed in the textfield
			private void update(DocumentEvent e) {
				layers.get(currentLayer()).name = nameField.getText();
				repaint();
			}

			@Override
			public void changedUpdate(DocumentEvent e) { }
		});

		//this should always be checked if the layer is visible and vice versa.
		visibleBox = new JCheckBox("visible");
		visibleBox.getModel().setSelected(true);
		rightPanel.add(visibleBox);
		visibleBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layers.get(currentLayer).visible = ((JCheckBox)e.getSource()).getModel().isSelected();
				repaint();
			}
		});

		JButton addLayer = new JButton("Add Layer");
		rightPanel.add(addLayer);
		addLayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.addElement();
				//currentLayer = layers.size() - 1;
				repaint();
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
				repaint();
			}
		});

		JButton moveLayerUp = new JButton("Move Layer Up");
		rightPanel.add(moveLayerUp);
		moveLayerUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.moveElementUp();
				repaint();
			}
		});

		JButton moveLayerDown = new JButton("Move Layer Down");
		rightPanel.add(moveLayerDown);
		moveLayerDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.moveElementDown();
				repaint();
			}
		});
		//end layers

		JPanel leftPanel = new JPanel();
		add(leftPanel, BorderLayout.WEST);
		leftPanel.setPreferredSize(new Dimension(200, 500));

		JButton clearButton = new JButton("Clear");
		clearButton.setToolTipText("Delete everything");
		leftPanel.add(clearButton);
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.clear();
				if(canvasPanel != null) {
					repaint();
				}
			}
		});

		//button to switch between draw mode and select mode.
		//it says "select" if clicking will bring you to select mode and "draw" if clicking will bring you to draw mode.
		JButton mouseModeButton = new JButton("Select");
		mouseModeButton.setToolTipText("Change to select mode");
		leftPanel.add(mouseModeButton);
		mouseModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mouseMode = (mouseMode + 1)%2;
				if(mouseMode == MOUSE_DRAW) {
					mouseModeButton.setText("Select");
					mouseModeButton.setToolTipText("Change to select mode");
					brushSettings.setVisible(true);
					selectionSettings.setVisible(false);
				}
				else if(mouseMode == MOUSE_SELECT) {
					mouseModeButton.setText("Draw");
					mouseModeButton.setToolTipText("Change to draw mode");
					brushSettings.setVisible(false);
					selectionSettings.setVisible(true);
				}
				selectedShapes.clear();
				repaint();
			}
		});

		//brush settings
		//moved into new class because it was getting long
		//is only visible in draw mode
		brushSettings = new BrushSettingsPanel();
		leftPanel.add(brushSettings);
		brushSettings.setPreferredSize(new Dimension(200, 500));

		//selection settings
		//is only visible in select mode
		selectionSettings = new JPanel();
		leftPanel.add(selectionSettings);
		selectionSettings.setPreferredSize(new Dimension(200, 500));
		selectionSettings.setVisible(false);//since it starts in draw mode

		//this only shows up if you try to do something with the selection without selecting anything
		//and it disappears if you select something.
		selectSomethingLabel = new JLabel("Select something first -->");
		selectSomethingLabel.setVisible(false);
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setToolTipText("Delete selected shapes");
		selectionSettings.add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedShapes.isEmpty()) {
					//delete all selected shapes from the layer and the selected shapes list.
					layers.get(currentLayer()).removeAll(selectedShapes);
					selectedShapes.clear();
					repaint();
				}
				else {
					//show notice if you haven't selected anything
					selectSomethingLabel.setVisible(true);
				}
			}
		});
		
		//move selected shapes to layer above the one they're in
		JButton upLayerButton = new JButton("Move Up Layer");
		upLayerButton.setToolTipText("Move selected shapes to layer above the one they're in");
		selectionSettings.add(upLayerButton);
		upLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedShapes.isEmpty()) {
					if(currentLayer() >= 1) {
						//sort selected shapes so they get moved in the correct order
						Collections.sort(selectedShapes);
						//then move shapes one by one to the layer above
						for(int i = 0; i < selectedShapes.size(); i++) {
							int index = selectedShapes.get(i);
							layers.get(currentLayer() - 1).shapelist.add(layers.get(currentLayer()).shapelist.get(index));
						}
						//and delete them from the current layer.
						layers.get(currentLayer()).removeAll(selectedShapes);
						setCurrentLayer(currentLayer() - 1);
					}
					repaint();
				}
				else {
					selectSomethingLabel.setVisible(true);
				}
			}
		});
		
		JButton downLayerButton = new JButton("Move Down Layer");
		downLayerButton.setToolTipText("Move selected shapes to layer below the one they're in");
		selectionSettings.add(downLayerButton);
		downLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!selectedShapes.isEmpty()) {
					if(currentLayer() < layers.size() - 1) {
						//same as above except moving shapes down.
						Collections.sort(selectedShapes);
						for(int i = 0; i < selectedShapes.size(); i++) {
							int index = selectedShapes.get(i);
							layers.get(currentLayer() + 1).shapelist.add(layers.get(currentLayer()).shapelist.get(index));
						}
						layers.get(currentLayer()).removeAll(selectedShapes);
						setCurrentLayer(currentLayer() + 1);
					}
					repaint();
				}
				else {
					selectSomethingLabel.setVisible(true);
				}
			}
		});
		
		selectionSettings.add(selectSomethingLabel);
		//end selection settings

		//drawing canvas
		canvasPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				//draw each layer
				for(int i = layers.size() - 1; i >= 0; i--) {
					Layer layer = layers.get(i);
					//don't draw layer if it's invisible
					if(!layer.visible) {
						continue;
					}

					//make a new bufferedimage to draw this layer on
					BufferedImage image = new BufferedImage(canvasPanel.getWidth(), canvasPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D imageGraphics = image.createGraphics();
					//make it so transparent shapes are not transparent to other shapes on the same layer
					imageGraphics.setComposite(AlphaComposite.Src);

					//draw each shape in this layer onto image
					for(int j = 0; j < layer.shapelist.size(); j++) {
						Brush shape = layer.shapelist.get(j);

						shape.draw(imageGraphics);
					}
					
					g.drawImage(image, 0, 0, null);
				}
				//draw red outlines around selected shapes, its down here so it gets drawn above all the drawings.
				for(int i = 0; i < layers.get(currentLayer()).shapelist.size(); i++) {
					if(selectedShapes.contains(i)) {
						Brush shape = layers.get(currentLayer()).shapelist.get(i);
						g.setColor(Color.RED);
						shape.drawOutline((Graphics2D)g);
						
						Rectangle rect = shape.getBounds();
						g.drawRect(rect.x, rect.y, rect.width, rect.height);
					}
				}

				//draw the selection box, when you click and drag to select things in select mode
				if(selectionRectOrigion != null) {
					Rectangle selection = new Rectangle(lastMouseX, lastMouseY, 0, 0);
					selection.add(selectionRectOrigion);
					g.setColor(Color.RED);
					g.drawRect(selection.x, selection.y, selection.width, selection.height);
				}
			}
		};
		canvasPanel.setLayout(new FlowLayout());
		CanvasMouseListener listener = new CanvasMouseListener();
		canvasPanel.addMouseListener(listener);
		canvasPanel.addMouseMotionListener(listener);
		add(canvasPanel, BorderLayout.CENTER);
		
		canvasPanel.setBackground(Color.WHITE);
		canvasPanel.setVisible(true);
		canvasPanel.setPreferredSize(new Dimension(500, 500));
		repaint();

		//set selected layer to 0. it's way down here so it doesn't NullPointerException because stuff hasn't been initialized.  
		layerJList.setSelectedIndex(0);

		setSize(900, 600);
		setVisible(true);
	}

	private class CanvasMouseListener extends MouseAdapter{

		private void newDrawnShape() {
			//create new brush based on the state of the brush drop down menu, add it to the current layer, and make it the currentDrawnShape.
			currentDrawnShape = brushSettings.createNewBrush();
			layers.get(currentLayer()).shapelist.add(currentDrawnShape);
		}

		//note: can only select shapes in current layer
		private void selectShape(double x, double y, MouseEvent e) {
			//loop through all shapes in current layer and see if (x, y) is inside them, if it is add it to the selected shapes if it's not already there.
			//if command key is down (shouldMultiSelect()) removes it from the list if it's already there
			//if command key is not down, remove all other shapes from selected list. 
			for(int i = layers.get(currentLayer()).shapelist.size() - 1; i >= 0; i--) {
				if(layers.get(currentLayer()).shapelist.get(i).containsPoint(x, y)) {
					if(!selectedShapes.contains(i)) {
						if(!shouldMultiSelect(e)) {
							selectedShapes.clear();
						}
						selectedShapes.add(i);
						selectSomethingLabel.setVisible(false);
					}
					else if(shouldMultiSelect(e)) {
						selectedShapes.remove(Integer.valueOf(i));
					}
					return;
				}
			}
			if(!shouldMultiSelect(e)) {
				selectedShapes.clear();
			}
		}

		private void selectShape(Rectangle selection, MouseEvent e) {
			if(!shouldMultiSelect(e)) {
				selectedShapes.clear();
			}
			//add every shape in current layer who's bounding box intersects with selection to the selected shape list.
			for(int i = layers.get(currentLayer()).shapelist.size() - 1; i >= 0; i--) {
				if(layers.get(currentLayer()).shapelist.get(i).getBounds().intersects(selection)) {
					if(!selectedShapes.contains(i)) {
						selectedShapes.add(i);
						selectSomethingLabel.setVisible(false);
					}
				}
			}
		}
		
		private boolean isMouseInSelection(MouseEvent e) {
			//test if the mouse is hovering over any currently selected shape
			for(int i:selectedShapes) {
				if(layers.get(currentLayer()).shapelist.get(i).containsPoint(lastMouseX, lastMouseY)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			//if in draw mode create new shape and add the mouse's position to it
			if(mouseMode == MOUSE_DRAW) {
				newDrawnShape();
				currentDrawnShape.addPoint(new DrawnPoint(e.getX(), e.getY(), brushSettings.getBrushSize(), brushSettings.getBrushColor()));
			}
			else if(mouseMode == MOUSE_SELECT) {
				//select shape if it's moused over
				selectShape(e.getX(), e.getY(), e);
				
				//only start selection rectangle if nothing was selected by selectShape or if command key is down,
				//otherwise we want to drag the selected shapes not start selection
				if(selectedShapes.isEmpty() || shouldMultiSelect(e)) {
					selectionRectOrigion = new Point(e.getX(), e.getY());
				}
				else {
					selectionRectOrigion = null;
				}
			}
			repaint();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(mouseMode == MOUSE_DRAW) {//draw mode
				//if somehow we're dragging the mouse and there's no currently drawn shape, make one.
				if(layers.get(currentLayer()).shapelist.isEmpty()) {
					newDrawnShape();
				}
				//add a new point to the current shape
				currentDrawnShape.addPoint(new DrawnPoint(e.getX(), e.getY(), brushSettings.getBrushSize(), brushSettings.getBrushColor()));
			}
			else if(mouseMode == MOUSE_SELECT) {//select mode
				//if there is a selection rect, select every shape inside of it
				if(selectionRectOrigion != null) {
					Rectangle selection = new Rectangle(e.getX(), e.getY(), 0, 0);
					selection.add(selectionRectOrigion);
					selectShape(selection, e);
				}
				//if there's not a selection rect and the mouse is over a selected shape, drag all the selected shapes with the mouse.
				if(!selectedShapes.isEmpty() && selectionRectOrigion == null) {
					if(isMouseInSelection(e)) {
						for(int shape:selectedShapes) {
							layers.get(currentLayer()).shapelist.get(shape).transform(AffineTransform.getTranslateInstance(e.getX() - lastMouseX, e.getY() - lastMouseY));
						}
					}
				}
			}
			repaint();
			lastMouseX = e.getX();
			lastMouseY = e.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			//get rid of selection rect
			selectionRectOrigion = null;
			repaint();
		}

	}

	private int currentLayer() {
		return currentLayer;
	}

	//change layers
	private void setCurrentLayer(int layer) {
		//if layer is out of bounds, set it to in bounds
		if(layer >= layers.size()) {
			layer = layers.size() - 1;
		}
		if(layer < 0) {
			layer = 0;
		}
		currentLayer = layer;
		layerJList.setSelectedIndex(layer);
		//set "is layer visible" checkbox to visible state of new layer
		visibleBox.getModel().setSelected(layers.get(currentLayer()).visible);
		//set layer name text field to name of new layer
		nameField.setText(layers.get(currentLayer()).name);
		//unselect every shape on old layer
		selectedShapes.clear();
		repaint();
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
				//notify jlist that contents changed
				fireContentsChanged(this, currentLayer() - 1, currentLayer());
				//move to above layer
				setCurrentLayer(currentLayer() - 1);
			}
		}

		public void moveElementDown() {
			if(currentLayer() < layers.size() - 1) {
				//swap current layer and one below it
				Layer current = layers.get(currentLayer());
				layers.set(currentLayer(), layers.get(currentLayer() + 1));
				layers.set(currentLayer() + 1, current);
				fireContentsChanged(this, currentLayer(), currentLayer() + 1);
				//move to below layer
				setCurrentLayer(currentLayer() + 1);
			}
		}

		public void addElement() {
			//add new layer, tell the jlist about it, and move to that layer
			layers.add(new Layer());
			fireIntervalAdded(this, layers.size() - 1, layers.size() - 1);
			setCurrentLayer(layers.size() - 1);
		}

		public void removeElement() {
			if(layers.size() > 1) {
				//remove current layer and tell the jlist about it
				layers.remove(currentLayer());
				fireIntervalRemoved(this, currentLayer(), currentLayer());
			}
		}

		public void clear() {
			//remove every existing layer and add a new one then tell the jlist about it
			int size = layers.size() - 1;
			layers.clear();
			addElement();
			fireIntervalRemoved(this, 0, size);
		}
	}
	
	static boolean shouldMultiSelect(InputEvent event) {
		//copied from javax.swing.plaf.basic.BasicGraphicsUtils.isMenuShortcutKeyDown()
        return (event.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
    }

	private class Layer {
		public ArrayList<Brush> shapelist = new ArrayList<>();
		public boolean visible = true;
		public String name;

		public Layer() {
			this.name = "Layer " + (layers.size() + 1);
		}
		
		public void removeAll(List<Integer> indices) {
			//sort indices so the correct ones get removed then remove them all from the shape list
			Collections.sort(indices, Collections.reverseOrder());
			for(int i = 0; i < indices.size(); i++) {
				int index = indices.get(i);
				shapelist.remove(index);
			}
		}

		public String toString() {
			//this is what is displayed on the jlist
			return name + (visible ? "" : "(invisible)");
		}
	}

}

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class BrushSettingsPanel extends JPanel {

	private int[] customColor = new int[]{0, 0, 0};
	private int size = 15;
	private int brush = 0;
	private int[] color = new int[]{0, 0, 0};
	private int alpha = 255;

	//brush that draws circles at every point
	public static final int BRUSH_CIRCLES = 0;
	//brush that draws a smooth line between every point
	public static final int BRUSH_SMOOTH_LINE = 1;
	//brush that fills the area inside every point
	public static final int BRUSH_FILL_AREA = 2;
	//brush that draws a rectangle between the first and last point
	public static final int BRUSH_RECTANGLE = 3;

	public BrushSettingsPanel() {

		//drop down menu to choose which brush to use
		JComboBox<String> brushes = new JComboBox<>(new String[]{"Circles", "Smooth Line", "Fill Area", "Rectangle"});
		add(brushes);
		brushes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				brush = ((JComboBox<?>)e.getSource()).getSelectedIndex();
			}
		});

		//slider to choose transparency
		add(new JLabel("Transparency:"));
		JSlider alphaSlider = new JSlider(0, 255, 0);
		add(alphaSlider);
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				alpha = 255-((JSlider)e.getSource()).getValue();
			}
		});

		//buttons to change current color
		JButton redButton = new JButton("Red");
		add(redButton);
		redButton.addActionListener(new ColorListener(new int[]{255, 0, 0}));
		redButton.setForeground(Color.RED);

		JButton greenButton = new JButton("Green");
		add(greenButton);
		greenButton.addActionListener(new ColorListener(new int[]{0, 255, 0}));
		greenButton.setForeground(Color.GREEN);

		JButton blueButton = new JButton("Blue");
		add(blueButton);
		blueButton.addActionListener(new ColorListener(new int[]{0, 0, 255}));
		blueButton.setForeground(Color.BLUE);

		JButton blackButton = new JButton("Black");
		add(blackButton);
		blackButton.addActionListener(new ColorListener(new int[]{0, 0, 0}));
		blackButton.setForeground(Color.BLACK);

		//4th number in color array overrides value of alpha variable controlled by slider
		JButton whiteButton = new JButton("Erase");
		add(whiteButton);
		whiteButton.addActionListener(new ColorListener(new int[]{255, 255, 255, 0}));
		whiteButton.setForeground(new Color(180, 180, 180));

		//passes reference to customColor to the listener so the custom color changes when the customColor variable is changed
		JButton customButton = new JButton("Custom");
		add(customButton);
		customButton.addActionListener(new ColorListener(customColor));
		customButton.setForeground(Color.BLACK);

		//custom color sliders change value of customColor variable and color of custom color button
		add(new JLabel("Custom Red:"));
		JSlider customRed = new JSlider(0, 255, 0);
		add(customRed);
		customRed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[0] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		add(new JLabel("Custom Green:"));
		JSlider customGreen = new JSlider(0, 255, 0);
		add(customGreen);
		customGreen.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[1] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		add(new JLabel("Custom Blue:"));
		JSlider customBlue = new JSlider(0, 255, 0);
		add(customBlue);
		customBlue.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				customColor[2] = ((JSlider)e.getSource()).getValue();
				customButton.setForeground(new Color(customColor[0], customColor[1], customColor[2]));
			}
		});
		
		//brush size buttons
		JButton smallSizeButton = new JButton("Small Brush");
		add(smallSizeButton);
		smallSizeButton.addActionListener(new SizeListener(5));
		
		JButton mediumSizeButton = new JButton("Medium Brush");
		add(mediumSizeButton);
		mediumSizeButton.addActionListener(new SizeListener(15));
		
		JButton largeSizeButton = new JButton("Large Brush");
		add(largeSizeButton);
		largeSizeButton.addActionListener(new SizeListener(25));
		
		JButton hugeSizeButton = new JButton("Huge Brush");
		add(hugeSizeButton);
		hugeSizeButton.addActionListener(new SizeListener(50));
	}
	
	public Color getBrushColor() {
		//makes a Color object out of the int array color, if array length is 4 then the 4th element is the alpha, otherwise variable alpha is the alpha
		return new Color(color[0], color[1], color[2], color.length == 4 ? color[3] : alpha);
	}
	
	public int getBrushSize() {
		return size;
	}
	
	public Brush createNewBrush() {
		return createNewBrush(brush);
	}
	
	public Brush createNewBrush(int brushType) {
		if(brushType == BRUSH_CIRCLES) {
			return new CirclesLineBrush();
		}
		else if(brushType == BRUSH_SMOOTH_LINE) {
			return new SmoothLineBrush();
		}
		else if(brushType == BRUSH_FILL_AREA) {
			return new FillAreaBrush();
		}
		else if(brushType == BRUSH_RECTANGLE) {
			return new BrushRectangle();
		}
		else {
			return new CirclesLineBrush();
		}
	}

	private class SizeListener implements ActionListener {
		private int buttonSize;

		public SizeListener(int size) {
			this.buttonSize = size;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			size = buttonSize;
		}
	}

	private class ColorListener implements ActionListener {

		private int[] buttonColor;

		public ColorListener(int[] color) {
			buttonColor = color;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			color = buttonColor;
		}

	}

}

import java.awt.Color;

public class DrawnPoint extends Point {
	private int size;
	private int[] color;

	public DrawnPoint(double xPos, double yPos, int size, int[] color) {
		super(xPos, yPos);
		this.size = size;
		this.color = new int[]{0, 0, 0, 255};
		for(int i = 0; i < color.length || i < this.color.length; i++) {
			this.color[i] = color[i];
		}
	}

	public DrawnPoint(double xPos, double yPos, int size, Color color) {
		this(xPos, yPos, size, new int[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()});
	}
	
	public DrawnPoint(double x, double y, int size) {
		this(x, y, size, new int[]{155, 155, 155, 255});
	}

	public int getSize() {
		return size;
	}

	public Color getColor() {
		return new Color(color[0], color[1], color[2], color[3]);
	}

	public int getRed() {
		return color[0];
	}

	public int getGreen() {
		return color[1];
	}

	public int getBlue() {
		return color[2];
	}

	public int getAlpha() {
		return color[3];
	}
	
	public int intxPos() {
		return (int)x;
	}

	public int intyPos() {
		return (int)y;
	}

	@Override
	public String toString() {
		return super.toString();
		//return x + ", " + y + " size: " + size + "color: " + color[0] + " " + color[1] + " " + color[2];
	}

}

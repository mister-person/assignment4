import java.awt.Color;

@SuppressWarnings("serial")
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

	public int getSize() {
		return size;
	}

	public Color getColor() {
		return new Color(color[0], color[1], color[2], color[3]);
	}
	
	@Override
	public DrawnPoint copy() {
		return new DrawnPoint(x, y, size, color);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}

import java.awt.Color;

public class Point {
	private int xPos;
	private int yPos;
	private int size;
	private int[] color;
	
	public Point(int xPos, int yPos, int size, int[] color) {
		super();
		this.xPos = xPos;
		this.yPos = yPos;
		this.size = size;
		this.color = new int[]{0, 0, 0, 255};
		for(int i = 0; i < color.length || i < this.color.length; i++) {
			this.color[i] = color[i];
		}
	}
	
	public Point(int xPos, int yPos, int size, Color color) {
		this(xPos, yPos, size, new int[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()});
	}

	public Point(int x, int y) {
		this(x, y, 10, new int[]{155, 155, 155, 255});
	}

	public Point(int x, int y, int size) {
		this(x, y, size, new int[]{155, 155, 155, 255});
	}

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public int getSize() {
		return size;
	}

	public Color getColor() {
		return new Color(color[0], color[1], color[2], color[3]);
	}
	
	@Override
	public String toString() {
		return xPos + ", " + yPos + " size: " + size + "color: " + color[0] + " " + color[1] + " " + color[2];
	}
	
}


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
		this.color = color;
	}

	public Point(int x, int y) {
		this(x, y, 10, new int[]{155, 155, 155});
	}

	public Point(int x, int y, int size) {
		this(x, y, size, new int[]{155, 155, 155});
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

	public int[] getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return xPos + ", " + yPos + " size: " + size + "color: " + color[0] + " " + color[1] + " " + color[2];
	}
	
}

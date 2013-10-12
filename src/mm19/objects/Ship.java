package mm19.objects;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;

import mm19.forseth.ForeverClient;

import org.json.JSONException;
import org.json.JSONObject;

public class Ship {
	
	public enum ShipType{Pilot, Destroyer, Main};
	public int ID;
	public int health;
	public ShipType type;
	public int xCoord;
	public int yCoord;
	public String orientation;
	
	private int width;

	/**
	 * Constructor.
	 * @param i
	 * @param h
	 * @param t
	 * @param x
	 * @param y
	 * @param o
	 */
	public Ship(int i, int h, ShipType t, int x, int y, String o) {
		ID = i;
		health = h;
		type = t;
		xCoord = x;
		yCoord = y;
		orientation = o;
	}

	/**
	 * Constructor.
	 * @param obj
	 * @throws JSONException
	 */
	public Ship(JSONObject obj) throws JSONException{
		ID = obj.getInt("ID");
		health = obj.getInt("health");

		String t = obj.getString("type");
		if (t.equals("P")) {
			type = ShipType.Pilot;
			width = 2;
		} else if (t.equals("D")) {
			type = ShipType.Destroyer;
			width = 4;
		} else if (t.equals("M")) {
			type = ShipType.Main;
			width = 5;
		}

		xCoord = obj.getInt("xCoord");
		yCoord = obj.getInt("yCoord");
		orientation = obj.getString("orientation");
	}
	
	public JSONObject toMainJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("xCoord", xCoord);
		obj.put("yCoord", yCoord);
		obj.put("orientation", orientation);
		return obj;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = toMainJSONObject();
		switch (type) {
		case Destroyer:
			obj.put("type", "D");
			break;
			
		case Pilot:
			obj.put("type", "P");
			break;
			
		case Main:
			obj.put("type", "M");
			break;
			
		default:
			System.err.println("NO. YOU SUCK. BAH.");
			break;
		}
		return obj;
	}
	
	public void takeDamage(int damage) {
		health -= damage;
	}

	public void move(int x, int y) {
		xCoord = x;
		yCoord = y;
	}
	
	/**
	 * Return the length (long side) of the ship.
	 * @return
	 */
	public int getLength() {
		switch(type) {
		case Main:
			return 5;
		case Destroyer:
			return 4;
		case Pilot:
			return 2;
		}
		return -1;
	}
	
	/**
	 * Return true if this ship is placed over the given coordinate.
	 * @param coord
	 * @return
	 */
	public boolean contains(Point coord) {
		return asRect().contains(coord);
	}
	
	/**
	 * Return rectangle representation of ship.
	 * @return
	 */
	public Rectangle asRect() {
		
		if (orientation.equals("H")) {
			return new Rectangle(xCoord, yCoord, 1, getLength());
		} else {
			return new Rectangle(xCoord, yCoord, getLength(), 1);
		}
		
	}
	
	/**
	 * Return rectangle representation of ship with given coordinates and orientation.
	 * @return
	 */
	public Rectangle asRect(int x, int y, String orient) {
		
		if (orient.equals("H")) {
			return new Rectangle(x, y, 1, getLength());
		} else {
			return new Rectangle(x, y, getLength(), 1);
		}
		
	}
	
	public void moveRandom(List<Ship> myShips) {
		
		Random rand = new Random();
		
		String newOrient = rand.nextInt(2) == 1 ? "H" : "V";
		int newX = rand.nextInt(ForeverClient.MAP_WIDTH);
		int newY = rand.nextInt(ForeverClient.MAP_HEIGHT);
		
		Rectangle newRect = asRect(newX, newY, newOrient);
		
		while (wouldCollideWithOther(newRect, myShips)) {
			
			newOrient = rand.nextInt(2) == 1 ? "H" : "V";
			newX = rand.nextInt(ForeverClient.MAP_WIDTH);
			newY = rand.nextInt(ForeverClient.MAP_HEIGHT);
			
			newRect = asRect(newX, newY, newOrient);
		}
		
		// update ship's values so other methods can retrieve them
		move(newX, newY);
		orientation = newOrient;
	
	}
	
	/**
	 * Check if this ship collides with any other friendly ships.
	 * 
	 * @param myShips
	 * @param moveHoriz
	 * @return
	 */
	public boolean wouldCollideWithOther(Rectangle shipAsRect, List<Ship> myShips) {
		
		for (Ship ship : myShips) {
			if (shipAsRect.intersects(ship.asRect())) {
				return true;
			}
		}
		
		return false;
		
	}
	

//	public boolean collides(Ship other, boolean moveHoriz) {
//
//		int myShipLength = 0;
//		int otherShipLength = 0;
//
//		Rectangle myShipRect = null;
//		Rectangle otherShipRect = null;
//
//		// figure out this ship's length
//		switch(this.type) {
//		case Main:
//			myShipLength = 5;
//			break;
//		case Destroyer:
//			myShipLength = 4;
//			break;
//		case Pilot:
//			myShipLength = 2;
//			break;
//		}
//
//		// figure out other ship's length
//		switch(other.type) {
//		case Main:
//			otherShipLength = 5;
//			break;
//		case Destroyer:
//			otherShipLength = 4;
//			break;
//		case Pilot:
//			otherShipLength = 2;
//			break;
//		}
//
//		if (moveHoriz) {
//			myShipRect = new Rectangle(this.xCoord, this.yCoord, 1, myShipLength);
//		} else {
//			myShipRect = new Rectangle(this.xCoord, this.yCoord, myShipLength, 1);
//		}
//
//		if (other.orientation.equals("H")) {
//			otherShipRect = new Rectangle(other.xCoord, other.yCoord, 1, otherShipLength);
//		} else {			
//			otherShipRect = new Rectangle(other.xCoord, other.yCoord, otherShipLength, 1);
//		}
//
//		return myShipRect.intersects(otherShipRect);
//	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(type.equals("M")) {
			sb.append("Main ");
		} else if (type.equals("D")) {
			sb.append("Destroyer ");
		} else if (type.equals("P")) {
			sb.append("Pilot " );
		}

		sb.append("Ship " + ID + " at (" + xCoord + ", " + yCoord + ")\n");
		sb.append('\t' + "Health : " + health + '\n');
		sb.append('\t' + "Orientation: " + orientation + '\n');

		return sb.toString();

	}
}

package mm19.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class HitReport {
	public int xCoord;
	public int yCoord;
	public boolean hit;
	
	HitReport(int x, int y, boolean h) {
		xCoord = x;
		yCoord = y;
		hit = h;
	}
	
	public HitReport(JSONObject obj) throws JSONException{
		xCoord = obj.getInt("xCoord");
		yCoord = obj.getInt("yCoord");
		hit = obj.getBoolean("hit");
	}
	
	public String toString() {
		return "\t(" + xCoord + ", " + yCoord + ")" + (hit ? " Hit" : " Missed");
	}
}

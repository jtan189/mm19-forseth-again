package mm19.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class PingReport {
	public int shipID;
	public int distance;
	
	public PingReport(int s, int d) {
		shipID = s;
		distance = d;
	}
	
	public PingReport(JSONObject obj) throws JSONException{
		shipID = obj.getInt("shipID");
		distance = obj.getInt("distance");
	}
	
	public String toString() {
		return "\tPing hit ship" + shipID + ", " + distance + " away\n";
	}
}

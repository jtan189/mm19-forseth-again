package mm19.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class ShipAction {
	public enum Action{Fire, MoveH, MoveV, BurstShot, Sonar, Nothing};
	
	public int shipID;
	public int actionX;
	public int actionY;
	public Action actionID;
	public int actionExtra;
	
	// Quick constructor to set the Ship to do nothing
	public ShipAction(int sID) {
		shipID = sID;
		actionX = 0;
		actionY = 0;
		actionID = Action.Nothing;
		actionExtra = 0;
	}
	
	public ShipAction(int sID, int x, int y, Action aID, int ae) {
		shipID = sID;
		actionX = x;
		actionY = y;
		actionID = aID;
		actionExtra = ae;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("ID", shipID);
			obj.put("actionX", actionX);
			obj.put("actionY", actionY);
			obj.put("actionExtra", actionExtra);
			
			if(actionID == Action.Fire) {
				obj.put("actionID", "F");
			} else if(actionID == Action.MoveH) {
				obj.put("actionID", "MH");
			} else if(actionID == Action.MoveV) {
				obj.put("actionID", "MV");
			} else if(actionID == Action.BurstShot) {
				obj.put("actionID", "BS");
			} else if(actionID == Action.Sonar) {
				obj.put("actionID", "S");
			} else if(actionID == Action.Nothing) {
				obj.put("actionID", "N");
			}
			
			return obj;
		} catch (JSONException e) {
			return null;
		}
	}
	
}

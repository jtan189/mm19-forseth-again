package mm19.objects;

import org.json.JSONException;
import org.json.JSONObject;

public class ActionResult {
	public enum Result {
		SUCCESS, INSUFFICIENT, INCORRECT;
	}
	public int ID;
	public String result;;
	
	public ActionResult(int i, String r) {
		ID = i;
		result = r;
	}
	
	public ActionResult(JSONObject obj) throws JSONException{
		ID = obj.getInt("ID");
		result = obj.getString("result");
	}
	
	@Override
	public String toString() {
		return "\tID: " + ID + " , Result: " + result +'\n';
	}
}

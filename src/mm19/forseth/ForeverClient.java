package mm19.forseth;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mm19.objects.ActionResult;
import mm19.objects.Ship;
import mm19.objects.ShipAction;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

public class ForeverClient extends TestClient {

	/**
	 * The number of bullets to unload on enemies we've detected.
	 */
	private static final int UNLOAD_BULLET_COUNT = 3;

	public ForeverClient() {
		super("ForsethAgain");
	}

	@Override
	public JSONObject setup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processResponse(ServerResponse sr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleInterrupt(ServerResponse sr) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Dumps 3 shots on any ships we detected.
	 * 
	 * @param fireableShips All ships that can fire.
	 * @param results Results of fired actions.
	 * @return A list of ship action that are the firing actions.
	 */
	private List<ShipAction> unloadArsenal(ArrayList<Ship> fireableShips, Collection<ActionResult> results) {
		List<ShipAction> fireActions = new ArrayList<ShipAction>();
		for (ActionResult ar : results) {
			if (fireableShips.size() > UNLOAD_BULLET_COUNT) {
				if (ar == null) { // ar .
					for (int i = 0; i < UNLOAD_BULLET_COUNT; i++) {
						int id = (fireableShips.remove(0)).ID;
						ShipAction sa = new ShipAction(id);
						sa.actionID
					}
				}
			} else {
				break;
			}
		}
		
	}

}

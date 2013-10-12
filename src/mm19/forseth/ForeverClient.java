package mm19.forseth;

import java.util.ArrayList;

import org.json.JSONObject;


import mm19.objects.HitReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
<<<<<<< HEAD
import mm19.objects.ActionResult;
=======

import mm19.objects.Ship;
>>>>>>> f5c8129a89af9e5746d7f293abf991bac4ab537d
import mm19.objects.ShipAction;
import mm19.objects.ShotResult;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

public class ForeverClient extends TestClient {
	
	private Ship mainShip;

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
	
	private int numDestroyersLeft(ArrayList<Ship> myShips) {
		int count = 0;
		for (Ship ship : myShips) {
			if (ship.type == ShipType.Destroyer) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Move ships, if they were hit. Main ship has highest priority. Pilots take priorities over destroyers.
	 * @param hitReports Hit reports for the previous turn.
	 * @param myShips All the ships currently existing.
	 */
	private void moveShips(ArrayList<HitReport> hitReports, ArrayList<Ship> myShips) {
		
		// flags
		boolean mainHit = false;
		boolean destroyerHit = false;
		boolean pilotHit = false;
		
		ArrayList<Ship> shipsHit = new ArrayList<Ship>();
		for (HitReport report : hitReports) {
			if (report.hit) {
				// figure out what was hit
				for (Ship ship : myShips) {
					if (report.xCoord == ship.xCoord && report.yCoord == ship.yCoord) {
						shipsHit.add(ship);
						
						// set flags
						if (ship.type == ShipType.Main) {
							mainHit = true;
						} else if (ship.type == ShipType.Destroyer) {
							destroyerHit = true;
						} else {
							pilotHit = true;
						}
						
						break;
					}
				}
			}
		}
		
		// decide what to move
		if (mainHit) {
			// move the main
		} else if (destroyerHit && pilotHit && numDestroyersLeft(myShips) == 1) {
			// move the destroyer
		} else if (destroyerHit && pilotHit) {
			// move the pilot
		} else {
			// move whatever was hit
		}
		
	}

	/**
	 * Dumps 3 shots on any ships we detected.
	 * 
	 * @param fireableShips All ships that can fire.
	 * @param results Results of shot actions.
	 * @return A list of the firing actions.
	 */
	private List<ShipAction> unloadArsenal(ArrayList<Ship> fireableShips, Collection<ShotResult> results) {
		List<ShipAction> fireActions = new ArrayList<ShipAction>();
		for (ShotResult sr : results) {
			if (fireableShips.size() > UNLOAD_BULLET_COUNT) {
				if (sr == null) { // ar .
					for (int i = 0; i < UNLOAD_BULLET_COUNT; i++) {
						int id = (fireableShips.remove(0)).ID;
						ShipAction sa = new ShipAction(id);
						sa.actionID = ShipAction.Action.Fire;
						sa.actionX = sr.x;
						sa.actionY = sr.y;
						fireActions.add(sa);
					}
				}
			} else {
				break;
			}
		}
		return fireActions;
	}

}

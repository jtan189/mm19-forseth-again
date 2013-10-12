package mm19.forseth;

import java.util.ArrayList;

import org.json.JSONObject;

import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;

import mm19.objects.HitReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mm19.objects.ActionResult;

import mm19.objects.Ship;
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
	
	public ArrayList<Ship> placeShips(){
	
		ArrayList<Ship> list = new ArrayList<Ship>();
		//(int i, int h, ShipType t, int x, int y, String o)
		list.add(new Ship(0, 60, ShipType.Main, 79, 80, "V"));
		list.add(new Ship(1, 40, ShipType.Destroyer, 3, 36, "H"));
		list.add(new Ship(2, 40, ShipType.Destroyer, 7, 94, "V"));
		list.add(new Ship(3, 40, ShipType.Destroyer, 14, 78, "H"));
		list.add(new Ship(4, 40, ShipType.Destroyer, 18, 80, "V"));
		list.add(new Ship(5, 40, ShipType.Destroyer, 22, 90, "H"));
		list.add(new Ship(6, 40, ShipType.Destroyer, 27, 22, "V"));
		list.add(new Ship(7, 40, ShipType.Destroyer, 31, 73, "H"));
		list.add(new Ship(8, 20, ShipType.Pilot, 35, 86, "V"));
		list.add(new Ship(9, 20, ShipType.Pilot, 43, 2, "H"));
		list.add(new Ship(10, 20, ShipType.Pilot, 49, 11, "V"));
		list.add(new Ship(11, 20, ShipType.Pilot, 54, 27, "H"));
		list.add(new Ship(12, 20, ShipType.Pilot, 59, 8, "V"));
		list.add(new Ship(13, 20, ShipType.Pilot, 63, 46, "H"));
		list.add(new Ship(14, 20, ShipType.Pilot, 67, 19, "V"));
		list.add(new Ship(15, 20, ShipType.Pilot, 72, 88, "H"));
		list.add(new Ship(16, 20, ShipType.Pilot, 86, 25, "V"));
		list.add(new Ship(17, 20, ShipType.Pilot, 91, 71, "H"));
		list.add(new Ship(18, 20, ShipType.Pilot, 95, 69, "V"));
		return list;
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

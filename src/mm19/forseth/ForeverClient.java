package mm19.forseth;

import java.util.ArrayList;

import org.json.JSONObject;

import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;

import mm19.objects.HitReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mm19.objects.ActionResult;

import mm19.objects.Ship;
import mm19.objects.ShipAction;

import mm19.objects.ShotResult;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

public class ForeverClient extends TestClient {
	
	private String token;
	
	private Ship mainShip;
	
	private int fireX = 0;
	private int fireY = 0;
	private int initialFireX = 0;
	private int initialFireY = 0;
	
	// all current ships
	private Ship[] ships;
	private Map<Integer, ShipAction> indexedPlannedShots;

	/**
	 * The number of bullets to unload on enemies we've detected.
	 */
	private static final int UNLOAD_BULLET_COUNT = 3;

	public ForeverClient() {
		super("ForsethAgain");
		placeShips();
	}

	@Override
	public JSONObject setup() {
		JSONObject obj = new JSONObject();
		obj.put("playerName", this.name);
		JSONObject jsonMainShip
		return null;
	}

	@Override
	public void processResponse(ServerResponse sr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		JSONObject turnObj = new JSONObject();
		token = sr.playerToken;
		ships = sr.ships;
		List<Ship> fireableShips = Arrays.asList(ships);
		List<HitReport> reports = Arrays.asList(sr.hitReport);
		Collection<JSONObject> actions = new ArrayList<JSONObject>();
		ShipAction specialAction = null;
		// TODO: Check for pings on us as well
		specialAction = moveShips(reports, fireableShips);
		if (specialAction == null) {
			Ship burster = selectBurstingShip(fireableShips);
			fireableShips.remove(burster);
			specialAction = fireBurst(burster);
		}
		List<ShotResult> shotResults = transformShotResults(Arrays.asList(sr.shipActionResults));
		indexedPlannedShots = new HashMap<Integer, ShipAction>();
		List<ShipAction> plannedShots = unloadArsenal(fireableShips, shotResults);
		addDiagonalShots(plannedShots, fireableShips);
		return translateToJSON(plannedShots, specialAction);
	}
	
	/**
	 * Translates our game plan to JSON.
	 * 
	 * @param plannedShots The shots that we plan on making.
	 * @param specialAction The special action that we plan on doing.
	 */
	private JSONObject translateToJSON(List<ShipAction> plannedShots, ShipAction specialAction) {
		JSONObject turnObj = new JSONObject();
		turnObj.put("PlayerKey", token);
		Collection<JSONObject> actions = new ArrayList<JSONObject>();
		for (ShipAction sa : plannedShots) {
			actions.add(sa.toJSONObject());
		}
		actions.add(specialAction.toJSONObject());
		turnObj.put("shipActions", actions);
		return turnObj;
	}

	@Override
	public void handleInterrupt(ServerResponse sr) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Plans diagonal shots to search for and destroy the enemy.
	 * 
	 * @param plannedShots The planned shots array. This will be modified to contain the
	 * newly-planned shots.
	 * @param fireableShips The ships that may still fire at the enemy.
	 */
	private void addDiagonalShots(List<ShipAction> plannedShots, List<Ship> fireableShips) {
		while (initialFireX < 100 && initialFireY < 100) {
			while (fireX < 100 && fireY < 100) {
				
				// fire:
				Ship toFire = fireableShips.remove(0);
				ShipAction sa = new ShipAction(toFire.ID);
				sa.actionID = ShipAction.Action.Fire;
				sa.actionX = fireX;
				sa.actionY = fireY;
				plannedShots.add(sa);
				
				fireY++;
				fireX++;
			}
			if (initialFireX < 100) {
				initialFireX += 6;
				fireX = initialFireX;
				fireY = initialFireY;
			} else {
				fireX = 0;
				initialFireY += 6;
				fireY = initialFireY;
			}
		}
	}
	
	/**
	 * Transforms all applicable ActionResults into ShotReports. Each ActionResult is checked 
	 * whether it is reporting a shot's result, and if so, converted into a ShotReport.
	 * 
	 * @param ars The action reports.
	 * @return the converted shot reports.
	 */
	private List<ShotResult> transformShotResults(List<ActionResult> actions) {
		List<ShotResult> shots = new ArrayList<ShotResult>();
		for (ActionResult ar : actions) {
			ShipAction sa = indexedPlannedShots.get(ar.ID);
			if (sa != null) {
				shots.add(new ShotResult(ar, sa.actionX, sa.actionY));
			}
		}
		return shots;
	}
	
	/**
	 * Selects a ship to do the burst ability. The first destroyer found is selected.
	 * 
	 * @param fireableShips The ships that may fire.
	 * @return The first destroyer found, or null if there is no destroyer.
	 */
	private Ship selectBurstingShip(List<Ship> fireableShips) {
		for (Ship s : fireableShips) {
			if (s.type == Ship.ShipType.Destroyer) {
				return s;
			}
		}
		return null;
	}
	
	private ArrayList<Ship> placeShips(){
	
		ArrayList<Ship> list = new ArrayList<Ship>();
		//(int i, int h, ShipType t, int x, int y, String o)
		mainShip = new Ship(0, 60, ShipType.Main, 79, 80, "V");
		list.add(mainShip);
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
			// IMMA FFIRE MY LAZORSES
		}
		
	}
	
	public ShipAction fireBurst(Ship s){
		ShipAction sa = new ShipAction(s.ID);
		sa.actionID = ShipAction.Action.BurstShot;
		sa.actionX = (int) Math.random() * 97 + 1;
		sa.actionY = (int) Math.random() * 97 + 1;
		return sa;
	}

	/**
	 * Dumps 3 shots on any ships we detected.
	 * 
	 * @param fireableShips All ships that can fire.
	 * @param results Results of shot actions.
	 * @return A list of the firing actions.
	 */
	private List<ShipAction> unloadArsenal(List<Ship> fireableShips, Collection<ShotResult> results) {
		List<ShipAction> fireActions = new ArrayList<ShipAction>();
		for (ShotResult sr : results) {
			if (fireableShips.size() > UNLOAD_BULLET_COUNT) {
				if (sr.result.equals("S")) {
					for (int i = 0; i < UNLOAD_BULLET_COUNT; i++) {
						if (fireableShips.size() == 0) {
							return fireActions;
						}
						int id = (fireableShips.remove(0)).ID;
						ShipAction sa = new ShipAction(id);
						sa.actionID = ShipAction.Action.Fire;
						sa.actionX = sr.x;
						sa.actionY = sr.y;
						indexedPlannedShots.put(sa.shipID, sa);
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

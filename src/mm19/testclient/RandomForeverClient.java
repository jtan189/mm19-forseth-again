package mm19.testclient;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mm19.objects.ActionResult;
import mm19.objects.HitReport;
import mm19.objects.PingReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import mm19.objects.ShipAction;
import mm19.objects.ShotResult;
import mm19.response.ServerResponse;

import org.json.JSONObject;

public class RandomForeverClient extends TestClient {

	private String token;
	public static final int MAP_WIDTH = 100;
	public static final int MAP_HEIGHT = 100;

	public static final int MOVE_COST_PILOT = 100;
	public static final int MOVE_COST_DESTROY = 200;
	public static final int MOVE_COST_MAIN = 250;

	public static final int FIRE_COST = 50;
	public static final int BURST_COST = 250;



	private Ship mainShip;

	private int resources = 0;

	private int usedResources = 0;

	// all current ships
	private Ship[] ships;
	// TODO: optimize this later
	private Map<Integer, ShipAction> indexedPlannedShots;
	private ServerResponse lastResponse;

	/**
	 * The number of bullets to unload on enemies we've detected.
	 */
	private static final int UNLOAD_BULLET_COUNT = 3;

	public RandomForeverClient(String name) {
		super(name);
	}

	@Override
	public JSONObject setup() {
		JSONObject obj = new JSONObject();
		ArrayList<Ship> placedShips = placeShips();
		obj.put("playerName", this.name);
		obj.put("mainShip", mainShip.toMainJSONObject());
		Collection<JSONObject> jsonShips = new ArrayList<JSONObject>();
		for (Ship s : placedShips) {
			jsonShips.add(s.toJSONObject());
		}
		obj.put("ships", jsonShips);
		return obj;
	}

	@Override
	public void processResponse(ServerResponse sr) {
		lastResponse = sr;
		resources = sr.resources;
	}

	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		turn++;
		usedResources = 0;
		JSONObject turnObj = new JSONObject();
		token = sr.playerToken;
		ships = sr.ships;

		List<Ship> fireableShips = new ArrayList<Ship>(Arrays.asList(ships));
		Collection<JSONObject> actions = new ArrayList<JSONObject>();
		ShipAction specialAction = null;

		// TODO: Check for pings on us as well
		if (lastResponse != null) { // spend check is in moveShip
			List<HitReport> hits = Arrays.asList(sr.hitReport);
			List<PingReport> pings = Arrays.asList(sr.pingReport);
			List<Ship> potentialHits = detectShipHits(hits, pings, fireableShips);
			specialAction = moveShip(potentialHits, fireableShips);
			// cost of moving is subtracted within moveShips()
		}
		if (specialAction == null && canSpend(BURST_COST)) {
			Ship burster = selectBurstingShip(fireableShips);
			fireableShips.remove(burster);
			specialAction = fireBurst(burster);
			spend(BURST_COST);
		}

		indexedPlannedShots = new HashMap<Integer, ShipAction>();
		List<ShipAction> plannedShots;
		if (lastResponse != null) {

			List<HitReport> oldHits = new ArrayList<HitReport>(Arrays.asList(lastResponse.hitReport));
			plannedShots = unloadArsenal(fireableShips, oldHits);
		} else {
			plannedShots = new ArrayList<ShipAction>();
		}


		if (!fireableShips.isEmpty()) {
			addDiagonalShots(plannedShots, fireableShips);
		}
		
//		// testing
//		ShipAction specialAction = new ShipAction(mainShip.ID, mainShip.xCoord + 1, mainShip.yCoord + 1,Action.MoveV, -1);
//		mainShip.move(mainShip.xCoord + 1, mainShip.yCoord + 1);
//		List<ShipAction> plannedShots = new ArrayList<ShipAction>();
		
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
		if (specialAction != null) {
			actions.add(specialAction.toJSONObject());
		}
		turnObj.put("shipActions", actions);
		return turnObj;
	}

	/**
	 * Returns resources we can spend.
	 */
	private int availableResources() {
		return (resources - usedResources);
	}

	/**
	 * Returns whether we can spend the given amount of resources.
	 * 
	 * @param amount The amount to check for.
	 */
	private boolean canSpend(int amount) {
		return (availableResources() >= amount + MOVE_COST_MAIN);
	}

	/**
	 * Marks the amount of resources as spent.
	 * 
	 * @param amount The amount of resources to spend.
	 */
	private void spend(int amount) {
		usedResources += amount;
	}

	@Override
	public void handleInterrupt(ServerResponse sr) {
		System.err.println("Your data is incorrect, and my worldview has been shattered. I must die now.");
	}

	/**
	 * Plans diagonal shots to search for and destroy the enemy.
	 * 
	 * @param plannedShots The planned shots array. This will be modified to contain the
	 * newly-planned shots.
	 * @param fireableShips The ships that may still fire at the enemy.
	 */
	private void addDiagonalShots(List<ShipAction> plannedShots, List<Ship> fireableShips) {
		
		if (fireableShips.isEmpty() || !canSpend(FIRE_COST)) {
			return;
		} else {
			// fire:
			Ship toFire = fireableShips.remove(0);
			ShipAction sa = new ShipAction(toFire.ID);
			sa.actionID = ShipAction.Action.Fire;
			sa.actionX = (int) (Math.random() * 99);
			sa.actionY = (int) (Math.random() * 99);
			plannedShots.add(sa);
			spend(FIRE_COST);
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
		// do NOT add mainship because we add it seperate from other ships during game start
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

	private int numDestroyersLeft(List<Ship> myShips) {
		int count = 0;
		for (Ship ship : myShips) {
			if (ship.type == ShipType.Destroyer) {
				count++;
			}
		}
		return count;
	}
	
	int turn = 0;
	
	/**
	 * Detects which ships were hit.
	 * 
	 * @param hits The hit reports.
	 * @param pings The ping reports.
	 * @param ships The current ships.
	 * @return The ships that were hit, in order of priority.
	 */
	private List<Ship> detectShipHits(List<HitReport> hits, List<PingReport> pings, List<Ship> ships) {
		List<Ship> allHits = new ArrayList<Ship>();
		Ship mainHit = null;
		Ship mainPing = null;
		Ship pilotHit = null;
		Ship pilotPing = null;
		Ship destHit = null;
		Ship destPing = null;
		for (Ship s : ships) {
			for (HitReport hr : hits) {
				if (s.contains(new Point(hr.xCoord, hr.yCoord))) {
					switch (s.type) {
					case Main:
						if (mainHit == null || mainHit.health > s.health) {
							mainHit = s;
						}
						break;
						
					case Destroyer:
						if (destHit == null || destHit.health > s.health) {
							destHit = s;
						}
						break;
						
					case Pilot:
						if (pilotHit == null || pilotHit.health > s.health) {
							pilotHit = s;
						}
						break;
					}
				}
			}
			for (PingReport pr : pings) {
				if (s.ID == pr.shipID) {
					switch (s.type) {
					case Main:
						if (mainPing == null || mainPing.health > s.health) {
							mainPing = s;
						}
						break;
						
					case Destroyer:
						if (destPing == null || destPing.health > s.health) {
							destPing = s;
						}
						break;
						
					case Pilot:
						if (pilotPing == null || pilotPing.health > s.health) {
							pilotPing = s;
						}
						break;
					}
				}
			}
			if (mainHit != null) {
				allHits.add(mainHit);
			}
			if (pilotHit != null) {
				allHits.add(pilotHit);
			}
			if (pilotPing != null && pilotPing != pilotHit) {
				allHits.add(pilotPing);
			}
			if (destHit != null) {
				allHits.add(destHit);
			}
			if (destPing != null && destPing != destHit) {
				allHits.add(destPing);
			}
		}
		return allHits;
	}

	/**
	 * Move hit ships, if they can be moved.
	 * 
	 * @param hitShips A priority list of ships to move. Highest priority is at the
	 * top of the list.
	 * @param fireableShips All the ships, to check for collisions in. The hit ship is
	 * removed from this.
	 * @return The ShipAction if a ship is moved, or null otherwise.
	 */
	private ShipAction moveShip(List<Ship> hitShips, List<Ship> fireableShips) {
		Ship toMove = null;
		for (Ship s : hitShips) {
			switch (s.type) {
			case Main:
				if (availableResources() >= MOVE_COST_MAIN) {
					spend(MOVE_COST_MAIN);
					toMove = s;
					break;
				}
				break;
				
			case Pilot:
				if (availableResources() >= MOVE_COST_PILOT) {
					spend(MOVE_COST_PILOT);
					toMove = s;
					break;
				}
				break;
				
			case Destroyer:
				if (availableResources() >= MOVE_COST_DESTROY) {
					spend(MOVE_COST_DESTROY);
					toMove = s;
					break;
				}
				break;
			}
		}
		
		if (toMove == null) {
			return null;
		}
		
		fireableShips.remove(toMove);
		
		ShipAction movement = new ShipAction(toMove.ID);
		boolean horz = Math.random() > 0.5;
		movement.actionID = (horz) ? ShipAction.Action.MoveH : ShipAction.Action.MoveV;
		
		java.util.Random rng = new java.util.Random();
		// keep going until we find a clear space:
		while (true) {
			int x, y;
			if (horz) {
				x = rng.nextInt(100 - (toMove.width - 1));
				y = rng.nextInt(100);
			} else {
				y = rng.nextInt(100 - (toMove.width - 1));
				x = rng.nextInt(100);
			}
			boolean collides = false;
			for (Ship s : fireableShips) {
				if (s.contains(new Point(x, y))) {
					collides = true;
					break;
				}
			}
			movement.actionX = x;
			movement.actionY = y;
			if (!collides) {
				break;
			}
		}
		return movement;
	}

	public ShipAction fireBurst(Ship s){
		ShipAction sa = new ShipAction(s.ID);
		sa.actionID = ShipAction.Action.BurstShot;
		sa.actionX = (int) (Math.random() * 97 + 1);
		sa.actionY = (int) (Math.random() * 97 + 1);
		return sa;
	}

	/**
	 * Dumps 3 shots on any ships we detected.
	 * 
	 * @param fireableShips All ships that can fire.
	 * @param results Results of shot actions.
	 * @return A list of the firing actions.
	 */
	private List<ShipAction> unloadArsenal(List<Ship> fireableShips, Collection<HitReport> hits) {
		List<ShipAction> fireActions = new ArrayList<ShipAction>();
		for (HitReport hr : hits) {
			if (fireableShips.size() > UNLOAD_BULLET_COUNT) {
				if (hr.hit) {
					for (int i = 0; i < UNLOAD_BULLET_COUNT; i++) {
						if (fireableShips.size() == 0) {
							return fireActions;
						}
						if (canSpend(FIRE_COST) ) {
							int id = (fireableShips.remove(0)).ID;
							ShipAction sa = new ShipAction(id);
							sa.actionID = ShipAction.Action.Fire;
							sa.actionX = hr.xCoord;
							sa.actionY = hr.yCoord;
							indexedPlannedShots.put(sa.shipID, sa);
							fireActions.add(sa);
							
							// subtract cost
							spend(FIRE_COST);
						}
					}
				}
			} else {
				break;
			}
		}
		return fireActions;
	}

}
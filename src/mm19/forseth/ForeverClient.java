package mm19.forseth;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mm19.objects.ActionResult;
import mm19.objects.HitReport;
import mm19.objects.PingReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import mm19.objects.ShipAction;
import mm19.objects.ShotResult;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

import org.json.JSONObject;

public class ForeverClient extends TestClient {

	private String token;
	public static final int MAP_WIDTH = 100;
	public static final int MAP_HEIGHT = 100;

	public static final int MOVE_COST_PILOT = 100;
	public static final int MOVE_COST_DESTROY = 200;
	public static final int MOVE_COST_MAIN = 250;

	public static final int FIRE_COST = 50;
	public static final int BURST_COST = 250;

	public static final int DELTA_NEAR_MAIN = 5;


	private Ship mainShip;

	private int fireX = 0;
	private int fireY = 0;
	private int initialFireX = 0;
	private int initialFireY = 0;
	private int loopsCompleted = 0;

	private int resources = 0;

	private int usedResources = 0;
	
	private Deque<Point> lowPoints = new ArrayDeque<Point>();
	
	private Deque<Point> highPoints = new ArrayDeque<Point>();
	
	private Point prevPredLow = null;
	
	private Point prevPredHigh = null;
	
	private int predictionsRight = 0;
	
	private int predictionChances = 0;
	
	private double accuracy = 0.0;

	// all current ships
	private Ship[] ships;
	// TODO: optimize this later
	private Map<Integer, ShipAction> indexedPlannedShots;
	private ServerResponse lastResponse;
	private Point predLow = null;
	private Point predHigh = null;
	private int numPreds = 0;

	/**
	 * The number of bullets to unload on enemies we've detected.
	 */
	private static final int UNLOAD_BULLET_COUNT = 5;

	public ForeverClient(String name) {
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
		usedResources = 0;
//		System.out.println(availableResources() + "R: ");
		JSONObject turnObj = new JSONObject();
		token = sr.playerToken;
		ships = sr.ships;

		List<Ship> fireableShips = new ArrayList<Ship>(Arrays.asList(ships));
		Collection<JSONObject> actions = new ArrayList<JSONObject>();
		ShipAction specialAction = null;

		if (lastResponse != null) { // spend check is in moveShip
			List<HitReport> hits = Arrays.asList(sr.hitReport);
			List<PingReport> pings = Arrays.asList(sr.pingReport);
			predict(hits);
			List<Ship> potentialHits = detectShipHits(hits, pings, fireableShips);
			specialAction = moveShip(potentialHits, fireableShips);
			// cost of moving is subtracted within moveShips()
		}
		if (specialAction == null && canSpend(BURST_COST)) {
			Ship burster = selectBurstingShip(fireableShips);
			if (burster != null) {
				fireableShips.remove(burster);
				specialAction = fireBurst(burster);
				spend(BURST_COST);
			}
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

	private void predict(List<HitReport> hits) {
		int min = 201;
		Point minP = null;
		int max = 0;
		Point maxP = null;
		for (HitReport hr : hits) {
			Point p = new Point(hr.xCoord, hr.yCoord);
			
			int norm = p.x + p.y;
			
			if (norm < min) {
				min = norm;
				minP = p;
			}
			if (norm > max) {
				max = norm;
				maxP = p;
			}
		}
		if (hits.size() > 0) {
			lowPoints.add(minP);
			highPoints.add(maxP);
			if (lowPoints.size() == 3) { // implies high is 3
				if (predHigh != null) {
					prevPredHigh = predHigh;
				}
				if (predLow != null) {
					prevPredLow = predLow;
				}
				Point p1 = lowPoints.remove();
				Point p2 = lowPoints.remove();
				Point p3 = lowPoints.remove();
				lowPoints.add(p2);
				lowPoints.add(p3);
				predLow = new Point(0, 0);
				predLow.x = (p1.x - p2.x) + ((p1.x - p2.x) - (p2.x - p3.x)); 
				predLow.y = (p1.y - p2.y) + ((p1.y - p2.y) - (p2.y - p3.y));
				Point ph1 = highPoints.remove();
				Point ph2 = highPoints.remove();
				Point ph3 = highPoints.remove();
				highPoints.add(ph2);
				highPoints.add(ph3);
				predHigh = new Point(0, 0);
				predHigh.x = (ph1.x - ph2.x) + ((ph1.x - ph2.x) - (ph2.x - ph3.x)); 
				predHigh.y = (ph1.y - ph2.y) + ((ph1.y - ph2.y) - (ph2.y - ph3.y));
				numPreds ++;
			}
		}
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
		initialFireX += loopsCompleted;
		while (initialFireX < 100 || initialFireY < 100) {
			while (fireX < 100 && fireY < 100) {
				if (fireableShips.isEmpty() || !canSpend(FIRE_COST)) {
					return;
				} else {
					// fire:
					Ship toFire = fireableShips.remove(0);
					ShipAction sa = new ShipAction(toFire.ID);
					sa.actionID = ShipAction.Action.Fire;
					sa.actionX = fireX;
					sa.actionY = fireY;
					plannedShots.add(sa);
					spend(FIRE_COST);
					fireY++;
					fireX++;
				}
			}
			if (initialFireX < 100) { // < 99
				initialFireX += 5;
				fireX = initialFireX;
				fireY = initialFireY;
				if (initialFireX >= 100){ // changed
					initialFireY += loopsCompleted;
				}
			} else {
				fireX = 0;
				initialFireY += 5;
				fireY = initialFireY;
				if (initialFireY >= 100){ // changed
					loopsCompleted++;
					initialFireX = 0;
					initialFireY = 0;
				}
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
		Ship mainInDanger = null;
		Ship predictDanger = null;
		for (Ship s : ships) {
			// get area around main ship
			Rectangle mainOutline = s.asRect();
			Rectangle mainProximity = new Rectangle(mainOutline.x - DELTA_NEAR_MAIN, mainOutline.y - DELTA_NEAR_MAIN,
					mainOutline.width + (2 * DELTA_NEAR_MAIN), mainOutline.height + (2 * DELTA_NEAR_MAIN));

			for (HitReport hr : hits) {
//				System.out.println(hr);
				if (s.contains(new Point(hr.xCoord, hr.yCoord))) {
					//JOptionPane.showConfirmDialog(null, "Collision at ("+s.xCoord+", "+s.yCoord+") on turn " + turn);
					switch (s.type) {
					case Main:
						if (mainHit == null || mainHit.health > s.health) {
							mainHit = s;
//							System.out.println("A main has been hit!");
						}
						break;

					case Destroyer:
						if (destHit == null || destHit.health > s.health) {
							destHit = s;
//							System.out.println("A destroyer has been hit!");
						}
						break;

					case Pilot:
						if (pilotHit == null || pilotHit.health > s.health) {
							pilotHit = s;
//							System.out.println("A pilot has been hit!");
						}
						break;
					}
				} else if (s.type == Ship.ShipType.Main && mainProximity.contains(new Point(hr.xCoord, hr.yCoord))) {
					mainInDanger = s;
//					System.out.println("A main is in danger!");
				}
			}
			for (PingReport pr : pings) {
				if (s.ID == pr.shipID) {
					switch (s.type) {
					case Main:
						if (mainPing == null || mainPing.health > s.health) {
							mainPing = s;
//							System.out.println("A main has been pinged!");
						}
						break;

					case Destroyer:
						if (destPing == null || destPing.health > s.health) {
							destPing = s;
//							System.out.println("A destroyer has been pinged!");
						}
						break;

					case Pilot:
						if (pilotPing == null || pilotPing.health > s.health) {
							pilotPing = s;
//							System.out.println("A pilot has been pinged!");
						}
						break;
					}
				}
			}
			Point shipLoc = new Point(s.xCoord, s.yCoord);
			if ((shipLoc.equals(predLow) || shipLoc.equals(predHigh)) && predIsReliable()) {
				predictDanger = s;
			}
		}
		boolean added = false;
		if (mainHit != null) {
			allHits.add(mainHit);
			added = true;
		}
		if (mainPing != null && mainPing != mainHit) {
			allHits.add(mainHit);
			added = true;
		}
		// no pilot ping 
		if (pilotHit != null) {
			allHits.add(pilotHit);
			added = true;
		}
		if (pilotPing != null && pilotPing != pilotHit) {
			allHits.add(pilotPing);
			added = true;
		}
		if (destHit != null) {
			allHits.add(destHit);
			added = true;
		}
		if (destPing != null && destPing != destHit) {
			allHits.add(destPing);
			added = true;
		}
		// if main has not been hit, but there have been near-hits or near-pings, add it to the move list
		// (with low priority, i.e. if others were hit they take priority)
		if (mainInDanger != mainHit && mainInDanger != mainPing && mainInDanger != null) {
			allHits.add(mainInDanger);
			added = true;
		}
		if (predictDanger != null && !added) {
			allHits.add(predictDanger);
		}
//		System.out.println("Finished checking hits");
		return allHits;
	}

	private boolean predIsReliable() {
		Point tErr = new Point();
		tErr.x = Math.abs(lowPoints.peekLast().x - prevPredLow.x);
		tErr.y = Math.abs(lowPoints.peekLast().y - prevPredLow.y);
		double avgX, avgY;
		avgX = tErr.x / (double) numPreds;
		avgY = tErr.y / (double) numPreds;
		double totalErrorLow = avgX + avgY;
		Point tErr2 = new Point();
		tErr2.x = Math.abs(highPoints.peekLast().x - prevPredHigh.x);
		tErr2.y = Math.abs(highPoints.peekLast().y - prevPredHigh.y);
		double avgX2, avgY2;
		avgX2 = tErr2.x / (double) numPreds;
		avgY2 = tErr2.y / (double) numPreds;
		double totalErrorLow2 = avgX2 + avgY2;
		return !(totalErrorLow > 10 || totalErrorLow2 > 10);
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
		boolean breakOut = false;
		for (Ship s : hitShips) {
			switch (s.type) {
			case Main:
				if (availableResources() >= MOVE_COST_MAIN) {
					spend(MOVE_COST_MAIN);
					toMove = s;
					breakOut = true;
//					System.out.println("We'll move the main.");
				}
				break;

			case Pilot:
				if (availableResources() >= MOVE_COST_PILOT) {
					spend(MOVE_COST_PILOT);
					toMove = s;
					breakOut = true;
//					System.out.println("We'll move the pilot.");
				}
				break;

			case Destroyer:
				if (availableResources() >= MOVE_COST_DESTROY) {
					spend(MOVE_COST_DESTROY);
					toMove = s;
					breakOut = true;
//					System.out.println("We'll move the destroyer.");
				}
				break;
			}
			if (breakOut) {
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
//							System.out.println("..Added one burst attack..:" + i);

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

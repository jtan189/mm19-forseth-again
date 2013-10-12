package mm19.testclient.alex;

import java.util.ArrayList;
import java.util.Collection;

import mm19.objects.HitReport;
import mm19.objects.PingReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import mm19.objects.ShipAction;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains logic for Alex's test client, feel free to disregard any
 * logic you don't agree with. I'll see you on the battle field. >:)
 * 
 * @author Flewp
 *
 */
public class TestClientAlex extends TestClient{
	
	int resources;
	int pingX;
	int pingY;
	boolean pingHit;
	String playerToken;
	
	public TestClientAlex(String name) {
		super(name);
	}
	
	/**
	 * Making my ships impossibly hard to find.
	 */
	@Override
	public JSONObject setup() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("playerName", this.name);
			
			JSONObject mainShip = new JSONObject();
			mainShip.put("xCoord", 5);
			mainShip.put("yCoord", 5);
			mainShip.put("orientation", "H");
			obj.put("mainShip", mainShip);
			
			Collection<JSONObject> ships = new ArrayList<JSONObject>();
			for(int i = 0; i < 4; i++) {
				JSONObject ship = new JSONObject();
				
				if(i % 2 == 0) {
					ship.put("type", "D");
					ship.put("orientation", "H");
				} else {
					ship.put("type", "P");
					ship.put("orientation", "V");
				}
				
				int xCoord = (int) (Math.random()*90 + 5);
				int yCoord = (int) ((Math.random()*5) + ((i+1) * TestClient.BOARD_WIDTH/6));
				ship.put("xCoord", xCoord);
				ship.put("yCoord", yCoord);
				ships.add(ship);
			}
			
			obj.put("ships", ships);
		}
		catch(JSONException e) {
			
		}
		
		return obj;
	}
	
	/**
	 * Process my response in O(n^2) like a boss.
	 */
	@Override
	public void processResponse(ServerResponse sr) {
		
		for(HitReport hr : sr.hitReport) {
			if(hr.hit) {
				System.out.println("I hit something...? :)");
			}
		}
		
		if(sr.pingReport.length > 0) {
			pingHit = true;
			System.out.println("Pinged!");
		}
	}

	/**
	 * Preparing my incredible offense
	 */
	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		JSONObject turnObj = new JSONObject();
		try {
			Collection<JSONObject> actions = new ArrayList<JSONObject>();
			boolean pinged = false;
			
			for(PingReport pr : sr.pingReport) {
				System.out.println("Ship " + pr.shipID + " got pinged :(");
			}
			
			for(Ship ship : sr.ships) {
				int xCoord = (int) (Math.random()*10);
				int yCoord = (int) (Math.random()*10);
				
				ShipAction tempAction = null;
				
				if(ship.type == ShipType.Pilot && !pinged && !pingHit) {
					tempAction = new ShipAction(ship.ID, xCoord, yCoord, ShipAction.Action.Sonar, 0);
					pingX = xCoord;
					pingY = yCoord;
					pinged = true;
					//System.out.println("Trying to ping at (" + xCoord + "," + yCoord + ")");
				} else if(ship.type == ShipType.Destroyer && pingHit) {
					tempAction = new ShipAction(ship.ID, xCoord, yCoord, ShipAction.Action.Fire, 0);
					pingHit = false;
					//System.out.println("Sending Burst Shot >:)");
				}
				
				if(tempAction != null) {
					actions.add(tempAction.toJSONObject());
				}
			}
			turnObj.put("playerToken", playerToken);
			turnObj.put("shipActions", actions);
			return turnObj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return turnObj;
	}

	/**
	 * wat
	 */
	@Override
	public void handleInterrupt(ServerResponse sr) {
		System.out.println("wat");
	}
	
}

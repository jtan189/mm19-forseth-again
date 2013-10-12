package mm19.testclient.dummy;

import java.util.ArrayList;
import java.util.Collection;

import mm19.objects.HitReport;
import mm19.objects.PingReport;
import mm19.objects.Ship;
import mm19.objects.ShipAction;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

import org.json.JSONException;
import org.json.JSONObject;

public class TestClientDummy extends TestClient {

	private Ship[] ships;
	String playerToken;
	
	int currX;
	int currY;
	
	public TestClientDummy(String name) {
		super(name);
	}

	@Override
	public JSONObject setup() {
		
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("playerName", this.name);
			
			JSONObject mainShip = new JSONObject();
			mainShip.put("xCoord", 4);
			mainShip.put("yCoord", 0);
			mainShip.put("orientation", "H");
			obj.put("mainShip", mainShip);
			
			Collection<JSONObject> ships = new ArrayList<JSONObject>();
			for(int i = 0; i < 4; i++) {
				JSONObject ship = new JSONObject();
				
				if(i < 2) {
					ship.put("type", "D");
					ship.put("orientation", "H");
					ship.put("xCoord", 3);
				} else {
					ship.put("type", "P");
					ship.put("orientation", "H");
					ship.put("xCoord", 1);
				}
				
				ship.put("yCoord", i+1);
				ships.add(ship);
			}
			
			obj.put("ships", ships);
		}
		catch(JSONException e) {
			
		}
		
		return obj;
	}

	@Override
	public void processResponse(ServerResponse sr) {
		for(HitReport hr : sr.hitReport) {
			if(hr.hit) {
				System.out.println("I hit something...? :)");
			}
		}
		for(int i = 0; i < sr.ships.length; i++) {
			Ship currResponseShip = sr.ships[i];
			
			for(int j = 0; j < ships.length; j++) {
				Ship currShip = ships[j];
				
				// If we're talking about the same boat...
				if(currShip.ID == currResponseShip.ID) {
					
					currShip.health = currResponseShip.health;
					
					// You sunk my battleship!
					if(currShip.health <= 0) {
						System.out.println("You sunk my battleship! :)");
					}
					break;
				}
			}
		}
	}

	/**
	 * Attack the top 5x5 corner of the board. LOL
	 */
	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		JSONObject turnObj = new JSONObject();
		ships = sr.ships;
		try {
			for(PingReport pr : sr.pingReport) {
				System.out.println("Ship " + pr.shipID + " got pinged :(");
			}
			
			ShipAction tempAction;
			Collection<JSONObject> actions = new ArrayList<JSONObject>();
			for(Ship s : ships) {
				if(s.type == Ship.ShipType.Destroyer) {
					 tempAction = new ShipAction(s.ID, currX++, currY, ShipAction.Action.Fire, 0);
					 actions.add(tempAction.toJSONObject());
				}
				
				if(currX > 5) {
					currY++;
					currX = 0;
				}
				
				if(currY > 5) {
					currY = 0;
				}
			}
			
			turnObj.put("shipActions", actions);
			turnObj.put("playerToken", playerToken);
			
			return turnObj;
		}
		catch(JSONException e) {
			
		}
		
		return turnObj;
	}

	@Override
	public void handleInterrupt(ServerResponse sr) {
		System.out.println("wat");
	}

}

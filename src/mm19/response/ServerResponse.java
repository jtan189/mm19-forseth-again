package mm19.response;

import javax.swing.JOptionPane;

import mm19.objects.ActionResult;
import mm19.objects.HitReport;
import mm19.objects.PingReport;
import mm19.objects.Ship;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerResponse {

    public int responseCode;
    public String[] error;

    public String playerToken;
    public String playerName;

    public int resources;
    public Ship[] ships;
    public ActionResult[] shipActionResults;
    public HitReport[] hitReport;
    public PingReport[] pingReport;

    public ServerResponse(JSONObject obj) throws ServerResponseException {
        try {
            if(!obj.has("responseCode")) {
                throw new ServerResponseException("Server does not have a responseCode object.\n" + obj);
            }
            if(obj.getInt("responseCode") == -1) {
                System.out.print("you lose");
                System.exit(0);
            }
            if(obj.getInt("responseCode") == 9001) {
                System.out.print("you win");
                System.exit(0);
            }

            if(!obj.has("error")) {
                throw new ServerResponseException("Server does not have an error object.\n" + obj);
            }
            if(!obj.has("playerToken")) {
                throw new ServerResponseException("Server does not have a token object.\n" + obj);
            }
            if(!obj.has("playerName")) {
                throw new ServerResponseException("Server does not have a playerName object.\n" + obj);
            }
            if(!obj.has("ships")) {
                throw new ServerResponseException("Server does not have a ships object.\n" + obj);
            }
            if(!obj.has("shipActionResults")) {
                throw new ServerResponseException("Server does not have a shipActionResults object.\n" + obj);
            }
            if(!obj.has("hitReport")) {
                throw new ServerResponseException("Server does not have a hitReport object.\n" + obj);
            }
            if(!obj.has("pingReport")) {
                throw new ServerResponseException("Server does not have a pingReport object.\n" + obj);
            }

            // Getting responseCode
            responseCode = obj.getInt("responseCode");

        	if(obj.has("resources")) {
                resources = obj.getInt("resources");
        	}
            // Getting error
            JSONArray err = obj.getJSONArray("error");
            error = new String[err.length()];
            
            for(int i = 0; i < err.length(); i++) {
                error[i] = err.getString(i);
            }
            // Getting playerToken
            playerToken = obj.getString("playerToken");

            // Getting playerName
            playerName = obj.getString("playerName");
            




            // Getting ships
            JSONArray shipsObj = obj.getJSONArray("ships");

            ships = new Ship[shipsObj.length()];

            for(int i = 0; i < ships.length; i++) {
                ships[i] = new Ship(shipsObj.getJSONObject(i));
            }

            // Getting shipActionResults
            JSONArray shipActionResultsObj = obj.getJSONArray("shipActionResults");

            shipActionResults = new ActionResult[shipActionResultsObj.length()];

            for(int i = 0; i < shipActionResults.length; i++) {
                shipActionResults[i] = new ActionResult(shipActionResultsObj.getJSONObject(i));
            }

            // Getting hitReport
            JSONArray hitReportObj = obj.getJSONArray("hitReport");

            hitReport = new HitReport[hitReportObj.length()];

            for(int i = 0; i < hitReport.length; i++) {
                hitReport[i] = new HitReport(hitReportObj.getJSONObject(i));
            }

            // Getting pingReport
            JSONArray pingReportObj = obj.getJSONArray("pingReport");

            pingReport = new PingReport[pingReportObj.length()];

            for(int i = 0; i < pingReport.length; i++) {
                pingReport[i] = new PingReport(pingReportObj.getJSONObject(i));
            }

        }
        catch(JSONException e) {
            e.printStackTrace();
            throw new ServerResponseException("Error with JSON?!");
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- BEGIN SERVER RESPONSE ---\n");

        sb.append("responseCode: " + responseCode + '\n');

        sb.append("error: \n");
        for(int i = 0; i < error.length; i++) {
            sb.append('\t' + error[i] + '\n');
        }

        sb.append("playerToken: " + playerToken + '\n');
        sb.append("playerName: " + playerName + '\n');

        sb.append("ships: \n");
        for(int i = 0; i < ships.length; i++) {
            sb.append(ships[i].toString());
        }

        sb.append("shipActionResults: \n");
        for(int i = 0; i < shipActionResults.length; i++) {
            sb.append('\t' + shipActionResults[i].toString() + '\n');
        }

        sb.append("hitReport: \n");
        for(int i = 0; i < hitReport.length; i++) {
            sb.append('\t' + hitReport[i].toString() + '\n');
        }

        sb.append("pingReport: \n");
        for(int i = 0; i < pingReport.length; i++) {
            sb.append('\t' + pingReport[i].toString() + '\n');
        }

        sb.append("--- END SERVER RESPONSE ---\n");

        return sb.toString();
    }
}

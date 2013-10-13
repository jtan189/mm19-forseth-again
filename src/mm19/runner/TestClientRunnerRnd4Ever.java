package mm19.runner;

import mm19.testclient.RandomForeverClient;
import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;

public class TestClientRunnerRnd4Ever {

    public static void main(String args[]) {
        String name = "Forseth Random Forever";
        if(args.length >= 1) {
            name = args[0];
        }
        TestClient tc1 = new RandomForeverClient(name);

        try {
            tc1.connect();

        }
        catch(TestClientException e) {
            e.printStackTrace();
        }
    }

}

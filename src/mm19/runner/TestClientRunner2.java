package mm19.runner;

import mm19.forseth.ForeverClient2;
import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;

public class TestClientRunner2 {

    public static void main(String args[]) {
        String name = "Forseth Never";
        if(args.length >= 1) {
            name = args[0];
        }
        TestClient tc1 = new ForeverClient2(name);

        try {
            tc1.connect();

        }
        catch(TestClientException e) {
            e.printStackTrace();
        }
    }

}

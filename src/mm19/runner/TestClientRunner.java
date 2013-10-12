package mm19.runner;

import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;
import mm19.testclient.sam.TestClientSam;

public class TestClientRunner {

    public static void main(String args[]) {
        String name = "Give_me_a_name!";
        if(args.length >= 1) {
            name = args[0];
        }
        TestClient tc1 = new TestClientSam(name);

        try {
            tc1.connect();

        }
        catch(TestClientException e) {
            e.printStackTrace();
        }
    }

}

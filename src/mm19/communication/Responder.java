package mm19.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import mm19.testclient.TestClient;

public class Responder {
	private Socket serverSocket;
	
	public Responder(Socket s) {
		serverSocket = s;
	}
	
	public boolean sendResponse(String message) {
		PrintWriter out;
		try {
			out = new PrintWriter(serverSocket.getOutputStream(), true);
			out.flush();
			out.println(message);
			System.out.println("Sent turn! " + TestClient.TURN++);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}

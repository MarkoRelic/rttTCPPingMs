import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class PingCatcher {
	// Bind address and port from main program
	private String bindAddress;
	private Integer port;
	
	// Creating a socket and accept connections
	private ServerSocket serverSocket;
	private Socket socket;
	
	// Receive and send messages
	private BufferedReader fromPitcher;
	private PrintWriter toPitcher;
	
	// Received message, message number and size
	private String receivedFromPitcher;
	private Integer msgNumberReceived;
	private Integer messageSize;
	
	private Long endPitcherTime;
	
	// Character that will fill a response message
	private char fillMessage = '#';
	
	// Constructor 
	public PingCatcher(String bindAddress, Integer port) {
		this.bindAddress = bindAddress;
		this.port = port;
		
    	System.out.println("PingCatcher created! (bind address: " + 
							this.bindAddress + 
							", port: " + 
							this.port + 
							")");
    	
    	// Try to create socket at specific port
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Catch Ctrl+c
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() { 
	        	printAfterCtrlC();
	        }
	    });
	}

	// Start listening for connections from Pitcher and answer on msgs
	public void start() throws Exception {
		try {
			System.out.println("Waiting for a pitcher ...");

			// Listen for connection to this socket and accepts it
			socket = serverSocket.accept();
		
			// Checking if the connection was established on the socket
			if (socket.isConnected()) {
				System.out.println("Pitcher connected on port " + port + " (" + socket.getInetAddress() + ")");
			}
			
			// Open input and output streams
			fromPitcher = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			toPitcher = new PrintWriter(socket.getOutputStream(), true);
			
			while (true) {
			    try {
			    	// Wait for message from Pitcher
			    	receivedFromPitcher = fromPitcher.readLine();

			    	// Get time when message arrived from Pitcher
			    	endPitcherTime = System.currentTimeMillis();
			    	
			    	// Extract msg number and size from received message
			    	checkReceivedMsg(receivedFromPitcher);
			    	TCPPing.verbosePrint(msgNumberReceived + ": endPitcherTime in C:   " + endPitcherTime);
			    	
			    	if (TCPPing.debug) TCPPing.verbosePrint("Received: " + receivedFromPitcher + ": " + messageSize);
			    	
			    	// Generate response to Pitcher
			    	String msg = generateMessage(messageSize);
			    	
			    	// Calculate time between receiving msg and sending response
			    	Long startCatcherTime = System.currentTimeMillis();
			    	
			    	TCPPing.verbosePrint(msgNumberReceived + ": startCatcherTime in C: " + startCatcherTime);
			    	// Send response to Pitcher
			    	String msg2 = msg.substring(0, messageSize - startCatcherTime.toString().length()) + startCatcherTime.toString();
			    	toPitcher.println(msg2);
			    	toPitcher.flush();

			    } catch (SocketTimeoutException | SocketException e) {
			    	System.out.println("Pitcher terminated! (" + socket.getInetAddress() + ")");
			    	break;
			    }
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (fromPitcher != null)
				fromPitcher.close();
			if (toPitcher != null)
				toPitcher.close();
			if (socket != null)
				socket.close();
		}
	}

	// Create response message specific size
	public String generateMessage(Integer size) {
		StringBuilder outputString = new StringBuilder();
		
		// Create first part of response
		String firstPartOfMsg = msgNumberReceived.toString() + fillMessage + endPitcherTime.toString() + fillMessage;
		outputString.append(firstPartOfMsg);
		
		// Fill the rest of response with specific character
		for (int i = firstPartOfMsg.length(); i < size; i++)
			outputString.append(fillMessage);

		return outputString.toString();
	}

	// Get information from received message
	public void checkReceivedMsg(String msg) {
    	// Save size of received message
		messageSize = msg.length();
		
		// Extract msg number from received message
		String delims = "["+fillMessage+"]+";
		String[] tokens = msg.split(delims);
		if (TCPPing.debug) {
			for (int i = 0; i < tokens.length; i++)
				System.out.println("token: " + tokens[i]);
		}
		msgNumberReceived = Integer.parseInt(tokens[0]);
	}
	
	// What to do when Ctrl+c is pressed
	public void printAfterCtrlC() {
		System.out.println();
		System.out.println("Exiting Catcher, Goodbye!");
	}
}

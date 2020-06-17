import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class PingPitcher {
	// Hostname, port, mps and size from main program
	private String hostname;
	private Integer port;
	private Integer mps;
	private Integer size;
	
	// Default send msg every second
	private Integer sendMsgEvery = 1000;
	// Message id number
	private Integer msgNumber = 0;
	
	private Integer msgsSent = 0;
	private Integer msgsReceived = 0;
	private Integer msgsFailedLastSecond = 0;
	private Integer msgsFailedAll = 0;
	private Integer msgsAtLastSecond = 0;
	
	// List of msgs that was success and failed, not used anywhere
	private ArrayList<Integer> msgsThatFailed = new ArrayList<Integer>();
	private ArrayList<Integer> msgsThatSucceeded = new ArrayList<Integer>();
	
	// To convert times from nano seconds to mili seconds
	//private double nanoSecondsToMiliSeconds = 1e6;
	
	// A -> B and B -> A times
	private double A2BTimeInMs = 0;
	private double B2ATimeInMs = 0;
	
	// Round trip time (A -> B + B -> A)
	private Long rtt;

	// Last second calculations
	private double rttInLastSec = 0;
	private double A2BInLastSec = 0;
	private double B2AInLastSec = 0;
	
	// Max rtt calculation
	private double rttMax = 0;
	
	// Store times in nanoseconds for calculation
	private Long startPitcherTime;
	private Long endPitcherTimeOnCatcher;
	private Long startCatcherTimeOnCatcher;
	private Long endCatcherTime;
	private Long offsetCatcherPitcher;
	
	// Character that will fill a send message
	private char fillMessage = '#';
	
	// Find ip address from hostname and creating a socket to Catcher
	private InetAddress inetAddress;
	public Socket socket;
	
	// Send and receive messages
	private PrintWriter toCatcher;
	private BufferedReader fromCatcher;
    
	// Header for statistics
	private List<String> colsHeader = Arrays.asList("time", "msgs", "msgs/s", "avgABA", "maxABA", "avgAB", "avgBA");
	private String stringFormatHeader = String.format("| %8s | %7s | %7s | %7s | %7s | %7s | %7s |", colsHeader.toArray());
	
	// List for each row that will printout
	private List<String> colsValues = new ArrayList<String>();
	
	// Constructor
	public PingPitcher(Integer port, Integer mps, Integer size, String hostname) {
		this.hostname = hostname;
		this.port = port;
		this.mps = mps;
		this.size = size;
		
		// if mps is less or equal zero use default value (1 mps)
		if (this.mps > 0)
			this.sendMsgEvery = this.sendMsgEvery / this.mps;
		
		System.out.println("PingPitcher created! (hostname: " + 
							this.hostname + 
							", port: " + 
							this.port + 
							", mps: " + 
							this.mps + 
							", size: " + 
							this.size + 
							")");
		
		try {
			// get IP address from hostname
			inetAddress = InetAddress.getByName(this.hostname);
		
			// connecting to the hostname on a certain port
			socket = new Socket(inetAddress, this.port);
			
			// set timeout on readline call on InputStream
			socket.setSoTimeout(1500);
			
			System.out.println("Connected to Catcher: " + this.inetAddress + ":" + this.port + "\n");
			
		} catch (ConnectException e) {
			System.out.println("Connection refused! (Maybe no Catcher on " + this.inetAddress + ":" + this.port + ")");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("Host unknown!");
		} catch (IOException e) {
			System.out.println("Connection failed!");
		}
		
		// Catch Ctrl+c
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() { 
	        	printAfterCtrlC();
	       	}
	    });
	}
	
	// Start sending msgs to Catcher and print statistics every second
	public void start() throws Exception {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		
		// Open input and output streams
		fromCatcher = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		toCatcher = new PrintWriter(socket.getOutputStream(), true);
		
		// Printout header for statistics
		System.out.println(stringFormatHeader);
		
		// Send message to Catcher at period
		executor.scheduleAtFixedRate(sendToCatcher, 0, this.sendMsgEvery, TimeUnit.MILLISECONDS);
		
		// Calculate and print statistics every second 
		executor.scheduleAtFixedRate(showStatistics, 1, 1, TimeUnit.SECONDS);
	}
	
	Runnable showStatistics = new Runnable() {
	    public void run() {
	        colsValues.clear();
	        
	        // Add current time to printout list
	        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:MM:SS");
	        colsValues.add(timeFormat.format(LocalDateTime.now()));
	        
	        // Add number of msgs to printout list
	        colsValues.add(msgsSent.toString());
	        
	        // Calculate statistics
	        Integer msgsInLastSecond = msgsSent - msgsAtLastSecond;
	        msgsAtLastSecond = msgsSent;
	        Double rttAvrg = rttInLastSec/msgsInLastSecond;
	        rttInLastSec = 0;
	        Double A2BInLastSecAvrg = A2BInLastSec/msgsInLastSecond;
	        A2BInLastSec = 0;
	        Double B2AInLastSecAvrg = B2AInLastSec/msgsInLastSecond;
	        B2AInLastSec = 0;

	        // Add number of msgs in last second to printout list
	        colsValues.add(msgsInLastSecond.toString());
	        
	        // Add average rtt in last second to printout list
	        colsValues.add(String.format("%.2f", rttAvrg));
	        
	        // Add max rtt to printout list
	        colsValues.add(String.format("%.2f", rttMax));
	        
	        // Add average time A->B in last second to printout list
	        colsValues.add(String.format("%.2f", A2BInLastSecAvrg));
	        
	        // Add average time B->A in last second to printout list
	        colsValues.add(String.format("%.2f", B2AInLastSecAvrg));
	        
	        // This is for failed msgs in last seccond
	        //colsValues.add(msgsFailedLastSecond.toString());
	        msgsFailedLastSecond=0;
	        
	        // Now, printout row to standard output
	        for (String s : colsValues)
	        	System.out.print(String.format("| %7s ", s));
	        System.out.print("|\n");
	    }
	};

	// Start sending messages to Catcher and waiting for response
	// Also, save times A->B, B->A and rtt for statistics
	Runnable sendToCatcher = new Runnable() {
	    public void run() {
			try {
				// Number of message that will be sent
				msgNumber++;
				
				// Generate message of specific size for sending to Catcher
				String msg = generateMessage(size);
				
				// Time before sending msg to Catcher, for A->B
		    	startPitcherTime = System.currentTimeMillis();
		    	TCPPing.verbosePrint(msgNumber + ": startPitcherTime: " + startPitcherTime);
		    	
		    	// Sending msg to Catcher
		    	toCatcher.println(msg);
		    	toCatcher.flush();
		    	
				msgsSent++;

				try {
					// Receiving message from the server
					String messageFromCatcher = fromCatcher.readLine();
					
					// Time after receiving msg from Catcher, for B->A
					endCatcherTime = System.currentTimeMillis();
					TCPPing.verbosePrint(msgNumber + ": endCatcherTime:   " + endCatcherTime);
					
					// Extract msg number and time difference from received message
					checkReceivedMsg(messageFromCatcher, msgNumber);

					TCPPing.verbosePrint(msgNumber + ": endPitcherTime on C:   " + endPitcherTimeOnCatcher);
					TCPPing.verbosePrint(msgNumber + ": startCatcherTime on C: " + startCatcherTimeOnCatcher);
					
					msgsReceived++;
					
					// Calculate offset by clock synchronization algorithm from NTP
					//https://en.wikipedia.org/wiki/Network_Time_Protocol
					offsetCatcherPitcher = ((endPitcherTimeOnCatcher - startPitcherTime) + (startCatcherTimeOnCatcher - endCatcherTime)) / 2;
					// Rtt time by clock synchronization algorithm from NTP
					rtt = (endCatcherTime - startPitcherTime) - (startCatcherTimeOnCatcher - endPitcherTimeOnCatcher);
					// Calculate endPitcherTime and startCatcherTime on Pitcher with offset
					
					TCPPing.verbosePrint(msgNumber + ": offsetCatcherPitcher:   " + offsetCatcherPitcher);
					TCPPing.verbosePrint(msgNumber + ": rtt:                    " + rtt);
					
					// Time from Pitcher to Catcher in ms
					A2BTimeInMs = endPitcherTimeOnCatcher - startPitcherTime - offsetCatcherPitcher;
					// Time from Catcher to Pitcher in ms
					B2ATimeInMs = endCatcherTime - startCatcherTimeOnCatcher + offsetCatcherPitcher;
					
					// Add times to last second
					rttInLastSec += rtt;
					A2BInLastSec += A2BTimeInMs;
					B2AInLastSec += B2ATimeInMs;
					
					TCPPing.verbosePrint(msgNumber+ ": A2B: " + A2BTimeInMs + ": B2A: " + B2ATimeInMs + "\n");
					
					// Calculating max rtt
					if (rtt > rttMax)
						rttMax = rtt;
					
				} catch (SocketTimeoutException exception) {
					msgsFailedAll++;
					msgsFailedLastSecond++;
					msgsThatFailed.add(msgNumber);
					TCPPing.verbosePrint("SocketTimeoutException!!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	};
	
	// Generate message of specific size
	public String generateMessage(Integer size) {
		StringBuilder outputString = new StringBuilder();
		
		// Create first part of message
		String firstPartOfMsg = msgNumber.toString() + fillMessage;
		outputString.append(firstPartOfMsg);
		
		// Fill the rest of response with specific character
		for (int i = firstPartOfMsg.length(); i < size; i++)
			outputString.append(fillMessage);

		return outputString.toString();
	}
	
	// Get information from received message
	public void checkReceivedMsg(String msg, Integer msgNumber) {
		String delims = "["+fillMessage+"]+";
		String[] tokens = msg.split(delims);
		
		if (TCPPing.debug) {
			for (int i = 0; i < tokens.length; i++)
				System.out.println("token: " + i + "- -" + tokens[i]);
		}

		if (msgNumber == Integer.parseInt(tokens[0])) {
			msgsThatSucceeded.add(Integer.parseInt(tokens[0]));
			endPitcherTimeOnCatcher = Long.parseLong(tokens[1]);
			startCatcherTimeOnCatcher = Long.parseLong(tokens[2]);
		}
	}
	
	// What to do when Ctrl+c is pressed
	public void printAfterCtrlC() {
		System.out.println();
		System.out.println("Messages sent successfully: " + msgsReceived);
		System.out.println("Messages sent failed: " + msgsFailedAll);
		System.out.println();
		System.out.println("Exiting Pitcher, Goodbye!");
	}
}

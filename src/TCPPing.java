public class TCPPing {
	// Variables for holding info from cmd line arguments
	static String bindAddress;
	static Integer port = 9900;
	static Integer mps = 1;
	static Integer size = 300;
	static String hostname;
	
	// Flags for program mode
	static boolean pitcherFlag = false;
	static boolean catcherFlag = false;
	static boolean verboseFlag = false;
	
	// For debugging printouts change this to true
	static boolean debug = false;
	
	public static void usage() {
		System.out.println("Usage: TCPPing [-v] [-c] [-bind IP] [-port PORT]");
    	System.out.println("       or");
    	System.out.println("       TCPPing [-v] [-p] [-port PORT] [-mps SPEED(1-1000)] [-size SIZE(50-3000)] IP");
	}
	
	public static void msgAndExit(String msg, int errorCode) {
		if (verboseFlag) System.err.println(msg);
		usage();
		System.exit(errorCode);
	}
	
	public static void verbosePrint(String msg) {
		if (verboseFlag) System.out.println(msg);
	}

	public static void parseCmdLineArguments(String[] args) {
		if (debug) {
			System.out.print("Parsing command line arguments: ");
			for (String s: args) System.out.print(s + " ");
			System.out.println();
		}
		
		int i = 0;
		String arg;
		
		while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (debug) {
            	System.out.println("Parse: -"+arg+"-");
            }

            if (arg.equals("-v")) {
            	verboseFlag = true;
            	verbosePrint("Verbose mode on");
            }
            else if (arg.equals("-p")) {
            	if (!catcherFlag) { 
            		pitcherFlag = true;
            		hostname = args[args.length-1];
            		verbosePrint("Pitcher mode (hostname = " + hostname + ")");
            	}
            	else {
            		msgAndExit("Can't run Catcher and Pitcher at same time", 1);
            	}
            }
            else if (arg.equals("-c")) {
            	if (!pitcherFlag) { 
            		catcherFlag = true;
            		verbosePrint("Catcher mode");
            	}
            	else {
            		msgAndExit("Can't run Catcher and Pitcher at same time", 1);
            	}
            }
            else if (arg.equals("-bind")) {
                if (i < args.length) {
                	bindAddress = args[i++];
                	verbosePrint("bind address = " + bindAddress);
                }
                else {
                	msgAndExit("-bind requires a IP address", 1);
                }
            }
            else if (arg.equals("-port")) {
                if (i < args.length) {
                	try {
                        port = Integer.parseInt(args[i++]);
                        verbosePrint("port number = " + port);
                    } catch (NumberFormatException nfe) {
                    	msgAndExit("port has to be number", 1);
                    }
                }
                else {
                	msgAndExit("-port requires port number", 1);
                }
            }
            else if (arg.equals("-mps")) {
                if (i < args.length) {
                	try {
                        mps = Integer.parseInt(args[i++]);
                        if (mps < 1 || mps > 1000) {
                        	msgAndExit("mps has to be between 1 and 1000", 1);
    					}
                        verbosePrint("mps = " + mps);
                    } catch (NumberFormatException nfe) {
                    	msgAndExit("mps has to be number", 1);
                    }
                }
                else {
                	msgAndExit("-mps requires msgs/s value", 1);
                }
            }
            else if (arg.equals("-size")) {
                if (i < args.length) {
                	try {
                        size = Integer.parseInt(args[i++]);
                        if (size < 50 || size > 3000) {
                        	msgAndExit("size has to be between 50 and 3000 bytes!", 1);
    					}
                        verbosePrint("size number = " + size);
                    } catch (NumberFormatException nfe) {
                    	msgAndExit("size has to be number", 1);
                    }
                }
                else {
                	msgAndExit("-size requires number between 50 - 3000", 1);
                }
            }
            else {
            	msgAndExit("ParseCmdLine: illegal option", 2);
            }
        }
	}
	
	public static void main(String[] args) {
		// Get cmd line arguments to variables 
		parseCmdLineArguments(args);
		
		
		
		// If Catcher flag is set, start Catcher
		if (catcherFlag) {
			// Create Catcher object and open socket on port
			PingCatcher catcher = new PingCatcher(bindAddress, port);
			
			// Start accepting connections from Pitcher
			// Loop it forever so Catcher don't die when pitcher is disconnected
			// Exit Catcher with Ctrl+c (terminate)
			while (true) {
				try {
					catcher.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// If Pitcher flag is set, start Catcher
		else if (pitcherFlag) {
			// Create Pitcher object and try to connect to Catcher
			PingPitcher pitcher = new PingPitcher(port, mps, size, hostname);
			
			// Start Pitcher only if connection is established
			if (pitcher.socket != null && pitcher.socket.isConnected()) {
				try {
					pitcher.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}

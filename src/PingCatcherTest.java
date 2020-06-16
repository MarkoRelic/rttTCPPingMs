import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.BeforeClass;
import org.junit.Test;

public class PingCatcherTest {
    static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    String output;
    String expectedOutput;
    static PingCatcher catcher;
    
    @BeforeClass
    public static void setUpClass() {
    	System.setOut(new PrintStream(outContent));
        catcher = new PingCatcher("localhost", 9900);
    }
    
	@Test	public void testGenerateMessage() {
		fail("Not yet implemented");

	}
	
	@Test	public void testPingCatcher() {
	    // What to expect
	    expectedOutput = "PingCatcher created! (bind address: localhost, port: 9900)\r\n"; 
	    
	    // Actions
	    // Test creation of PingCatcher object
	    
	    // Test expected:
	    output = new String(outContent.toByteArray());
	    
	    assertTrue(output.contains(expectedOutput));
	}

	@Test
	public void testCheckReceivedMsg() {
		fail("Not yet implemented");
	}

	@Test
	public void testPrintAfterCtrlC() {
	    // What to expect
	    expectedOutput = "\r\nExiting Catcher, Goodbye!";
	    
	    // Actions
	    catcher.printAfterCtrlC();
	    
	    // Test expected:
	    output = new String(outContent.toByteArray());
	    
	    assertTrue(output.contains(expectedOutput));
	}

}

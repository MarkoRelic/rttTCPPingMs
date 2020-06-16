import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class TCPPingTest {
    static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    String output;
    String expectedOutput;
    static PingCatcher catcher;
    
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
	@Test
	public void testVerbosePrint() {
		fail("Not yet implemented");
	}

	@Test
	public void testParseCmdLineArguments_NoModeError() {
		exit.expectSystemExitWithStatus(TCPPing.ExitCodes.NO_MODE.intValue());
				
		TCPPing.parseCmdLineArguments(new String[]{"-v"});
	}
	@Test
	public void testParseCmdLineArguments_IllegalOptionError() {
		exit.expectSystemExitWithStatus(TCPPing.ExitCodes.ILLEGAL_OPTION.intValue());
				
		TCPPing.parseCmdLineArguments(new String[]{"-g"});
	}
	@Test
	public void testParseCmdLineArguments_BindNeedAddressError() {
		exit.expectSystemExitWithStatus(TCPPing.ExitCodes.BIND_NEED_ADDRESS.intValue());
				
		TCPPing.parseCmdLineArguments(new String[]{"-bind"});
	}
	@Test
	public void testParseCmdLineArguments_MpsOutOfRangeError() {
		exit.expectSystemExitWithStatus(TCPPing.ExitCodes.MPS_OUT_OF_RANGE.intValue());
				
		TCPPing.parseCmdLineArguments(new String[]{"-mps","999999"});
	}
	@Test
	public void testParseCmdLineArguments_SizeOutOfRangeError() {
		exit.expectSystemExitWithStatus(TCPPing.ExitCodes.SIZE_OUT_OF_RANGE.intValue());
				
		TCPPing.parseCmdLineArguments(new String[]{"-size","3001"});
	}

	@Test
	public void testParseCmdLineArguments() {
		PrintStream stdout = System.out;
		System.setOut(new PrintStream(outContent));
		
		expectedOutput = "Catcher mode\r\n";
		
		TCPPing.parseCmdLineArguments(new String[]{"-v", "-c", "-port", "9900"});
		output = new String(outContent.toByteArray());
		
		assertTrue(output.contains(expectedOutput));
		
		expectedOutput = "port number = 9900\r\n";
		assertTrue(output.contains(expectedOutput));

		System.setOut(stdout);
	}

	@Test
	public void testMain() {
		fail("Not yet implemented");
	}

}

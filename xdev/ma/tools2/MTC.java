package ma.tools2;

import java.awt.Font;
import java.nio.charset.Charset;

/**
 * <p style="color:red;background-color:black;font-size:300%;font-weight:bold;text-align:center;">
 * 	WARNING: Tools 2.1 is IN-DEVELOPMENT
 * </p>
 * 
 * @version 1.4
 * @author Linux-Fan, Ma_Sys.ma
 * @since Tools 2
 */
public class MTC {
	
	// Static
	private MTC() {
		super();
	}

	public static final String TOOLS_VERSION = "2.1.0.2 Alpha";

	/* TODO THIS CAN BE EXTRACTED BY DIFFERENT MEANS (cf. d5manserver) */
	public static final Charset UTF8 = Charset.forName("UTF-8");

	public static final Font TERMINAL_FONT =
			new Font(Font.MONOSPACED, Font.PLAIN, 12);

	public static final String EMS = "Error Missing String";

}

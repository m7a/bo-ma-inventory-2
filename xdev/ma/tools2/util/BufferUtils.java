package ma.tools2.util;

import java.io.*;
import java.security.MessageDigest;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @version 1.0.1.2
 * @since 2014/07, Tools 2.0
 * @author Linux-Fan, Ma_Sys.ma
 */
public class BufferUtils {

	/** 4K Buffer should be good for most systems */
	public static final int DEFAULT_BUFFER = 0x1000;

	/** Instances of this class must not be created. */
	private BufferUtils() {
		super();
	}

	/** The streams will not be closed by this method. */
	public static void copy(InputStream in, OutputStream out)
							throws IOException {
		byte[] buf = new byte[DEFAULT_BUFFER];
		int len;
		while((len = in.read(buf, 0, buf.length)) != -1)
			out.write(buf, 0, len);
	}

	public static void copy(InputStream in, OutputStream out1,
					OutputStream out2) throws IOException {
		byte[] buf = new byte[DEFAULT_BUFFER];
		int len;
		while((len = in.read(buf, 0, buf.length)) != -1) {
			out1.write(buf, 0, len);
			out2.write(buf, 0, len);
		}
	}

	/**
	 * Copies updating the checksum at the same time.
	 * This was originally part of JMBB.
	 */
	public static void copy(InputStream in, MessageDigest md,
					OutputStream out) throws IOException {
		byte[] buf = new byte[DEFAULT_BUFFER];
		int len;
		while((len = in.read(buf, 0, buf.length)) != -1) {
			out.write(buf, 0, len);
			if(md != null)
				md.update(buf, 0, len);
		}
	}

	/**
	 * Reads all the data from the given stream into a string which is then
	 * returned. The stream is always closed.
	 *
	 * Encoding is hard coded to be UTF-8 and newlines are created as
	 * <code>\n</code> chars (not OS specific).
	 *
	 * @param inRaw <code>InputStream</code> to read from.
	 * @return all Data from <code>inRaw</code>
	 */
	public static String readfile(InputStream inRaw) throws IOException {
		StringBuilder ret = new StringBuilder();
		String line;
		try(BufferedReader in = new BufferedReader(
					new InputStreamReader(inRaw, UTF_8))) {
			while((line = in.readLine()) != null) {
				ret.append(line);
				ret.append('\n');
			}
		}
		return ret.toString();
	}

	public static ByteArrayInputStream writefile(String inRaw) {
		return new ByteArrayInputStream(inRaw.getBytes(UTF_8));
	}

}

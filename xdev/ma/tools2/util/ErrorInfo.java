package ma.tools2.util;

import java.util.Arrays;

/**
 * @author Linux-Fan, Ma_Sys.ma
 * @version 2.1.0.0
 * @since Tools 2.0 (?)
 */
public class ErrorInfo {

	private static final String PREFIX_STR = "    ";

	public static interface ExtendedMessageGenerator {

		/** @return Array of lines or null if nothing */
		public String[] getExtendedInfo(Throwable t);

	}

	private static class NullMessageGenerator
					implements ExtendedMessageGenerator {

		@Override
		public String[] getExtendedInfo(Throwable t) {
			return null;
		}

	}

	public static StringBuilder getStackTrace(Throwable t) {
		return getStackTrace(t, new NullMessageGenerator());
	}

	public static StringBuilder getStackTrace(Throwable t,
						ExtendedMessageGenerator gen) {
		StringBuilder ret = new StringBuilder();
		appendStackTrace(ret, t, gen, 1);
		return ret;
	}

	public static void appendStackTrace(StringBuilder trace, Throwable t,
				ExtendedMessageGenerator gen, int level) {
		char[] prefixChr = new char[level * PREFIX_STR.length()];
		Arrays.fill(prefixChr, ' ');
		String prefix = new String(prefixChr);

		if(level > 64) {
			trace.append(prefix);
			trace.append("Tools 2 Error: Recursion " +
							"level exceeded.");
			return;
		}

		trace.append(t.toString());
		trace.append('\n');
		trace.append(prefix);
		trace.append("Trace:\n");
		for(StackTraceElement el: t.getStackTrace()) {
			trace.append(prefix);
			trace.append(PREFIX_STR);
			trace.append(el.getClassName());
			trace.append('.');
			trace.append(el.getMethodName());
			trace.append('(');
			trace.append(el.isNativeMethod()? "native":
						(el.getFileName() + ":" +
						el.getLineNumber()));
			trace.append(")\n");
		}
		
		String[] einfo = gen.getExtendedInfo(t);
		if(einfo != null) {
			trace.append(prefix);
			trace.append("Extended info:");
			trace.append('\n');
			for(String s: einfo) {
				trace.append(prefix);
				trace.append(PREFIX_STR);
				trace.append(s);
				trace.append('\n');
			}
		}

		int nl = level + 1;

		for(Throwable s: t.getSuppressed()) {
			trace.append(prefix);
			trace.append("Suppressed: ");
			appendStackTrace(trace, s, gen, nl);
		}

		Throwable cause = t.getCause();
		if(cause != null) {
			trace.append(prefix);
			trace.append("Cause: ");
			appendStackTrace(trace, cause, gen, nl);
		}
	}

}

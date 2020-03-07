package ma.inventory2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.awt.Color;
import java.math.BigInteger;

import uk.org.okapibarcode.backend.*;
import uk.org.okapibarcode.output.SvgRenderer;

public class BarCodeGenSVG {

	private static enum CodeType { CODE128, PDF417, CODE2O5, DATAMATRIX };

	public static void main(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("USAGE $0 FROMINCL NUM DIRECTORY");
			System.exit(1);
		}
		new BarCodeGenSVG().run(new BigInteger(args[0]),
				Integer.parseInt(args[1]), Paths.get(args[2]));
	}

	private BarCodeGenSVG() {
		super();
	}

	private void run(BigInteger from, int num, Path targetDir)
							throws Exception {
		try(BufferedWriter toc = Files.newBufferedWriter(
						targetDir.resolve("toc.tex"))) {
			for(int i = 0; i < num; i++) {
				BigInteger ib = new BigInteger(
							String.valueOf(i));
				String id = from.add(ib).toString();
				String fn = String.format("bc_%04d", i);
				writeSym(makeSymbol(id), fn + 'p',
								targetDir);
				writeSym(makeSecondary(id), fn + 's',
								targetDir);
				toc.write("\\barcode{" + fn + "}{" + id + "}%");
				toc.newLine();
			}
		}
	}

	private static void writeSym(Symbol sym, String fn, Path targetDir)
							throws IOException {
		// We do our own borders later
		//sym.setBorderWidth(0);
		try(OutputStream os = Files.newOutputStream(targetDir.resolve(
								fn + ".svg"))) {
			renderSymbolSVG(sym, os);
		}
	}

	private static Symbol makeSymbol(String msg) {
		return makeSymbol(CodeType.CODE128, msg);
	}

	private static Symbol makeSecondary(String msg) {
		return makeSymbol(CodeType.DATAMATRIX, msg);
	}

	private static void renderSymbolSVG(Symbol sym, OutputStream os)
							throws IOException {
		//new SvgRenderer(os, Color.WHITE, Color.BLACK).render(sym);
		new SvgRenderer(os, 1.0, Color.WHITE, Color.BLACK).render(sym);
	}

	private static Symbol makeSymbol(CodeType type, String msg) {
		try {
			switch(type) {
			case CODE128: return defaultSym(new Code128(), msg);
			case PDF417: return defaultSym(new Pdf417(), msg);
			case CODE2O5:
				Code2Of5 c25 = defaultSym(new Code2Of5(), msg);
				//c25.setInterleavedMode();
				return c25;
			case DATAMATRIX:
				DataMatrix dm = defaultSym(new DataMatrix(),
									msg);
				//dm.forceSquare(true); 
				return dm;
			default:
				throw new IllegalArgumentException(
					"Unknown enum constant: " + type);
			}
		} catch(OkapiException ex) {
			throw new RuntimeException("Failed to make symbol", ex);
		}
	}

	private static <T extends Symbol> T defaultSym(T sym, String msg) {
		//sym.setBorderWidth(4);
		sym.setContent(msg);
		sym.setHumanReadableLocation(HumanReadableLocation.NONE);
		sym.setFontSize(0);
		return sym;
	}

}

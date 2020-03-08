package ma.inventory.plugin.barcode;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import uk.org.okapibarcode.backend.*;
import uk.org.okapibarcode.output.Java2DRenderer;

import ma.inventory.m.Verteiler;
import ma.inventory.m.Plugin;

public class PluginBarCode implements Plugin, Function<Character,Node> {

	private static enum CodeType { CODE128, PDF417, CODE2O5, DATAMATRIX };

	private final Map<Character,Node> cache;

	private Verteiler v;
	private CodeType code;
	private CodeType secondary;
	private BarCodeExport export;

	public PluginBarCode() {
		super();
		cache = new HashMap<Character,Node>();
		export = new BarCodeExport(this);
	}

	@Override
	public void init(String conf, Verteiler v) {
		this.v = v;
		String[] codeNames = conf.split("/");
		code = CodeType.valueOf(codeNames[0]);
		if(codeNames.length > 1)
			secondary = CodeType.valueOf(codeNames[1]);
		v.buttonIcons.setValue(this);
		v.exports.add(export);
	}

	@Override
	public String getDefaultConfiguration() {
		return "CODE128/DATAMATRIX";
	}

	@Override
	public Node apply(Character chr) {
		return cache.containsKey(chr)? cache.get(chr): mk(chr);
	}

	@Override
	public void close() {
		cache.clear();
		v.buttonIcons.setValue(null);
		v.exports.remove(export);
	}

	private Node mk(char chr) {
		Node ret = new ImageView(SwingFXUtils.toFXImage(
					// double escape to exit autocomplete
					encode("\u001b\u001b" + chr), null));
		cache.put(chr, ret);
		return ret;
	}

	private BufferedImage encode(String msg) {
		return renderBarcode(makeSymbol(msg));
	}

	Symbol makeSymbol(String msg) {
		return makeSymbol(code, msg);
	}

	Symbol makeSecondary(String msg) {
		if(secondary == null)
			throw new RuntimeException("No secondary symbol " +
								"registered.");
		return makeSymbol(secondary, msg);
	}

	private static Symbol makeSymbol(CodeType type, String msg) {
		try {
			switch(type) {
			case CODE128: return defaultSym(new Code128(), msg);
			case PDF417: return defaultSym(new Pdf417(), msg);
			case CODE2O5:
				Code2Of5 c25 = defaultSym(new Code2Of5(), msg);
				c25.setInterleavedMode();
				return c25;
			case DATAMATRIX:
				DataMatrix dm = defaultSym(new DataMatrix(),
									msg);
				dm.forceSquare(true); 
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
		sym.setBorderWidth(4);
		sym.setContent(msg);
		sym.setHumanReadableLocation(HumanReadableLocation.NONE);
		sym.setFontSize(0);
		return sym;
	}

	// -> https://www.jayway.com/2016/06/30/gs1-datamatrix-codes-java/

	private static BufferedImage renderBarcode(Symbol symbol) {
		int scale = (symbol instanceof DataMatrix)? 2: 1;
		BufferedImage image = new BufferedImage(
					symbol.getRenderWidth() * scale,
					symbol.getRenderHeight() * scale,
					BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		Java2DRenderer renderer = new Java2DRenderer(g2d, Color.WHITE,
								Color.BLACK);
		renderer.setUIMagnification(scale);
		renderer.render(symbol);
		return image;
	}

}

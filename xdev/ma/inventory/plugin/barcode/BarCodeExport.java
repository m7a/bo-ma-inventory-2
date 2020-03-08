package ma.inventory.plugin.barcode;

import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.awt.Color;

import ma.inventory.m.Export;
import ma.inventory.m.MainTableRow;

import uk.org.okapibarcode.backend.Symbol;
import uk.org.okapibarcode.output.SvgRenderer;

class BarCodeExport implements Export {

	private static final String[] COPY_RES = {
		"barcodes.tex", "masysmaicon_print_sw.pdf"
	};

	private final PluginBarCode root;

	BarCodeExport(PluginBarCode root) {
		super();
		this.root = root;
	}

	@Override
	public String toString() {
		return "Barcodes";
	}

	@Override
	public void export(Stage guiRoot, Iterable<MainTableRow> rows)
							throws Exception {
		DirectoryChooser c = new DirectoryChooser();
		File targetDirF = c.showDialog(guiRoot);
		if(targetDirF != null) {
			Path targetDir = targetDirF.toPath();
			int i = 1;
			try(BufferedWriter toc = Files.newBufferedWriter(
						targetDir.resolve("toc.tex"))) {
				for(MainTableRow r: rows) {
					String id = r.getUserSuppliedID();
					String fn = String.format("bc_%04d", i);
					writeSym(root.makeSymbol(id), fn +
							'p', targetDir);
					writeSym(root.makeSecondary(id), fn +
							's', targetDir);
					toc.write("\\barcode{" + fn + "}{" + id
									+ "}%");
					toc.newLine();
					i++;
				}
			}
			for(String r: COPY_RES) {
				try(InputStream is = getClass().
						getResourceAsStream(r)) {
					Files.copy(is, targetDir.resolve(r),
						StandardCopyOption.
						REPLACE_EXISTING);
				}
			}
		}
	}

	private static void writeSym(Symbol sym, String fn, Path targetDir)
							throws IOException {
		// We do our own borders later
		sym.setBorderWidth(0);
		try(OutputStream os = Files.newOutputStream(targetDir.resolve(
								fn + ".svg"))) {
			renderSymbolSVG(sym, os);
		}
	}

	private static void renderSymbolSVG(Symbol sym, OutputStream os)
							throws IOException {
		new SvgRenderer(os, Color.WHITE, Color.BLACK).render(sym);
	}

}

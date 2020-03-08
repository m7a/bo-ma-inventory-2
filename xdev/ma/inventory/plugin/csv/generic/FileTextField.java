package ma.inventory.plugin.csv.generic;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import javafx.event.ActionEvent;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class FileTextField extends BorderPane {

	private final boolean dirsOnly;
	private final boolean requireExistence;
	private final TextField fsel;

	private Window parent;

	/**
	 * @param dirsOnly true: only directories, false: only files
	 * @param requireExistence true: selection of existent files/dirs,
	 */
	public FileTextField(final Path defaultSelection,
					final boolean dirsOnly,
					final boolean requireExistence) {
		super();
		this.dirsOnly = dirsOnly;
		this.requireExistence = requireExistence;
		setCenter(fsel = new TextField(defaultSelection == null? "":
				defaultSelection.toAbsolutePath().toString()));
		Button sel = new Button("...");
		sel.setOnAction(this::refOnAction);
		setRight(sel);
	}

	private void refOnAction(ActionEvent ev) {
		File rs;
		if(dirsOnly && requireExistence) {
			DirectoryChooser dc = new DirectoryChooser();
			try {
				dc.setInitialDirectory(Paths.get(
						fsel.getText()).toFile());
			} catch(InvalidPathException ex) {
				// ignore intermittend invalid paths
			}
			rs = dc.showDialog(parent);
		} else {
			FileChooser fc = new FileChooser();
			if(dirsOnly) {
				try {
					fc.setInitialDirectory(Paths.get(
						fsel.getText()).toFile());
				} catch(InvalidPathException ex) {
					// ignore intermittend invalid paths
				}
			} else {
				fc.setInitialFileName(fsel.getText());
			}
			if(requireExistence) {
				rs = fc.showOpenDialog(parent);
			} else {
				rs = fc.showSaveDialog(parent);
			}
		}
		if(rs != null)
			fsel.setText(rs.toPath().toAbsolutePath().toString());
	}

	public void assignParent(Window newParent) {
		parent = newParent;
	}

	public boolean hasValidSelection() {
		try {
			Path sp = Paths.get(fsel.getText());
			// permit overwrite selection: if existence not required
			// all checks are skipped....
			return !requireExistence ||
				(dirsOnly?
					Files.isDirectory(sp):
					(Files.exists(sp) &&
						!Files.isDirectory(sp)));
		} catch(InvalidPathException ex) {
			return false;
		}
	}

	public Path getSelection() {
		return Paths.get(fsel.getText());
	}

}

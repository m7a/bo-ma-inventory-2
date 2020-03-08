package ma.inventory.vc;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.CheckBox;       // need to be explicit because
import javafx.scene.control.ContentDisplay; // otherwise `TableRow` is
import javafx.scene.control.Label;          // ambiguous.
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.function.Function;

import ma.inventory.m.*;

class EditDialog { // TODO z ALL DIALOGS SHOULD HAVE TITLES

	private static final int MAX_QTY = 0x10000;

	// Parmeter
	private final Verteiler v;

	// Single instance
	private final EditAutoCompleteDataByColumn eac;
	private final Stage dialog;
	private final ObjectProperty<EnterOption> enterOption;
	private final BooleanProperty keepEnterOption;
	private final GridPane rootNode;
	private final ObjectProperty<RowType> type;
	private final BooleanProperty keepType;
	private final ObjectProperty<Integer> qtyIn;

	// Multi instance
	private final BooleanProperty[][] keepSpecific;
	private final ObservableList<EditDialogColumn> columns;

	private int currentColCount = 0;
	private boolean normalMode = false;
	private boolean isAdd = true;

	// Input data
	private MainTableRow currr;
	private Function<MainTableRow,String> callback;

	EditDialog(Stage owner, Verteiler v) {
		super();
		this.v = v;
		eac = new EditAutoCompleteDataByColumn(v.tableData);
		dialog = new Stage();
		dialog.initOwner(owner);
		rootNode = new GridPane();
		rootNode.setPadding(new Insets(20.0));

		Spinner<Integer> qty = new Spinner<Integer>(0, MAX_QTY, 1);
		qtyIn = qty.getValueFactory().valueProperty();
		ComboBox<RowType> cb = new ComboBox<RowType>();
		cb.getItems().addAll(RowType.class.getEnumConstants());
		type = cb.valueProperty();
		CheckBox keepTypeB = new CheckBox("Keep Type");
		keepType = keepTypeB.selectedProperty();
		// TODO z looks redundant w/ above probably move ComboBox + CheckBox to a component class etc.
		ComboBox<EnterOption> enterOptionG =
						new ComboBox<EnterOption>();
		enterOptionG.getItems().addAll(
					EnterOption.class.getEnumConstants());
		enterOption = enterOptionG.valueProperty();
		CheckBox keepEnterOptionG = new CheckBox("Keep Enter Option");
		keepEnterOption = keepEnterOptionG.selectedProperty();
		enterOptionG.getSelectionModel().select(0);
		Button reset = new Button("Reset");
		reset.setContentDisplay(ContentDisplay.BOTTOM);
		assignIcon(reset, 'r', v.buttonIcons);
		reset.setOnAction(this::refReset);
		Button switchType = new Button("Switch Type");
		switchType.setContentDisplay(ContentDisplay.BOTTOM);
		assignIcon(switchType, 's', v.buttonIcons);
		switchType.setOnAction(this::refSwitchType);
		Button disambiguate = new Button("Disambiguate");
		disambiguate.setContentDisplay(ContentDisplay.BOTTOM);
		assignIcon(disambiguate, 'd', v.buttonIcons);
		disambiguate.setOnAction(ev -> handleDisambiguateOnly());
		HBox top = new HBox(
			20.0,
			reset,
			new VBox(new Label("Quantity"), qty),
			new VBox(new FlowPane(new Label("Type "), cb),
								keepTypeB),
			switchType,
			new VBox(new FlowPane(new Label("Enter Option "),
					enterOptionG), keepEnterOptionG),
			disambiguate
		);
		rootNode.addColumn(1, top);
		GridPane.setHgrow(top, Priority.ALWAYS);

		final GridPane main = new GridPane();
		main.setHgap(15.0);
		main.setVgap(2.0);
		final CheckBox[][] keepSpecificG =
					new CheckBox[Constants.FD.length][];
		final Label[][] description = new Label[Constants.FD.length][];
		keepSpecific = new BooleanProperty[Constants.FD.length][];
		for(int i = 0; i < Constants.FD.length; i++) {
			int sl = Constants.FD[i].length;
			keepSpecificG[i] = new CheckBox[sl];
			keepSpecific[i] = new BooleanProperty[sl];
			description[i] = new Label[sl];
			for(int j = 0; j < sl; j++) {
				keepSpecificG[i][j] = new CheckBox();
				keepSpecific[i][j] = keepSpecificG[i][j].
							selectedProperty();
				description[i][j] = new Label(Constants.
								FD[i][j]);
			}
		}
		// empty label as spacer (easier than complete indexing)
		main.addColumn(0, new Label(), new Label());
		main.addColumn(1, new Label(), new Label("ID or ISBN"));
		columns = FXCollections.observableArrayList();
		rootNode.addColumn(1, main);
		GridPane.setVgrow(main, Priority.ALWAYS);
		GridPane.setHgrow(main, Priority.ALWAYS);

		BorderPane footer = new BorderPane();
		Button cancel = new Button("Cancel");
		cancel.setContentDisplay(ContentDisplay.BOTTOM);
		assignIcon(cancel, 'c', v.buttonIcons);
		cancel.setOnAction(this::refCancel);
		footer.setLeft(cancel);
		Button ok = new Button("OK");
		ok.setContentDisplay(ContentDisplay.BOTTOM);
		assignIcon(ok, 'o', v.buttonIcons);
		ok.setOnAction(this::refDisambiguateOrComplete);
		footer.setRight(ok);
		rootNode.addColumn(1, footer);
		//GridPane.setVgrow(footer, Priority.ALWAYS);
		GridPane.setHgrow(footer, Priority.ALWAYS);

		Scene rsc = new Scene(rootNode);
		rsc.setOnKeyPressed(this::refKeyPressed);
		dialog.setScene(rsc);

		// Register type listener
		type.addListener((x, o, n) -> {
			if(o != null) {
				main.getChildren().removeAll(
						keepSpecificG[o.ordinal()]);
				main.getChildren().removeAll(
						description[o.ordinal()]);
			}
			main.addColumn(0, keepSpecificG[n.ordinal()]);
			main.addColumn(1, description[n.ordinal()]);
			Iterator<EditDialogColumn> iter = columns.iterator();
			for(int i = 2; iter.hasNext(); i++) {
				EditDialogColumn cc = iter.next();
				if(o != null)
					removeAll(main, o.ordinal(), cc);
				addAll(main, i, n.ordinal(), cc);
			}
		});
		// Initialize with books
		cb.getSelectionModel().select(0);

		// Register columns listerner
		columns.addListener(new ListChangeListener<EditDialogColumn>() {
			@Override
			public void onChanged(ListChangeListener.Change<
					? extends EditDialogColumn> change) {
				int ct = type.getValue().ordinal();
				while(change.next()) {
					for(EditDialogColumn c:
							change.getRemoved()) {
						removeAll(main, ct, c);
						// TODO z AUFRÜCKEN? (NUR NÖTIG, FALLS REMOVAL [IN DER MITTE] UNTERSTÜTZT WERDEN SOLL)
						currentColCount--;
					}
					for(EditDialogColumn c: change.
							getAddedSubList()) {
						int acc = ++currentColCount + 1;
						addAll(main, acc, ct, c);
					}
				}
				updateButtonNumberIcons(v.buttonIcons.
								getValue());
			}
		});

		v.buttonIcons.addListener((x, o, n)
						-> updateButtonNumberIcons(n));
	}

	// TODO z below comes logic which we would love to extract to a separate ``controller'' (now it is part of the ``view''... :( )

	private void updateButtonNumberIcons(Function<Character,Node> icons) {
		int i = 0;
		for(EditDialogColumn c: columns) {
			if(i > 9)
				break; // ignore > 9 (they do not get icons)
			assignIconNow(c.okBtn, String.valueOf(i++).charAt(0),
									icons);
		}
	}

	private static void assignIconNow(Button btn, char c,
					Function<Character,Node> icons) {
		btn.setGraphic(icons == null? null: icons.apply(c));
	}

	private static void assignIcon(final Button btn, final char c,
			ObjectProperty<Function<Character,Node>> icons) {
		assignIconNow(btn, c, icons.getValue());
		icons.addListener((x, o, n) -> assignIconNow(btn, c, n));
	}

	private static void removeAll(GridPane main, int ct,
							EditDialogColumn c) {
		main.getChildren().removeAll(c.idInG, c.lblG, c.okBtn);
		main.getChildren().removeAll(c.specificFieldsG[ct]);
	}

	private static void addAll(GridPane main, int acc, int ct,
							EditDialogColumn c) {
		main.addColumn(acc, c.lblG);
		main.addColumn(acc, c.idInG);
		main.addColumn(acc, c.specificFieldsG[ct]);
		main.addColumn(acc, c.okBtn);
	}

	private void refKeyPressed(KeyEvent ev) {
		KeyCode c = ev.getCode();
		if(c == KeyCode.ESCAPE) {
			normalMode = true;
			rootNode.requestFocus();
		} else if(normalMode) {
			ev.consume();
			leaveNormalMode();
			switch(c) {
			case O:      handleDisambiguateOrComplete(); break;
			case C:      handleCancel();                 break;
			case D:      handleDisambiguateOnly();       break;
			case R:      handleReset();                  break;
			case S:      handleSwitchType();             break;
			case DIGIT0: handleNumberKey(0);             break;
			case DIGIT1: handleNumberKey(1);             break;
			case DIGIT2: handleNumberKey(2);             break;
			case DIGIT3: handleNumberKey(3);             break;
			case DIGIT4: handleNumberKey(4);             break;
			case DIGIT5: handleNumberKey(5);             break;
			case DIGIT6: handleNumberKey(6);             break;
			case DIGIT7: handleNumberKey(7);             break;
			case DIGIT8: handleNumberKey(8);             break;
			case DIGIT9: handleNumberKey(9);             break;
			}
		}
	}

	private void handleNumberKey(int n) {
		handleOK(columns.get(n));
	}

	private void leaveNormalMode() {
		normalMode = false;
		if(columns.size() != 0)
			// Delay requestFocus as to make sure the event which
			// is currently processed is not recognized by the
			// text field, too. If one does not do this, spurious
			// characters appear in the text field for ID input.
			Platform.runLater(() ->
					columns.get(0).idInG.requestFocus());
	}

	private void refReset(ActionEvent ev) {
		handleReset();
	}

	private void handleReset() {
		set(currr);
	}

	private void refSwitchType(ActionEvent ev) {
		handleSwitchType();
	}

	private void handleSwitchType() {
		switch(type.getValue()) {
		case BOOK:  type.setValue(RowType.OTHER); break;
		case OTHER: type.setValue(RowType.BOOK); break;
		}
	}

	/**
	 * Does not change parameter
	 *
	 * Callback return values
	 * null: Consumed no continue
	 * "": Consumed continue
	 * "MSG": Not consumed display error message
	 *
	 * @return new MainTableRow with changes applied.
	 */
	void edit(MainTableRow tr, Function<MainTableRow,String> cb) {
		set(tr);
		callback = cb;
		dialog.show();
	}

	private void set(MainTableRow tr) {
		currr = tr;
		columns.clear();
		isAdd = (tr == null);
		if(isAdd) {
			dialog.setTitle("Add");
			if(!keepType.getValue())
				type.set(RowType.BOOK);
		} else {
			dialog.setTitle("Edit");
			type.set(tr.getType());
		}
		if(!keepEnterOption.getValue())
			enterOption.setValue(EnterOption.AUTO);
		qtyIn.setValue(tr == null? 1: tr.getQTY());
		columns.add(instanciateColumn(tr));
		leaveNormalMode();
	}

	private EditDialogColumn instanciateColumn(TableRow tr) {
		return new EditDialogColumn(this::refOK, this::refEnter, tr,
									eac);
	}

	/** Cancel button pressed */
	private void refCancel(ActionEvent ev) {
		handleCancel();
	}

	private void handleCancel() {
		dialog.close();
	}

	private void refEnter(ActionEvent ev) {
		switch(enterOption.getValue()) {
		case AUTO:         handleDisambiguateOrComplete(); break;
		case DISAMBIGUATE: handleDisambiguateOnly();       break;
		case APPLY:        handleOK((EditDialogColumn)
					(((Node)ev.getSource()).getUserData()));
		                                                   break;
		case NOP:          /* nop */                       break;
		default: throw new RuntimeException("Unknown enum constant: " +
						enterOption.getValue());
		}
	}

	private void refDisambiguateOrComplete(ActionEvent ev) {
		handleDisambiguateOrComplete();
	}

	/** OK (disambiguate if necessary) button pressed */
	private void handleDisambiguateOrComplete() {
		boolean hasSugg = !isAdd || handleDisambiguateOnly();
		if(columns.size() == 1 && (hasSugg ||
				!v.set.isRequireAutoCompleteBeforeOK()))
			handleOK(columns.get(0));
	}

	/** Disambiguate in the sense of auto-complete only (do not submit) */
	private boolean handleDisambiguateOnly() {
		EditDialogColumn orig = columns.get(0);
		MainTableRow sr = makeCompleteTableRow(orig);
		List<EditDialogColumn> rc = new ArrayList<EditDialogColumn>();
		TableRow[] csug;
		for(Suggester currentSuggester: v.suggesters) {
			try {
				if((csug = currentSuggester.suggest(sr)) != null)
					for(TableRow r: csug)
						rc.add(instanciateColumn(r));
			} catch(RuntimeException ex) {
				v.log.error("Suggester " + currentSuggester +
								" failed.", ex);
			}
		}

		switch(rc.size()) {
		case 0:
			// nothing suggested
			return false;
		case 1:
			EditDialogColumn onlyResult = rc.get(0);
			if(onlyResult.isAutoCompleteFor(type.getValue(), orig)
						&& onlyResult.getInType() ==
						type.getValue())
				orig.autoCompleteFrom(type.getValue(),
								onlyResult);
			else
				columns.add(onlyResult);
			return true;
		default:
			columns.clear();
			columns.add(orig);
			columns.addAll(rc);
			return true;
		}
	}

	/** OK (disambiguation complete) Button pressed */
	private void refOK(ActionEvent ev) {
		handleOK((EditDialogColumn)(((Button)ev.getSource()).
								getUserData()));
	}

	private MainTableRow makeCompleteTableRow(EditDialogColumn col) {
		MainTableRow rs = col.getTableRow();
		rs.type.setValue(type.getValue());
		rs.qty.setValue((isAdd? col.getImplicitQTY(): 0) +
							qtyIn.getValue());
		return rs;
	}

	private void handleOK(EditDialogColumn col) {
		MainTableRow rs = makeCompleteTableRow(col);
		String result = callback.apply(rs);
		if(result == null) {
			dialog.close();
		} else if(result.length() == 0) {
			set(null);
			// evaluate "keep"-checkboxes
			EditDialogColumn newCol = columns.get(0);
			int i = type.getValue().ordinal();
			for(int j = 0; j < keepSpecific[i].length; j++)
				if(keepSpecific[i][j].getValue())
					newCol.specificFields[i][j].setValue(rs.
						getField(type.getValue(), j));
		} else {
			v.log.displayAlert("Failed to save entry.", result);
		}
	}

}

package ma.inventory.vc;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.beans.value.ChangeListener;

import ma.inventory.m.*;

import np.com.ngopal.control.AutoFillTextBox;

class EditDialogColumn {

	// GUI management
	final Label lblG;
	final TextField idInG;
	final AutoFillTextBox[][] specificFieldsG;
	final Button okBtn;

	// Data access
	private final StringProperty idIn;
	final StringProperty[][] specificFields;

	private int implicitQTY;
	private long useID;
	private final RowType inType;

	EditDialogColumn(EventHandler<ActionEvent> btnAction,
			EventHandler<ActionEvent> textFieldAction,
			TableRow base, EditAutoCompleteDataByColumn eac) {
		super();
		useID = base == null? -1: base.getDBID();
		implicitQTY = (base == null || useID == -1)? 0: base.getQTY();
		inType = base == null? null: base.getType();
		lblG = new Label(lblMsg(useID, implicitQTY, inType));
		idInG = new TextField(base == null? "":
						base.getUserSuppliedID());
		idInG.setUserData(this);
		idInG.setOnAction(textFieldAction);
		idIn = idInG.textProperty();
		specificFieldsG = new AutoFillTextBox[Constants.FD.length][];
		specificFields = new StringProperty[Constants.FD.length][];
		okBtn = new Button("OK");
		okBtn.setContentDisplay(ContentDisplay.BOTTOM);
		okBtn.setOnAction(btnAction);

		for(RowType t: RowType.class.getEnumConstants()) {
			int i = t.ordinal();
			int sl = Constants.FD[i].length;
			specificFieldsG[i] = new AutoFillTextBox[sl];
			specificFields[i] = new StringProperty[sl];
			for(int j = 0; j < sl; j++) {
				// TODO z AutoFillTextBox is pretty buggy -> now remove from external jar and to this application then fix that thing (and then make git patch!) -> TEST IS IT STILL AN ISSUE?
				specificFieldsG[i][j] = new AutoFillTextBox<
						String>(eac.getList(t, j));
				specificFieldsG[i][j].setListLimit(32);
				TextField tf = specificFieldsG[i][j].
								getTextbox();
				tf.setText(base == null? "":
							base.getField(t, j));
				tf.setUserData(this);
				tf.setOnAction(textFieldAction);
				specificFields[i][j] = tf.textProperty();
			}
		}

		okBtn.setUserData(this);
		GridPane.setHgrow(okBtn, Priority.ALWAYS);
	}

	private static String lblMsg(long useID, int implicitQTY,
							RowType inType) {
		return "dID " + useID + " / iQTY " + implicitQTY + " / iT " +
									inType;
	}

	MainTableRow getTableRow() {
		MainTableRow ret = new MainTableRow(useID);
		ret.userSuppliedID.setValue(idIn.getValue());
		for(int i = 0; i < specificFields.length; i++)
			for(int j = 0; j < specificFields[i].length; j++)
				ret.fields[i][j].setValue(specificFields[i][j].
								getValue());
		return ret;
	}

	boolean isAutoCompleteFor(RowType t, EditDialogColumn incompleteCol) {
		return fieldEqualOrEmptyOnOneSide(idIn, incompleteCol.idIn) &&
			fieldsEqualOrEmptyOnOneSide(specificFields[t.ordinal()],
				incompleteCol.specificFields[t.ordinal()]);
	}

	private static boolean fieldEqualOrEmptyOnOneSide(StringProperty a,
							StringProperty b) {
		return a.getValue().length() == 0 ||
					b.getValue().length() == 0 ||
					a.getValue().equals(b.getValue());
	}

	private static boolean fieldsEqualOrEmptyOnOneSide(StringProperty[] a,
							StringProperty[] b) {
		assert a.length == b.length;
		for(int i = 0; i < a.length; i++)
			if(!fieldEqualOrEmptyOnOneSide(a[i], b[i]))
				return false;
		return true;
	}

	void autoCompleteFrom(RowType t, EditDialogColumn completeCol) {
		if(useID == -1)
			useID = completeCol.useID;
		if(implicitQTY == 0 && useID != -1)
			implicitQTY = completeCol.implicitQTY;

		lblG.setText(lblMsg(useID, implicitQTY, inType));

		if(idIn.getValue().length() == 0)
			idIn.setValue(completeCol.idIn.getValue());

		int i = t.ordinal();
		for(int j = 0; j < specificFields[i].length; j++)
			if(specificFields[i][j].getValue().length() == 0)
				specificFields[i][j].setValue(completeCol.
					specificFields[i][j].getValue());
	}

	int getImplicitQTY() {
		return implicitQTY;
	}

	RowType getInType() {
		return inType;
	}

}

package ma.inventory.m;

import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import ma.tools2.util.ErrorInfo;

public class Log implements Thread.UncaughtExceptionHandler {

	private Stage wnd;

	Log() {
		super();
		Thread.currentThread().setUncaughtExceptionHandler(this);
	}

	public void setWnd(Stage wnd) {
		this.wnd = wnd;
	}

	public void info(String msg) {
		System.out.println("INFO    " + msg);
	}

	public void warning(String msg) {
		System.out.println("WARNING " + msg);
	}

	public void error(String msg, Throwable t) {
		System.out.println("ERROR   " + msg);
		t.printStackTrace();

		if(wnd == null)
			info("Not displaying a graphical error message " +
							"because wnd is null.");
		else
			displayAlert(msg,
					ErrorInfo.getStackTrace(t).toString());
	}

	public void displayAlert(String msg, String report) {
		Alert alert = new Alert(Alert.AlertType.ERROR, msg);
		alert.setHeaderText(null);
		TextArea errorReport = new TextArea(report);
		errorReport.setPrefRowCount(16);
		errorReport.setEditable(false);
		alert.getDialogPane().setExpandableContent(errorReport);
		if(wnd != null)
			alert.initOwner(wnd);
		alert.showAndWait();
	}

	@Override
	public void uncaughtException(Thread p, Throwable t) {
		try {
			error("Uncaught exception in thread " + p.getName() +
						" (" + p.getClass() + "): " +
						t.toString(), t);
		} catch(Throwable tS) {
			tS.addSuppressed(t);
			System.err.println("FATAL ERROR PROGRAM EXECUTION " +
								"HALTED");
			tS.printStackTrace();
			Runtime.getRuntime().halt(2);
		}
	}

}

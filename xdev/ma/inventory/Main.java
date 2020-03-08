package ma.inventory;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

import ma.inventory.vc.AppWndCnt;
import ma.inventory.m.Verteiler;
import ma.inventory.m.Constants;

public class Main extends Application {

	private Verteiler v;

	@Override
	public void init() {
		v = new Verteiler();
	}

	@Override
	public void start(Stage myStage) {
		AppWndCnt rootNode = new AppWndCnt(v, myStage);
		v.log.setWnd(myStage);
		myStage.setScene(new Scene(rootNode));
		myStage.setTitle(Constants.ID);
		v.loadPlugins();
		myStage.show();
	}

	@Override
	public void stop() {
		v.stop();
	}

	public static void main(String[] args) {
		launch(args);
	}

}

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.application.Application;

public class JavaFXSkel extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
		// ...
	}

	@Override
	public void start(Stage myStage) {
		myStage.setTitle("JavaFX Skeleton");
		FlowPane rootNode = new FlowPane();
		Scene myScene = new Scene(rootNode, 300, 200);
		myStage.setScene(myScene);
		myStage.show();
	}

	@Override
	public void stop() {
		// ...
	}

}

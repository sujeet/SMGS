
package alviz2.app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Screen;

import alviz2.app.ColorPalette;
import alviz2.app.AppConfig;

public class Alviz extends Application {

	private AlvizController controller;

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Alviz.fxml"));
		BorderPane rootPane = (BorderPane) loader.load();
		controller = loader.getController();
		double screenWidth = Screen.getPrimary().getVisualBounds().getWidth() - 10;
		double screenHeight = Screen.getPrimary().getVisualBounds().getHeight() - 50;
		Scene scene = new Scene(rootPane, screenWidth, screenHeight);
		AppConfig.canvasWidth = screenWidth;
		AppConfig.canvasHeight = screenHeight - 50;

		stage.setTitle("Alviz 2");
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		try {
			controller.cleanup();
		}
		catch(Exception ex) {
			System.out.println("In app");
			System.out.println(ex);
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
	
}
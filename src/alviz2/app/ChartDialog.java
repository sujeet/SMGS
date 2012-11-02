
package alviz2.app;

import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Orientation;

import static alviz2.app.AlvizController.AlgoVizBundle;

public class ChartDialog {
	private static ChartDialog dlgInstance = null;
	private ObservableList<AlgoVizBundle> algoList;
	private Stage dlgStage;
	private FlowPane chartPane;

	private class AlgoListListener implements ListChangeListener<AlgoVizBundle> {
		@Override
		public void onChanged(Change<? extends AlgoVizBundle> change) {
			chartPane.getChildren().clear();
			for (AlgoVizBundle bundle : algoList) {
				chartPane.getChildren().add(bundle.chart);
			}			
		}
	}

	private ChartDialog(ObservableList<AlgoVizBundle> algoVizList) {
		algoList = algoVizList;

		algoList.addListener(new AlgoListListener());

		// construct dialog
		dlgStage = new Stage();
		dlgStage.setTitle("Charts Window");
		ScrollPane root = new ScrollPane();
		chartPane = new FlowPane(Orientation.HORIZONTAL);
		chartPane.setPrefWrapLength(800);
		root.setContent(chartPane);
		Scene scene = new Scene(root);
		dlgStage.setScene(scene);

		for (AlgoVizBundle bundle : algoList) {
			chartPane.getChildren().add(bundle.chart);
		}
		// construct dialog
	}

	public static ChartDialog getInstance(ObservableList<AlgoVizBundle> algoVizList) {
		if (dlgInstance == null || dlgInstance.algoList != algoVizList) {
			if (dlgInstance != null) {
				dlgInstance.chartPane.getChildren().clear();
				dlgInstance.dlgStage.hide();
				dlgInstance = null;				
			}

			dlgInstance = new ChartDialog(algoVizList);
		}

		return dlgInstance;
	}

	public void show() {
		if (!dlgStage.isShowing()) {
			dlgStage.show();
		}
	}
}
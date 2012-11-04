
package alviz2.app;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.WritableImage;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.jgrapht.Graph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.SimpleGraph;

import alviz2.algo.Algorithm;
import alviz2.graph.Node;
import alviz2.graph.Edge;
import alviz2.util.*;
import alviz2.graph.factory.*;
import alviz2.app.AppConfig;

public class AlvizController implements Initializable {

	@FXML private BorderPane rootPane;
	@FXML private Button startButton;
	@FXML private Button stopButton;
	@FXML private Button stepButton;
	@FXML private Menu menuAlgo;
	@FXML private Menu menuGraph;
	@FXML private Slider algoUpdCycle;
	@FXML private Group canvasPane;
	@FXML private MenuItem menuFilePipe;
	@FXML private MenuItem menuFileNew;
	@FXML private MenuItem menuFileSaveGraph;

	static class AlgoVizBundle {
		Algorithm<Node, Edge> algo;
		Set<Node> startNodes;
		Set<Node> goalNodes;
		Graph<Node, Edge> graph;
		Visualizer viz;
		XYChart<Number, Number> chart;
		Canvas canvas;
		boolean algoActive;
	}
	private ObservableList<AlgoVizBundle> algoBundles;
	private ObservableList<AlgoVizBundle> fixedObsAlgoBundles;
	private Timeline animTimer;

	// Private variables related to the state machine and its state
	private enum AppState {
		ALGO_SELECT, GRAPH_SELECT, RUN_PENDING, RUNNING, PAUSED, FINISHED, PIPE_SELECT;
	}
	private StateMachine<AppState> appStateMachine;
	private List<String> algoList;
	private Class<Algorithm<Node, Edge>> curAlgoClass;
	private GraphFactory curGraphFactory;

	private StateMachine.StateHandler<AppState> stateALGO_SELECTEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			switch(prevState) {
				case PAUSED:
				case FINISHED:
				case PIPE_SELECT:
				case RUN_PENDING:
				{
					canvasPane.getChildren().clear();
				}
				case ALGO_SELECT:
				{
					for (AlgoVizBundle bundle : algoBundles) {
						bundle.algo.cleanup();
					}
					algoBundles.clear();

					menuAlgo.setDisable(false);
					menuFileNew.setDisable(false);
				}
				break;


				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.ALGO_SELECT + " from " + prevState);
				}
			}
		}
	};

	private StateMachine.StateHandler<AppState> stateGRAPH_SELECTEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			switch(prevState) {
				case ALGO_SELECT:
				case GRAPH_SELECT:
				{
					menuAlgo.setDisable(false);
					menuGraph.setDisable(false);
					menuFileNew.setDisable(false);

					AlgorithmRequirements ar = curAlgoClass.getAnnotation(AlgorithmRequirements.class);
					GraphMenuHandler gmh = new GraphMenuHandler();
					menuGraph.getItems().clear();
					Map<String, GraphFactory> gt = ar.graphType().getFactoryMap();
					for (Map.Entry<String, GraphFactory> me: gt.entrySet()) {
						MenuItem gitm = new MenuItem(me.getKey());
						gitm.setUserData(me.getValue());
						gitm.setOnAction(gmh);
						menuGraph.getItems().add(gitm);
					}
				}
				break;


				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.GRAPH_SELECT + " from " + prevState);
				}
			}
		}
	};

	private StateMachine.StateHandler<AppState> stateRUN_PENDINGEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			AlgoVizBundle bundle = null;
			switch(prevState) {
				case GRAPH_SELECT:
				{
					try {
						bundle = createAlgoVizBundle(curAlgoClass, curGraphFactory);
						canvasPane.getChildren().add(bundle.canvas);
					}
					catch(Exception ex) {
						System.out.println(ex);
						return;
					}
				}
				break;

				case PIPE_SELECT:
				{
					try {
						AlgoVizBundle sourceBundle = algoBundles.get(algoBundles.size()-1);
						bundle = createAlgoVizBundleClone(curAlgoClass, sourceBundle);
						ColorAdjust deSaturate = new ColorAdjust();
						deSaturate.setSaturation(-1.0);
						sourceBundle.canvas.setEffect(deSaturate);
						bundle.canvas.setBlendMode(BlendMode.SRC_OVER);
						canvasPane.getChildren().add(bundle.canvas);
					}
					catch(Exception ex) {
						System.out.println(ex);
						return;
					}
				}
				break;

				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.RUN_PENDING + " from " + prevState);
				}
			}
			algoBundles.add(bundle);
			bundle.viz.render();

			startButton.setDisable(false);
			stepButton.setDisable(false);
			algoUpdCycle.setDisable(false);
			menuFileNew.setDisable(false);
			menuFileSaveGraph.setDisable(false);
		}
	};

	private StateMachine.StateHandler<AppState> stateRUNNINGEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			switch(prevState) {
				case PAUSED:
				case RUN_PENDING:
				{
					stopButton.setDisable(false);

					EventHandler<ActionEvent> timerHandler = new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent e) {
							if (!stepAlgos()) {
								animTimer.stop();
								animTimer = null;
								appStateMachine.setState(AppState.FINISHED);
							}
						}
					};
					animTimer = new Timeline(new KeyFrame(Duration.millis(algoUpdCycle.getValue()), timerHandler));
					animTimer.setCycleCount(Timeline.INDEFINITE);
					animTimer.playFromStart();
				}
				break;

				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.RUNNING + " from " + prevState);
				}
			}
		}
	};

	private StateMachine.StateHandler<AppState> statePAUSEDEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			switch(prevState) {
				case RUNNING:
				{
					animTimer.stop();
					animTimer = null;
					startButton.setDisable(false);
					stepButton.setDisable(false);
					algoUpdCycle.setDisable(false);
					menuFileNew.setDisable(false);
					menuFileSaveGraph.setDisable(false);
				}
				break;

				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.PAUSED + " from " + prevState);
				}
			}
		}
	};

	private StateMachine.StateHandler<AppState> stateFINISHEDEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			switch(prevState) {
				case PAUSED:
				case RUN_PENDING: // These two cases happen because of stepping
				case RUNNING:
				{
					menuFilePipe.setDisable(false);
					menuFileNew.setDisable(false);
					menuFileSaveGraph.setDisable(false);
				}
				break;

				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.FINISHED + " from " + prevState);
				}
			}
		}
	};

	private StateMachine.StateHandler<AppState> statePIPE_SELECTEnter = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState prevState) {
			switch(prevState) {
				case FINISHED:
				{
					menuAlgo.setDisable(false);
				}
				break;

				default:
				{
					throw new IllegalStateException("Cannot come to " + AppState.PIPE_SELECT + " from " + prevState);
				}
			}
		}
	};

	private StateMachine.StateHandler<AppState> stateExitHandler = new StateMachine.StateHandler<AppState>() {
		@Override
		public void run(AppState newState) {
			menuFileNew.setDisable(true);
			menuFilePipe.setDisable(true);
			menuFileSaveGraph.setDisable(true);
			startButton.setDisable(true);
			stopButton.setDisable(true);
			stepButton.setDisable(true);
			algoUpdCycle.setDisable(true);
			menuGraph.setDisable(true);
			menuAlgo.setDisable(true);
		}
	};

	private class AlgoMenuHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			MenuItem itm = (MenuItem) e.getSource();
			curAlgoClass = (Class<Algorithm<Node, Edge>>) itm.getUserData();

			if (appStateMachine.getState() == AppState.ALGO_SELECT) {
				appStateMachine.setState(AppState.GRAPH_SELECT);
			}
			else {
				appStateMachine.setState(AppState.RUN_PENDING);
			}
		}
	}

	private class GraphMenuHandler implements EventHandler<ActionEvent> {
		@Override
		public void	handle(ActionEvent e) {
			MenuItem itm = (MenuItem) e.getSource();
			curGraphFactory = (GraphFactory) itm.getUserData();
			appStateMachine.setState(AppState.RUN_PENDING);
		}
	}

	@FXML
	private void handleAlgoStart(ActionEvent event) {
		appStateMachine.setState(AppState.RUNNING);
	}

	@FXML
	private void handleAlgoStop(ActionEvent event) {
		appStateMachine.setState(AppState.PAUSED);
	}

	@FXML
	private void handleAlgoStep(ActionEvent event) {
		if (!stepAlgos()) {
			appStateMachine.setState(AppState.FINISHED);
		}
	}

	@FXML
	private void handleFileClose(ActionEvent event) throws Exception {
		Platform.exit();
	}

	@FXML 
	private void handleFileNew(ActionEvent event) {
		appStateMachine.setState(AppState.ALGO_SELECT);
	}

	@FXML 
	private void handleFilePipe(ActionEvent event) {
		appStateMachine.setState(AppState.PIPE_SELECT);
	}
	// Private variables related to the state machine and its state

	@FXML 
	private void handleChartsButton(ActionEvent event) {
		ChartDialog.getInstance(fixedObsAlgoBundles).show();
	}

	@FXML
	private void handleFileSaveGraph(ActionEvent event) {
		try {
			GraphUtils.serializeGraph(algoBundles.get(algoBundles.size()-1).graph);
		}
		catch(Exception ex) {
			System.out.println(ex);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		algoList = getAlgoList();
		algoBundles = FXCollections.observableList(new LinkedList<AlgoVizBundle>());
		fixedObsAlgoBundles = FXCollections.unmodifiableObservableList(algoBundles);
		appStateMachine = new StateMachine.StateMachineBuilder<AppState>(AppState.class)
							.add(AppState.ALGO_SELECT, stateALGO_SELECTEnter, stateExitHandler)
							.add(AppState.GRAPH_SELECT, stateGRAPH_SELECTEnter, stateExitHandler)
							.add(AppState.RUN_PENDING, stateRUN_PENDINGEnter, stateExitHandler)
							.add(AppState.RUNNING, stateRUNNINGEnter, stateExitHandler)
							.add(AppState.PAUSED, statePAUSEDEnter, stateExitHandler)
							.add(AppState.FINISHED, stateFINISHEDEnter, stateExitHandler)
							.add(AppState.PIPE_SELECT, statePIPE_SELECTEnter, stateExitHandler)
							.build(AppState.ALGO_SELECT);

		AlgoMenuHandler algoMH = new AlgoMenuHandler();

		for (String clName: algoList) {
			try {
				Class<Algorithm> algo = (Class<Algorithm>) Class.forName(clName);
				MenuItem itm = new MenuItem(algo.getSimpleName());
				itm.setUserData(algo);
				itm.setOnAction(algoMH);
				menuAlgo.getItems().add(itm);
			}
			catch(Exception e) { 
				System.out.println(e);
			}
		}
	}

	public void cleanup() throws Exception {
		if (!algoBundles.isEmpty()) {
			for (AlgoVizBundle bundle : algoBundles) {
				bundle.algo.cleanup();
			}
		}
	}

	private List<String> getAlgoList() {
		List<String> list = new ArrayList<>();
		try {
			Path configPath = Paths.get(System.getProperty("alviz2ConfigFile", "/home/sujeet/Dropbox/Courses/AI/alviz2-project/dist/alviz2.config"));
			Scanner sc = new Scanner(configPath);
			while (sc.hasNextLine()) {
				list.add(sc.nextLine());
			}
		}
		catch(Exception ex) {
			System.out.println(ex);
		}
		return list;
	}

	private boolean stepAlgos() {
		int activeCnt = 0;

		for (AlgoVizBundle bundle : algoBundles) {
			if (bundle.algoActive) {
				bundle.algoActive = bundle.algo.executeSingleStep();
				bundle.viz.render();
			}
			activeCnt = bundle.algoActive ? activeCnt + 1 : activeCnt;
		}

		return activeCnt > 0;
	}
	
	private AlgoVizBundle createAlgoVizBundle(Class<Algorithm<Node, Edge>> algoClass, GraphFactory gFac) throws Exception {
		AlgoVizBundle bundle = new AlgoVizBundle();
		
		bundle.algo = algoClass.newInstance();

		VertexFactory<Node> vfac = bundle.algo.getVertexFactory();
		EdgeFactory<Node,Edge> efac = bundle.algo.getEdgeFactory();
		bundle.graph = gFac.createGraph(vfac, efac);

		PointIndex2D nodeIndex = new PointIndex2D();
		for(Node n: bundle.graph.vertexSet())
			nodeIndex.insert(n);
		GraphInputDialog grphInpDlg = new GraphInputDialog(bundle.graph, nodeIndex);


		AlgorithmRequirements ar = algoClass.getAnnotation(AlgorithmRequirements.class);
		for (GraphInit gi: ar.graphInitOptions()) {
			switch(gi) {
				case NODE_COST:
				{
					Random r = new Random();
					for (Node n: bundle.graph.vertexSet()) {
						n.setCost(r.nextInt(100));
					}
				}
				break;

				case EDGE_COST:
				{
					Random r = new Random();
					for (Edge ed: bundle.graph.edgeSet()) {
						Point2D sp = ed.getPositionS();
						Point2D dp = ed.getPositionD();
						ed.setCost(sp.distance(dp) + r.nextInt(10));
					}
				}
				break;

				case START_NODE:
				{
					Set<Node> blacklist = Collections.EMPTY_SET;
					bundle.startNodes = grphInpDlg.getNodes("Select a Node", 
						"Select a single node for a start", blacklist, false);
				}
				break;

				case GOAL_NODE:
				{
					bundle.goalNodes = grphInpDlg.getNodes("Select a Node",
						"Select a single node for a goal", bundle.startNodes, false);
				}
				break;

				case MANY_START_NODES:
				{
					Set<Node> blacklist = Collections.EMPTY_SET;
					bundle.startNodes = grphInpDlg.getNodes("Select a Node", 
						"Select a single node for a start", blacklist, true);
				}
				break;

				case MANY_GOAL_NODES:
				{
					bundle.goalNodes = grphInpDlg.getNodes("Select a Node",
						"Select a single node for a goal", bundle.startNodes, true);
				}
				break; 
			}
		}

		bundle.chart = new LineChart<>(new NumberAxis(), new NumberAxis());
		bundle.canvas = new Canvas(AppConfig.canvasWidth, AppConfig.canvasHeight);
		Node.PropChanger npr = Node.PropChanger.create();
		Edge.PropChanger epr = Edge.PropChanger.create();
		bundle.viz = new Visualizer(bundle.canvas, bundle.graph, npr, epr);
		bundle.algo.setGraph(bundle.graph, npr, epr, bundle.startNodes, bundle.goalNodes);
		bundle.algo.setChart(bundle.chart);
		bundle.algoActive = true;

		return bundle;
	}

	private AlgoVizBundle createAlgoVizBundleClone(Class<Algorithm<Node, Edge>> algoClass, AlgoVizBundle sourceBundle) throws Exception {
		AlgoVizBundle bundle = new AlgoVizBundle();

		bundle.algo = algoClass.newInstance();
		HashMap<Node, Node> cloneMap = new HashMap<>();
		bundle.graph = GraphUtils.cloneGraph(sourceBundle.graph, bundle.algo.getVertexFactory(), bundle.algo.getEdgeFactory(), cloneMap);
		bundle.startNodes = new HashSet<>();
		for (Node n : sourceBundle.startNodes) {
			bundle.startNodes.add(cloneMap.get(n));
		}
		bundle.goalNodes = new HashSet<>();
		for (Node n : sourceBundle.goalNodes) {
			bundle.goalNodes.add(cloneMap.get(n));
		}

		bundle.chart = new LineChart<>(new NumberAxis(), new NumberAxis());
		bundle.canvas = new Canvas(AppConfig.canvasWidth, AppConfig.canvasHeight);
		Node.PropChanger npr = Node.PropChanger.create();
		Edge.PropChanger epr = Edge.PropChanger.create();
		bundle.viz = new Visualizer(bundle.canvas, bundle.graph, npr, epr);
		bundle.algo.setGraph(bundle.graph, npr, epr, bundle.startNodes, bundle.goalNodes);
		bundle.algo.setChart(bundle.chart);
		bundle.algoActive = true;

		return bundle;
	}
}


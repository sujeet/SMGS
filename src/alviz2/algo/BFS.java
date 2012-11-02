
package alviz2.algo;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.EdgeFactory;

import alviz2.app.ColorPalette;
import alviz2.graph.Node;
import alviz2.graph.Edge;
import alviz2.util.AlgorithmRequirements;
import alviz2.util.GraphType;
import alviz2.util.GraphInit;

@AlgorithmRequirements (
	graphType = GraphType.ANY_GRAPH,
	graphInitOptions = {GraphInit.START_NODE, GraphInit.MANY_GOAL_NODES}
	)

public class BFS implements Algorithm<Node, Edge> {

	private Graph<Node, Edge> graph;
	private XYChart<Number, Number> chart;
	private Node.PropChanger npr;
	private Edge.PropChanger epr;
	private Node start;
	private Set<Node> goals;
	private LinkedList<Node> open = new LinkedList<>();
	private HashSet<Node> closed = new HashSet<>();
	private XYChart.Series<Number, Number> openListSeries;
	private XYChart.Series<Number, Number> closedListSeries;
	private int iterCnt;
	private ColorPalette palette;
	private SynchronousQueue<Boolean> queue;
	private boolean algoState;
	private Thread child;

	public static class VFac implements VertexFactory<Node> {
		int id;
		
		private VFac() {
			id = 0;
		}

		@Override public Node createVertex() {
			return new Node(id++);
		}
	}

	public static class EFac implements EdgeFactory<Node, Edge> {
		int id;

		private EFac() {
			id = 0;
		}

		@Override public Edge createEdge(Node s, Node d) {
			return new Edge(id++, s, d);
		}
	}

	public BFS() {
		graph = null;
		chart = null;
		start = null;
		goals = null;
		openListSeries = new XYChart.Series<>();
		closedListSeries = new XYChart.Series<>();
		iterCnt = 0;
		palette = ColorPalette.getInstance();
		algoState = true;
		queue = new SynchronousQueue<>();
		child = new Thread(new BFSHelper());
		child.start();
	}

	@Override
	public void setGraph(Graph<Node,Edge> graph, Node.PropChanger npr, Edge.PropChanger epr, Set<Node> start, Set<Node> goals) {
		this.graph = graph;
		this.epr = epr;
		this.npr = npr;
		for (Node n : start) {
			this.start = n;
		}

		for (Node n : graph.vertexSet()) {
			npr.setVisible(n, true);
		}

		for (Edge e : graph.edgeSet()) {
			epr.setVisible(e, true);
		}

		this.goals = goals;
		npr.setFillColor(this.start, palette.getColor("node.open"));
	}

	@Override
	public void setChart(XYChart<Number, Number> chart) {
		this.chart = chart;
		this.chart.setTitle("Breadth First Search");
		openListSeries.setName("Size of open list");
		closedListSeries.setName("Size of closed list");
		this.chart.getData().add(openListSeries);
		this.chart.getData().add(closedListSeries);
	}

	@Override public VertexFactory<Node> getVertexFactory()
	{
		return new VFac();
	}

	@Override public EdgeFactory<Node,Edge> getEdgeFactory()
	{
		return new EFac();
	}

	private class BFSHelper implements Runnable {
		@Override
		public void run() {
			// pause until you are called first
			try {
				queue.take();
			}
			catch(Exception ex) {
				return;
			}

			// BFS code starts
			HashMap<Node, Node> parentMap = new HashMap<>();
			open.add(BFS.this.start);

			try {
				while(!open.isEmpty()) {
					Node curNode = open.remove();

					if (closed.contains(curNode)) {
						continue;
					}

					closed.add(curNode);
					npr.setFillColor(curNode, palette.getColor("node.closed"));

					if (parentMap.get(curNode) != null) {
						epr.setStrokeColor(graph.getEdge(parentMap.get(curNode), curNode), palette.getColor("edge.closed"));
					}

					if(goals.contains(curNode)) {
						Node n = curNode;
						Node parent = parentMap.get(n);
						Color edgeColor = palette.getColor("edge.path");
						while (parent != null) {
							Edge e = graph.getEdge(n, parent);
							epr.setStrokeColor(e, edgeColor);
							epr.setBold(e, true);
							n = parent;
							parent = parentMap.get(n);
						}
						break;
					}

					for (Node nn : Graphs.neighborListOf(graph, curNode)) {
						if (!closed.contains(nn)) {
							open.add(nn);
							npr.setFillColor(nn, palette.getColor("node.open"));
							epr.setStrokeColor(graph.getEdge(curNode, nn), palette.getColor("edge.open"));
							parentMap.put(nn, curNode);
						}
					}

					// yield control to parent
					queue.put(true);
					queue.take();
					// execution will continue from here next time executeSingleStep is called
				}
			}
			catch(Exception ex) {
				return;
			}

			// terminate execution
			try {
				queue.put(false);
			}
			catch(Exception ex) {
				return;
			}
		}		
	}

	public boolean executeSingleStep() {
		if (algoState) {
			try {
				queue.put(algoState);
				algoState = queue.take();
			}
			catch(Exception ex) {
				System.out.println(ex);
				algoState = false;
			}
		}

		openListSeries.getData().add(new XYChart.Data<Number, Number>(iterCnt, open.size()));
		closedListSeries.getData().add(new XYChart.Data<Number, Number>(iterCnt, closed.size()));
		iterCnt++;

		return algoState;
	}

	@Override
	public void cleanup() {
		if (child.getState() != Thread.State.TERMINATED) {
			child.interrupt();

			try {
				child.join();
			}
			catch(Exception ex) {
				System.out.println(ex);
			}
		}
	}
}
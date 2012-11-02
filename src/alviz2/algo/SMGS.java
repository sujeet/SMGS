
package alviz2.algo;

import java.util.LinkedList;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

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

public class SMGS implements Algorithm<Node, Edge> {

    private Graph<Node, Edge> graph;
    private XYChart<Number, Number> chart;
    private Node.PropChanger npr;
    private Edge.PropChanger epr;
    private Node start;
    private Set<Node> goals;
    private LinkedList<Node> open;
    private Set<Node> closed;
    private Map<Node, Node> parents;
    private XYChart.Series<Number, Number> openListSeries;
    private XYChart.Series<Number, Number> closedListSeries;
    private int iterCnt;
    private ColorPalette palette;
    
    public int memory_size = 4;

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

    public SMGS() {
        graph = null;
        chart = null;
        start = null;
        goals = null;
        open = new LinkedList<>();
        closed = new HashSet<>();
        parents = new HashMap<>();
        openListSeries = new XYChart.Series<>();
        closedListSeries = new XYChart.Series<>();
        iterCnt = 0;
        palette = ColorPalette.getInstance();
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
        open.push(this.start);
        parents.put(this.start, null);
        npr.setFillColor(this.start, palette.getColor("node.open"));
    }

    @Override
    public void setChart(XYChart<Number, Number> chart) {
        this.chart = chart;
        this.chart.setTitle("Depth First Search");
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

    public void pruneClosedList () {
        LinkedList <Node> open_closed = new LinkedList <Node> ();
        open_closed.addAll (open);
        open_closed.addAll (closed);
		for (Node node: open_closed) {
		    if (Graphs.neighborListOf (graph, node).contains (parents.get (node))) {
		        Node parent = parents.get (node);
		        while (closed.contains (parent) && parent.shouldBePruned ()) {
		            parent = parents.get (parent);
		        }
		        if (parent != parents.get (node)) {
		            // Parent is actually a relay node now.
		            parents.put (node, parent);
		            parent.makeUnprunable ();
		        }
		    }
		}
		LinkedList <Node> to_be_removed_from_closed = new LinkedList <> ();
		for (Node node: closed) {
		    if (node.shouldBePruned ()) to_be_removed_from_closed.add (node);
		}
		for (Node node: to_be_removed_from_closed) {
		    closed.remove (node);
		}
    }
    
    public void expandNode (Node node) {
		for (Node child : Graphs.neighborListOf(graph, node)) {
		    if (open.contains (child)) {
		            child.reducePruneCount ();
		            double path_cost = node.getCost ()
		                               + graph.getEdge (node, child).getCost ();
		            if (path_cost < child.getCost ()) {
		                child.setCost (path_cost);
		                parents.put (child, node);
		            }
		    }
		    else if (closed.contains (child)) {
		        child.reducePruneCount ();
		    }
		    // Now, generate new node.
		    else {
		        child.setPruneCount (Graphs.neighborListOf(graph, child).size()-1);
		        double path_cost = node.getCost ()
		                + graph.getEdge (node, child).getCost ();
		        child.setCost (path_cost);
		        parents.put (child, node);
		        open.add (child);
		        
                npr.setFillColor(child, palette.getColor("node.open"));
                epr.setStrokeColor(graph.getEdge(node, child), palette.getColor("edge.open"));
                
		        // TODO: if memory full, then prune the list.
		        if (closed.size () >= memory_size) {
		            pruneClosedList ();
		        }
		    }
		}
    }
    
    @Override
    public boolean executeSingleStep() {

        if (open.isEmpty()) {
            return false;           
        }

        Node n = null;
        while(!open.isEmpty() && (n == null || closed.contains(n))) {
            // Choose a node from open with minimum cost.
            Node min_node = open.get (0);
            for (Node node : open) {
                if (min_node.getCost () > node.getCost ()) {
                    min_node = node;
                }
            }
            n = min_node;
        }

        if (n == null) {
            return false;
        }

        open.remove (n);
        closed.add(n);
        
        /*
        npr.setFillColor(n, Color.BLUE);
        if (parents.containsKey(n) && parents.get(n) != null) {
            epr.setStrokeColor(graph.getEdge(n, parents.get(n)), palette.getColor("edge.closed"));
        }
        */

        if (goals.contains(n)) {
            Node curNode = n;
            Node parent = parents.get(curNode);
            Color edgeColor = palette.getColor("edge.path");
            while (parent != null) {
                Edge e = graph.getEdge(curNode, parent);
                epr.setBold(e, true);
                epr.setStrokeColor(e, edgeColor);
                curNode = parent;
                parent = parents.get(curNode);
            }

            return false;
        }

        expandNode (n);

        openListSeries.getData().add(new XYChart.Data<Number, Number>(iterCnt, open.size()));
        closedListSeries.getData().add(new XYChart.Data<Number, Number>(iterCnt, closed.size()));
        iterCnt++;

        return true;
    }

    @Override
    public void cleanup() {
    }
}

package alviz2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import javafx.stage.FileChooser;
import javafx.geometry.Point2D;

import org.jgrapht.Graphs;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

import org.jdom2.input.SAXBuilder;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.Format;

import alviz2.app.ColorPalette;
import alviz2.graph.Node;
import alviz2.graph.Edge;

public class GraphUtils {

	public static <N extends Node,E extends Edge> Graph<N,E> cloneGraph(Graph<Node,Edge> srcGraph, VertexFactory<N> vfac, EdgeFactory<N,E> efac, Map<Node, N> nodeMap) {
		Graph<N,E> g = new SimpleGraph<>(efac);
		Node.PropChanger npr = Node.PropChanger.create();

		for (Node n: srcGraph.vertexSet()) {
			N nn = vfac.createVertex();
			nn.setCost(n.getCost());
			npr.setPosition(nn, n.getPosition());
			nodeMap.put(n, nn);
		}

		Graphs.addAllVertices(g, nodeMap.values());

		for(Edge e: srcGraph.edgeSet()) {
			N ns = nodeMap.get(srcGraph.getEdgeSource(e));
			N nd = nodeMap.get(srcGraph.getEdgeTarget(e));
			E newE = g.addEdge(ns, nd);
			newE.setCost(e.getCost());
		}

		return g;		
	}

	public static <N extends Node, E extends Edge> void serializeGraph(Graph<N,E> graph) throws Exception {
		FileChooser dlgFile = new FileChooser();
		dlgFile.setTitle("Select destination to save");
		File outFile = dlgFile.showSaveDialog(null);
		Element root = new Element("graph");
		Document document = new Document(root);

		for (N n : graph.vertexSet()) {
			Element ne = new Element("node");
			ne.setAttribute("id", Integer.toString(n.getId()));
			ne.setAttribute("posX", Double.toString(n.getPosition().getX()));
			ne.setAttribute("posY", Double.toString(n.getPosition().getY()));
			ne.setAttribute("cost", Double.toString(n.getCost()));
			root.addContent(ne);
		}

		for (E e : graph.edgeSet()) {
			Element ee = new Element("edge");
			ee.setAttribute("id", Integer.toString(e.getId()));
			ee.setAttribute("source", Integer.toString(graph.getEdgeSource(e).getId()));
			ee.setAttribute("dest", Integer.toString(graph.getEdgeTarget(e).getId()));
			ee.setAttribute("cost", Double.toString(e.getCost()));
			root.addContent(ee);
		}

		XMLOutputter domOutput = new XMLOutputter(Format.getPrettyFormat());
		domOutput.output(document, new FileOutputStream(outFile));
	}

	public static <N extends Node, E extends Edge> Graph<N,E> deSerializeGraph(VertexFactory<N> vfac, EdgeFactory<N,E> efac) throws Exception {
		FileChooser dlgFile = new FileChooser();
		dlgFile.setTitle("Select XML Graph");
		File inputFile = dlgFile.showOpenDialog(null);
		Document doc = new SAXBuilder().build(inputFile);
		Element root = doc.getRootElement();
		SimpleGraph<N,E> graph = new SimpleGraph<>(efac);
		HashMap<Number, N> nodeHash = new HashMap<>();
		Node.PropChanger npr = Node.PropChanger.create();
		Edge.PropChanger epr = Edge.PropChanger.create();

		// Get all the nodes
		for (Element ne : root.getChildren("node")) {
			int id = ne.getAttribute("id").getIntValue();
			double posX = ne.getAttribute("posX").getDoubleValue();
			double posY = ne.getAttribute("posY").getDoubleValue();
			double cost = ne.getAttribute("cost").getDoubleValue();
			N n = vfac.createVertex();
			n.setId(id);
			n.setCost(cost);
			npr.setPosition(n, new Point2D(posX, posY));
			nodeHash.put(id, n);
		}
		Graphs.addAllVertices(graph, nodeHash.values());

		// Get all the edges
		for (Element ee : root.getChildren("edge")) {
			int source = ee.getAttribute("source").getIntValue();
			int dest = ee.getAttribute("dest").getIntValue();
			int id = ee.getAttribute("id").getIntValue();
			double cost = ee.getAttribute("cost").getDoubleValue();
			N sn = nodeHash.get(source);
			N dn = nodeHash.get(dest);
			E e = efac.createEdge(sn, dn);
			e.setId(id);
			e.setCost(cost);
			graph.addEdge(sn, dn, e);
		}

		return graph;
	}
}
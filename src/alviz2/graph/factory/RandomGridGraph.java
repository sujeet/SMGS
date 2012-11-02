
package alviz2.graph.factory;

import java.util.Random;
import java.util.ArrayList;

import javafx.geometry.Point2D;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

import alviz2.graph.Node;
import alviz2.graph.Edge;
import alviz2.util.InputDialog;

public class RandomGridGraph implements GraphFactory {
	public String getName() {
		return "Random Grid Graph";
	}

	@Override
	public <N extends Node, E extends Edge> Graph<N,E> createGraph(VertexFactory<N> vfac, EdgeFactory<N,E> efac) {
		final Integer gridLen = InputDialog.getIntegerInput("Random Grid Factory", "Grid Size?", 4, Integer.MAX_VALUE);
		if (gridLen == null) {
			return null;
		}

		final Integer delPt = InputDialog.getIntegerInput("Random Grid Factory", "Edge retention ratio (0 - 100)", 0, 100);
		double delP = 0.0;
		if(delPt != null) {
			delP = delPt / 100.0;
		}

		Graph<N,E> graph = new SimpleGraph<>(efac);
		Node.PropChanger npr = Node.PropChanger.create();
		ArrayList<ArrayList<N>> nodes = new ArrayList<>(gridLen);

		for (int i=0; i<gridLen; i++) {
			ArrayList<N> nds = new ArrayList<>(gridLen);
			for (int j=0; j<gridLen; j++) {
				N n = vfac.createVertex();
				graph.addVertex(n);
				nds.add(n);
			}
			nodes.add(nds);
		}

		Random rnd = new Random();
		for (int i=0; i<gridLen-1; i++) {
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(0).get(i), nodes.get(0).get(i+1));	
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(0).get(i), nodes.get(1).get(i));
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(gridLen-1).get(i), nodes.get(gridLen-1).get(i+1));
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(gridLen-1).get(i), nodes.get(gridLen-2).get(i));
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(i).get(0), nodes.get(i+1).get(0));
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(i).get(0), nodes.get(i).get(1));
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(i).get(gridLen-1), nodes.get(i+1).get(gridLen-1));
			}
			
			if (rnd.nextDouble() < delP) {
				graph.addEdge(nodes.get(i).get(gridLen-1), nodes.get(i).get(gridLen-2));
			}
		}

		for (int i=1; i<gridLen-1; i++) {
			for (int j=1; j<gridLen-1; j++) {
				if (rnd.nextDouble() < delP) {
					graph.addEdge(nodes.get(i).get(j), nodes.get(i).get(j-1));
				}
				
				if (rnd.nextDouble() < delP) {
					graph.addEdge(nodes.get(i).get(j), nodes.get(i+1).get(j));
				}
			}
		}

		final int xScale = 20;
		final int yScale = 20;
		final int xDelta = 10;
		final int yDelta = 10;

		for (int i=0; i<gridLen; i++) {
			for (int j=0; j<gridLen; j++) {
				double x = (j+1) * xScale;
				double y = (i+1) * yScale;
				x += rnd.nextInt(xDelta);
				y += rnd.nextInt(yDelta);
				N n = nodes.get(i).get(j);
				npr.setPosition(n, new Point2D(x, y));
			}
		}

		return graph;
	}
	
}
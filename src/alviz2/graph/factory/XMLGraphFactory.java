
package alviz2.graph.factory;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.EdgeFactory;

import alviz2.util.GraphUtils;
import alviz2.graph.Node;
import alviz2.graph.Edge;

public class XMLGraphFactory implements GraphFactory {

	@Override 
	public String getName() {
		return "XML Graph Factory";
	}

	public <N extends Node, E extends Edge> Graph<N,E> createGraph(VertexFactory<N> vfac, EdgeFactory<N,E> efac) throws Exception {
		return GraphUtils.deSerializeGraph(vfac, efac);
	}
}
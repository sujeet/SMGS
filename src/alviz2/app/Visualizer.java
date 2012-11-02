
package alviz2.app;

import org.jgrapht.Graph;

import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import alviz2.graph.Node;
import alviz2.graph.Edge;
import alviz2.app.AppConfig;

class Visualizer {

	private Graph<? extends Node, ? extends Edge> graph;
	private Node.PropChanger npr;
	private Edge.PropChanger epr;
	private Canvas c;
	private double maxX, maxY;
	private double width, height;

	public Visualizer(Canvas cv, Graph<? extends Node, ? extends Edge> g, Node.PropChanger npr, Edge.PropChanger epr) {
		graph = g;
		this.npr = npr;
		this.epr = epr;
		this.c = cv;
		this.width = c.getWidth();
		this.height = c.getHeight();

		maxX = Double.MIN_VALUE;
		maxY = Double.MIN_VALUE;

		for (Node n: graph.vertexSet()) {
			Point2D p = n.getPosition();
			maxX = maxX > p.getX() ? maxX : p.getX();
			maxY = maxY > p.getY() ? maxY : p.getY();
		}
	}

	public void render() {

		final double xFactor = width/(maxX+10);
		final double yFactor = height/(maxY+10);

		GraphicsContext gc = c.getGraphicsContext2D();
		gc.clearRect(0, 0, width, height);

		for (Edge e: graph.edgeSet()) {
			if(!e.isVisible())
				continue;
			Point2D ps = e.getPositionS();
			Point2D pd = e.getPositionD();
			Point2D nps = new Point2D(ps.getX(), ps.getY());
			Point2D npd = new Point2D(pd.getX(), pd.getY());
			Color c = e.getStrokeColor();
			gc.setStroke(c);
			gc.setLineWidth(e.getBold() ? 4.0 : 1.0);
			gc.strokeLine(ps.getX() * xFactor, ps.getY() * yFactor, npd.getX() * xFactor, npd.getY() * yFactor);
		}
		gc.setLineWidth(1.0);

		for (Node n: graph.vertexSet()) {
			if(!n.isVisible())
				continue;
			Point2D p = n.getPosition();
			Color c = n.getFillColor();
			gc.setFill(c);
			gc.fillOval(p.getX() * xFactor, p.getY() * yFactor, 5, 5);
		}

		npr.clearChangedNodes();
		epr.clearChangedEdges();

	}
	
}

package alviz2.graph;

import java.util.Set;
import java.util.HashSet;

import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import alviz2.app.ColorPalette;
import alviz2.graph.Node;

public class Edge {

	private int id;
	private Node ns, nd;
	private Color strokeColor;
	private double cost;
	private boolean visible;
	private boolean bold;


	public static class PropChanger {
		private Set<Edge> changedEdges;

		private PropChanger() {
			changedEdges = new HashSet<>();
		}

		public static PropChanger create() {
			return new PropChanger();
		}

		public void setStrokeColor(Edge n, Color c) {
			changedEdges.add(n);
			n.setStrokeColor(c);
		}

		public void setVisible(Edge e, boolean v) {
			changedEdges.add(e);
			e.setVisible(v);
		}

		public Set<Edge> getChangedEdges() {
			return changedEdges;
		}

		public void clearChangedEdges() {
			changedEdges.clear();
		}

		public void setBold(Edge e, boolean b) {
			changedEdges.add(e);
			e.bold = b;
		}
	}

	public Edge(int id, Node s, Node d) {
		this.id = id;
		ns = s;
		nd = d;
		strokeColor = ColorPalette.getInstance().getColor("edge.default");
		cost = 0;
		visible = false;
		bold = false;
	}

	public boolean getBold() {
		return bold;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isVisible() {
		return visible;
	}

	private void setVisible(boolean v) {
		visible = v;
	}

	public final double getCost() {
		return cost;
	}

	public final void setCost(double c) {
		cost = c;
	}

	public final Color getStrokeColor() {
		return strokeColor;
	}

	private void setStrokeColor(Color c) {
		strokeColor = c;
	}

	public final Point2D getPositionS() {
		return ns.getPosition();
	}

	public final Point2D getPositionD() {
		return nd.getPosition();
	}
	
}
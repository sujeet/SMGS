
package alviz2.graph;

import java.util.Set;
import java.util.HashSet;

import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import alviz2.app.ColorPalette;

public class Node {

	private int id;
	private Point2D pos;
	Color fillColor;
	double cost;
	boolean visible;
	int prune_count; // Prune off when reaches 0
	// prune_count = -1 means never prune.
	
	public void reducePruneCount () {
	    if (prune_count > 0) prune_count -= 1;
	}
	
	public void makeUnprunable () {
	    prune_count = -1;
	}
	
	public boolean shouldBePruned () {
	    if (prune_count == 0) return true;
	    return false;
	}
	
	public void setPruneCount (int prn_cnt) {
	    prune_count = prn_cnt;
	}

	public static class PropChanger {
		private Set<Node> changedNodes;

		private PropChanger() {
			changedNodes = new HashSet<>();
		}

		public static PropChanger create() {
			return new PropChanger();
		}

		public void setFillColor(Node n, Color c) {
			changedNodes.add(n);
			n.setFillColor(c);
		}

		public void setVisible(Node n, boolean v) {
			changedNodes.add(n);
			n.setVisible(v);
		}

		public void setPosition(Node n, Point2D pt) {
			n.setPosition(pt);
		}

		public Set<Node> getChangedNodes() {
			return changedNodes;
		}

		public void clearChangedNodes() {
			changedNodes.clear();
		}
	}

	public Node(int id) {
		this.id = id;
		pos = new Point2D(0,0);
		fillColor = ColorPalette.getInstance().getColor("node.default");
		cost = 0;
		visible = false;
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

	public final Color getFillColor() {
		return fillColor;
	}

	private void setFillColor(Color c) {
		fillColor = c;
	}

	public final Point2D getPosition() {
		return pos;
	}

	private void setPosition(Point2D pt) {
		pos = pt;
	}
	
}
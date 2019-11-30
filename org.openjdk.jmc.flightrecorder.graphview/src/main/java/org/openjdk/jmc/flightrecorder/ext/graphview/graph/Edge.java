package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

/**
 * An edge in the graph of aggregated stack traces.
 */
public class Edge {
	private final Node from;
	private final Node to;
	int count;
	double value;

	/**
	 * Constructor.
	 * 
	 * @param from non null from node.
	 * @param to   non null to node.
	 */
	public Edge(Node from, Node to) {
		if (from == null || to == null) {
			throw new NullPointerException("Nodes must not be null");
		}
		this.from = from;
		this.to = to;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	public int getCount() {
		return count;
	}

	public double getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from.hashCode();
		result = prime * result + to.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (!from.equals(other.from)) {
			return false;
		}
		if (!to.equals(other.to)) {
			return false;
		}
		return true;
	}
}
package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

/**
 * A node in the graph of aggregated stacktraces.
 */
public class Node {
	/**
	 * Integer uniquely identifying this node within the graph instance.
	 */
	private final Integer nodeId;

	/**
	 * The frame associated with this node.
	 */
	private final AggregatableFrame frame;

	// If we want to optimize for memory, these could be calculated later,
	// and only the frames used.
	/**
	 * The number of times being the top frame.
	 */
	int count;

	/**
	 * The number of times found in any stack-trace.
	 */
	int cumulativeCount;

	/**
	 * The weight when being the top frame.
	 */
	double weight;

	/**
	 * The cumulative weight for all contributions.
	 */
	double cumulativeWeight;

	public Node(Integer nodeId, AggregatableFrame frame) {
		this.nodeId = nodeId;
		this.frame = frame;
		if (frame == null) {
			throw new NullPointerException("Frame cannot be null!");
		}
	}

	/**
	 * @return the number of times this node was on the top of the stack.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the weight of this node.
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @return the number of times this node was passed through.
	 */
	public int getCumulativeCount() {
		return cumulativeCount;
	}

	public AggregatableFrame getFrame() {
		return frame;
	}

	@Override
	public int hashCode() {
		// This will get a few extra collisions.
		return frame.getMethod().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (frame == null) {
			if (other.frame != null)
				return false;
		}
		return true;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	@Override
	public String toString() {
		return String.format("%s:%d(%d)", frame.toString(), count, cumulativeCount);
	}
}

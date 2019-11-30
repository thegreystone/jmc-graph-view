package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

/**
 * Renders a {@link StacktraceGraphModel} in the Json format used by Cytoscape.
 * 
 * @see https://js.cytoscape.org.
 */
public final class CytoscapeGenerator {
	
	/**
	 * Renders a {@link StacktraceGraphModel} in DOT format.
	 */
	public static String toCytoScapeJSon(StacktraceGraphModel model) {
		StringBuilder builder = new StringBuilder(2048);
		
		// Convert to cytoscape format
		
		return builder.toString();
	}
}

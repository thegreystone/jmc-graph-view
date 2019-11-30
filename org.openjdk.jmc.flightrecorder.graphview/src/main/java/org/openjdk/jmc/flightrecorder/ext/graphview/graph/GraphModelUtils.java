package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator.FrameCategorization;

public class GraphModelUtils {
	public final static FrameSeparator DEFAULT_FRAME_SEPARATOR = new FrameSeparator(FrameCategorization.METHOD, false);

	public static String printGraph(StacktraceGraphModel model) {
		StringBuilder builder = new StringBuilder();
		builder.append("=== Graph Printout ===\n");
		printNodes(builder, model.getNodes());
		printLinks(builder, model.getEdges());
		return builder.toString();
	}

	public static String getTypeNames(IItemCollection items) {
		List<String> typeNames = new LinkedList<>();
		items.forEach((iterable) -> typeNames.add(iterable.getType().getName()));
		return String.join(", ", typeNames);

	}

	private static void printLinks(StringBuilder builder, Collection<Edge> edges) {
		builder.append("Number of edges:");
		builder.append(edges.size());
		builder.append("\n");
	}

	private static void printNodes(StringBuilder builder, Collection<Node> nodes) {
		builder.append("Number of nodes:");
		builder.append(nodes.size());
		builder.append("\n");
	}
}

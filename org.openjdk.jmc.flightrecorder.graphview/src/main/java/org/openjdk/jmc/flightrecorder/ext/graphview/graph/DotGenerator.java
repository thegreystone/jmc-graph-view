package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator.FrameCategorization;

/**
 * Converts a {@link StacktraceGraphModel} to DOT format text. This format can, for example, be used
 * by d3-graphviz, to visualize the graphs.
 */
public final class DotGenerator {
	private static final String DEFAULT_EDGE_STYLE = "dotted";
	private static final String DEFAULT_MIN_EDGE_WEIGHT = "1";
	private static final String DEFAULT_MAX_EDGE_WEIGHT = "40";
	private static final String DEFAULT_NODE_SIZE_ATTRIBUTE = "count";
	private static final String DEFAULT_NAME = "Unnamed";
	private static final String DEFAULT_SHAPE = "box";
	private static final String DEFAULT_STYLE = "filled";
	private static final String DEFAULT_FILL_COLOR = "#f8f8f8";
	private static final String DEFAULT_TITLE_FONT_SIZE = "16";
	private static final String DEFAULT_MIN_NODE_FONT_SIZE = "8";
	private static final String DEFAULT_MAX_NODE_FONT_SIZE = "32";

	public enum ConfigurationKey {
		/**
		 * The name of the graph.
		 */
		Name,
		/**
		 * The style, e.g. filled.
		 */
		Style,
		/**
		 * The fill color, e.g. #f8f8f8.
		 */
		Fillcolor,
		/**
		 * Font size of the title area, e.g. 16. Need to be parseable to a number.
		 */
		TitleFontSize,
		/**
		 * Shape of the title area, e.g. box.
		 */
		TitleShape,
		/**
		 * Shape of the nodes, e.g. box.
		 */
		NodeShape,
		/**
		 * The color of the node, e.g. #b22b00.
		 */
		NodeColor,
		/**
		 * The fill color of the node, e.g. #eddbd5.
		 */
		NodeFillColor,
		/**
		 * Font size for the biggest node. This will be the font size used for the node with the
		 * biggest count or weight.
		 */
		MaxNodeFontSize,
		/**
		 * Font size for the biggest node. This will be the font size used for the node with the
		 * smallest count or weight.
		 */
		MinNodeFontSize,
		/**
		 * The attribute to use for node font size. [count|weight]
		 */
		NodeSizeAttribute,
		/**
		 * The attribute to use for the edge weights. [count|weight]
		 */
		EdgeWeightAttribute,
		/**
		 * The style for the edges, e.g. dotted.
		 */
		EdgeStyle,
		/**
		 * The max edge weight to use for the most traveled path.
		 */
		MaxEdgeWeight,
		/**
		 * The min edge weight to use for the least traveled path.
		 */
		MinEdgeWeight

	}

	private final static class NodeConfigurator {
		private static final String COUNT = DEFAULT_NODE_SIZE_ATTRIBUTE;
		private final String shape;
		private final boolean useCount;
		private final double minCount;
		private final double maxCount;
		private final double minRange;
		private final double maxRange;
		private final int minFontSize;
		private final int maxFontSize;
		private final String color;
		private final String fillColor;

		public NodeConfigurator(StacktraceGraphModel model, Map<ConfigurationKey, String> configuration) {
			useCount = getConf(configuration, ConfigurationKey.NodeSizeAttribute, COUNT).equals(COUNT);
			minCount = model.findNodeMinCount();
			maxCount = model.findNodeMaxCount();

			if (useCount) {
				maxRange = maxCount;
				minRange = minCount;
			} else {
				maxRange = model.findNodeMaxWeight();
				minRange = model.findNodeMinWeight();
			}
			maxFontSize = Integer
					.parseInt(getConf(configuration, ConfigurationKey.MaxNodeFontSize, DEFAULT_MAX_NODE_FONT_SIZE));
			minFontSize = Integer
					.parseInt(getConf(configuration, ConfigurationKey.MinNodeFontSize, DEFAULT_MIN_NODE_FONT_SIZE));
			shape = getConf(configuration, ConfigurationKey.NodeShape, DEFAULT_SHAPE);
			color = getConf(configuration, ConfigurationKey.NodeColor, "#b22b00");
			fillColor = getConf(configuration, ConfigurationKey.NodeFillColor, "#eddbd5");
		}

		public int getFontSize(Node node) {
			double value = useCount ? node.getCount() : node.getWeight();
			double fraction = (value - minRange) / (maxRange - minRange);
			return (int) Math.round((maxFontSize - minFontSize) * fraction + minFontSize);
		}
	}

	private final static class EdgeConfigurator {
		private final boolean useCount;
		private final double minWeight;
		private final double maxWeight;
		private final int minCount;
		private final int maxCount;
		private final double minRange;
		private final double maxRange;
		private final String style;

		public EdgeConfigurator(StacktraceGraphModel model, Map<ConfigurationKey, String> configuration) {
			useCount = getConf(configuration, ConfigurationKey.NodeSizeAttribute, DEFAULT_NODE_SIZE_ATTRIBUTE)
					.equals(DEFAULT_NODE_SIZE_ATTRIBUTE);
			minCount = model.findEdgeMinCount();
			maxCount = model.findEdgeMaxCount();

			if (useCount) {
				minRange = minCount;
				maxRange = maxCount;
			} else {
				minRange = model.findEdgeMinValue();
				maxRange = model.findEdgeMaxValue();
			}

			minWeight = Integer
					.parseInt(getConf(configuration, ConfigurationKey.MinEdgeWeight, DEFAULT_MIN_EDGE_WEIGHT));
			maxWeight = Integer
					.parseInt(getConf(configuration, ConfigurationKey.MaxEdgeWeight, DEFAULT_MAX_EDGE_WEIGHT));
			style = getConf(configuration, ConfigurationKey.EdgeStyle, DEFAULT_EDGE_STYLE);
		}

		public String generateTooltip(Edge e) {
			return e.getFrom().getFrame().getHumanReadableSeparatorSensitiveString() + " -> "
					+ e.getTo().getFrame().getHumanReadableSeparatorSensitiveString() + " (" + getPercentage(e) + " %)";
		}

		private String getPercentage(Edge e) {
			double val = 0;
			if (useCount) {
				val = ((double) e.getCount()) / maxCount;
			} else {
				val = e.getValue() / maxRange;
			}
			return String.format("%.3f", val);
		}

		/**
		 * This is the weight for the edge, not the edge value.
		 */
		public int getWeight(Edge edge) {
			double value = useCount ? edge.getCount() : edge.getValue();
			double fraction = (value - minRange) / (maxRange - minRange);
			return (int) Math.round((maxWeight - minWeight) * fraction + minWeight);
		}

		public boolean isMax(Edge edge) {
			if (useCount) {
				return edge.getCount() == maxCount;
			} else {
				return edge.getValue() == maxRange;
			}
		}

		public String getColor(Edge edge) {
			// if weight == 0, then have as gray as possible,
			// if weight == MAX_WEIGHT, keep it red.
			// TODO Auto-generated method stub
			int color = 0xb2 << 16;
			double value = useCount ? edge.getCount() : edge.getValue();
			double fraction = (value - minRange) / (maxRange - minRange);
			int colorval = (int) ((1 - fraction) * 0xb2);
			color = color | (colorval << 8) | colorval;
			return "#" + Integer.toHexString(color);
		}

	}

	/**
	 * Renders a {@link StacktraceGraphModel} in DOT format.
	 */
	public static String toDot(StacktraceGraphModel model, Map<ConfigurationKey, String> configuration) {
		StringBuilder builder = new StringBuilder(2048);
		String graphName = getConf(configuration, ConfigurationKey.Name, DEFAULT_NAME);
		builder.append(String.format("digraph \"%s\" {\n", graphName));
		createSubgraphNode(builder, graphName, configuration, model);

		// Convert Nodes
		NodeConfigurator nodeConfigurator = new NodeConfigurator(model, configuration);
		model.getNodes().forEach((node) -> emitNode(builder, model, nodeConfigurator, node));

		// Convert Edges
		EdgeConfigurator edgeConfigurator = new EdgeConfigurator(model, configuration);
		model.getEdges().forEach((edge) -> emitEdge(builder, model, edgeConfigurator, edge));

		builder.append("}");
		return builder.toString();
	}

	private static void emitEdge(
		StringBuilder builder, StacktraceGraphModel model, EdgeConfigurator edgeConfigurator, Edge edge) {
		builder.append("N");
		builder.append(edge.getFrom().getNodeId());
		builder.append(" -> N");
		builder.append(edge.getTo().getNodeId());
		builder.append(" [label=\"");
		if (edgeConfigurator.useCount) {
			builder.append(edge.count);
		} else {
			builder.append(edge.value);
		}
		int weight = edgeConfigurator.getWeight(edge);
		if (weight >= 2) {
			builder.append("\" weight=");
			builder.append(weight);
		}
		builder.append(edgeConfigurator.isMax(edge) ? " penwidth=2 " : " ");
		builder.append("color=\"");
		builder.append(edgeConfigurator.getColor(edge));
		builder.append("\" tooltip=\"");
		String tooltip = edgeConfigurator.generateTooltip(edge);
		builder.append(tooltip);
		builder.append("\" labeltooltip=\"");
		builder.append(tooltip);
		builder.append("\" style=\"");
		builder.append(edgeConfigurator.style);
		builder.append("\"]\n");
	}

	private static void emitNode(
		StringBuilder builder, StacktraceGraphModel model, NodeConfigurator configurator, Node node) {
		double percentOfSamples = node.getCount() * 100.0 / model.getTotalTraceCount();
		builder.append("N");
		builder.append(node.getNodeId());
		builder.append(" [label=\"");
		builder.append(node.getFrame().getHumanReadableSeparatorSensitiveString());
		builder.append("\\nSamples: ");
		builder.append(node.getCount());
		builder.append(" (");
		builder.append(percentOfSamples);
		builder.append(" %) id=\"node");
		builder.append(node.getNodeId());
		builder.append("\" fontsize=\"");
		builder.append(configurator.getFontSize(node));
		builder.append("\" shape=");
		builder.append(configurator.shape);
		builder.append("\" tooltip=\"");
		builder.append(node.getFrame().getHumanReadableSeparatorSensitiveString());
		builder.append(" (");
		builder.append(percentOfSamples);
		builder.append(" %) color=\"");
		builder.append(configurator.color);
		builder.append("\" fillcolor=\"");
		builder.append(configurator.fillColor);
		builder.append("\"]\n");
	}

	private static void createSubgraphNode(
		StringBuilder builder, String graphName, Map<ConfigurationKey, String> configuration,
		StacktraceGraphModel model) {
		builder.append("subgraph cluster_L { ");
		builder.append("\"");
		builder.append(graphName);
		builder.append(" ");
		builder.append("[shape=");
		builder.append(getConf(configuration, ConfigurationKey.TitleShape, DEFAULT_SHAPE));
		builder.append(" fontsize=");
		builder.append(getConf(configuration, ConfigurationKey.TitleFontSize, DEFAULT_TITLE_FONT_SIZE));
		builder.append(" label=\"");
		builder.append(graphName);
		builder.append("\\lTypes: ");
		builder.append(GraphModelUtils.getTypeNames(model.getItems()));
		builder.append("\\lTotal samples = ");
		builder.append(model.getTotalTraceCount());
		builder.append("\\lTotal edge count = ");
		builder.append(model.getTotalEdgeCount());
		builder.append("\" tooltip=\"");
		builder.append(graphName);
		builder.append("\"] }\n");
	}

	/**
	 * @return an example configuration for the dot files, using the defaults.
	 */
	public static Map<ConfigurationKey, String> getDefaultConfiguration() {
		Map<ConfigurationKey, String> configuration = new HashMap<>();
		configuration.put(ConfigurationKey.Name, DEFAULT_NAME);
		configuration.put(ConfigurationKey.Fillcolor, DEFAULT_FILL_COLOR);
		configuration.put(ConfigurationKey.Style, DEFAULT_STYLE);
		configuration.put(ConfigurationKey.TitleShape, DEFAULT_SHAPE);
		configuration.put(ConfigurationKey.TitleFontSize, DEFAULT_TITLE_FONT_SIZE);
		configuration.put(ConfigurationKey.NodeShape, DEFAULT_SHAPE);
		configuration.put(ConfigurationKey.NodeSizeAttribute, DEFAULT_NODE_SIZE_ATTRIBUTE);
		configuration.put(ConfigurationKey.MaxNodeFontSize, DEFAULT_MAX_NODE_FONT_SIZE);
		configuration.put(ConfigurationKey.MinNodeFontSize, DEFAULT_MIN_NODE_FONT_SIZE);
		configuration.put(ConfigurationKey.MaxEdgeWeight, DEFAULT_MAX_EDGE_WEIGHT);
		configuration.put(ConfigurationKey.MinEdgeWeight, DEFAULT_MIN_EDGE_WEIGHT);
		configuration.put(ConfigurationKey.EdgeStyle, DEFAULT_EDGE_STYLE);
		return configuration;
	}

	private static String getConf(
		Map<ConfigurationKey, String> configuration, ConfigurationKey key, String defaultValue) {
		String value = configuration.get(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Generates a dot file for the CPU profiling events available in the recording.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws CouldNotLoadRecordingException
	 */
	public static void main(String[] args) throws IOException, CouldNotLoadRecordingException {
		IItemCollection items = JfrLoaderToolkit.loadEvents(new File(args[0]));
		IItemCollection filteredItems = items.apply(JdkFilters.EXECUTION_SAMPLE);
		FrameSeparator frameSeparator = new FrameSeparator(FrameCategorization.METHOD, false);
		StacktraceGraphModel model = new StacktraceGraphModel(frameSeparator, filteredItems, null);
		System.out.println(toDot(model, getDefaultConfiguration()));
	}
}
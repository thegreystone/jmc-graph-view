package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openjdk.jmc.common.item.Aggregators;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;

class GraphModelTest {

	@Test
	void testBuildGraph() throws IOException, CouldNotLoadRecordingException {
		IItemCollection events = JfrLoaderToolkit.loadEvents(GraphModelTest.class.getResourceAsStream("hotmethods.jfr"));
		assertTrue(events.hasItems());
		IItemCollection executionSamples = events.apply(JdkFilters.EXECUTION_SAMPLE);
		assertTrue(executionSamples.hasItems());
		assertEquals(executionSamples.getAggregate(Aggregators.count()).longValue(), 24526);
		StacktraceGraphModel model = new StacktraceGraphModel(GraphModelUtils.DEFAULT_FRAME_SEPARATOR, executionSamples, null);
		assertFalse("No nodes!", model.getNodes().isEmpty());
		assertFalse("No edges!", model.getEdges().isEmpty());
	}

	
	public static void main(String [] args) throws IOException, CouldNotLoadRecordingException {
		new GraphModelTest().testBuildGraph();
	}
}

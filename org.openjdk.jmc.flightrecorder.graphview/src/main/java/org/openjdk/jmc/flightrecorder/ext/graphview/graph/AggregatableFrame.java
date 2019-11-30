package org.openjdk.jmc.flightrecorder.ext.graphview.graph;

import org.openjdk.jmc.common.IMCFrame;
import org.openjdk.jmc.common.IMCMethod;
import org.openjdk.jmc.common.util.FormatToolkit;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator;

/**
 * Frame wrapper taking into account a frame separator for hash code and equals.
 */
public final class AggregatableFrame implements IMCFrame {
	private final FrameSeparator separator;
	private final IMCFrame frame;

	/**
	 * Constructor.
	 * 
	 * @param separator can't be null.
	 * @param frame     can't be null.
	 */
	public AggregatableFrame(FrameSeparator separator, IMCFrame frame) {
		if (separator == null) {
			throw new NullPointerException("Separator must not be null");
		} else if (frame == null) {
			throw new NullPointerException("Frame must not be null");
		}
		this.separator = separator;
		this.frame = frame;
	}

	@Override
	public Integer getFrameLineNumber() {
		return frame.getFrameLineNumber();
	}

	@Override
	public Integer getBCI() {
		return frame.getBCI();
	}

	@Override
	public IMCMethod getMethod() {
		return frame.getMethod();
	}

	@Override
	public Type getType() {
		return frame.getType();
	}

	@Override
	public int hashCode() {
		switch (separator.getCategorization()) {
		case LINE:
			return frame.getMethod().hashCode() + 31 * frame.getFrameLineNumber();
		case METHOD:
			return frame.getMethod().hashCode();
		case CLASS:
			return frame.getMethod().getType().hashCode();
		case PACKAGE:
			return frame.getMethod().getType().getPackage().hashCode();
		case BCI:
		}
		return frame.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AggregatableFrame other = (AggregatableFrame) obj;
		return !separator.isSeparate(this.frame, other.frame);
	}
	
	@Override
	public String toString() {
		return FormatToolkit.getHumanReadable(getMethod()) + ":" + separator.getCategorization();
	}
	
	public String getHumanReadableSeparatorSensitiveString() {
		switch (separator.getCategorization()) {
		case LINE:
			return FormatToolkit.getHumanReadable(getMethod()) + ":" + frame.getFrameLineNumber();
		case METHOD:
			return FormatToolkit.getHumanReadable(getMethod());
		case CLASS:
			return frame.getMethod().getType().getFullName();
		case PACKAGE:
			return frame.getMethod().getType().getPackage().getName();
		default:
			return FormatToolkit.getHumanReadable(getMethod()) + ":" + frame.getFrameLineNumber() + "(" + getBCI() + ")";
		}
	}
}

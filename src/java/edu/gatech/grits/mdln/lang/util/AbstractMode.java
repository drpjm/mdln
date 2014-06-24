package edu.gatech.grits.mdln.lang.util;

/**
 * Abstract class that all motion description type modes inherit from.
 * @author pmartin
 *
 */
public abstract class AbstractMode {
	
	protected String id;
	protected ControlAdapter control;
	protected long timerLength;
//	public final static long INF_TIMER = -1;
	public static final long INF_TIMER = -1;

	
	public String getId() {
		return id;
	}
	public ControlAdapter getControl() {
		return control;
	}
	public long getTimerLength() {
		return timerLength;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setControl(ControlAdapter control) {
		this.control = control;
	}
	public void setTimerLength(long timerLength) {
		this.timerLength = timerLength;
	}
	@Override
	public String toString() {
		return id;
	}

	
}

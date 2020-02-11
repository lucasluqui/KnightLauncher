package xyz.lucasallegri.net;

public class ProjectXEvent {
	
	protected String eventName;
	protected String contentUrl;
	protected int beganAt;
	protected int endsAt;
	
	public ProjectXEvent() {
		this.eventName = null;
		this.contentUrl = null;
		this.beganAt = 0;
		this.endsAt = 0;
	}
	
	@Override
	public String toString() {
		return "[event=" + this.eventName + ",contentUrl=" + this.contentUrl + ",beganAt=" + this.beganAt + ",endsAt=" + this.endsAt + "]";
	}

}

package cz.cvut.sigmet.model;

public class LogDTO {

	public enum Level{
		INFO,
		WARN,
		ERROR
	}
	
	private Level level;
	private String message;
	private long timestamp;
	
	public LogDTO(){
		
	}
	
	public LogDTO(Level level, String message, long timestamp) {
		super();
		this.level = level;
		this.message = message;
		this.timestamp = timestamp;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "LogDTO [level=" + level + ", message=" + message + ", timestamp=" + timestamp + "]";
	}

}

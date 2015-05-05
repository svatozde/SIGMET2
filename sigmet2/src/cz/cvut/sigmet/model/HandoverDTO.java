package cz.cvut.sigmet.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "handover")
public class HandoverDTO {
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private CellDTO from;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private CellDTO to;
	@DatabaseField
	private double latitude;
	@DatabaseField
	private double longtitude;
	@DatabaseField
	private long timestamp;
	@DatabaseField(foreign = true, foreignAutoRefresh = false, canBeNull = true)
	private WalkDTO walk;

	public HandoverDTO() {
		// needed by ormlite
	}

	public HandoverDTO(CellDTO from, CellDTO to, double latitude, double longtitude, long timestamp) {
		super();
		this.from = from;
		this.to = to;
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.timestamp = timestamp;
	}

	public CellDTO getFrom() {
		return from;
	}

	public void setFrom(CellDTO from) {
		this.from = from;
	}

	public CellDTO getTo() {
		return to;
	}

	public void setTo(CellDTO to) {
		this.to = to;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public WalkDTO getWalk() {
		return walk;
	}

	public void setWalk(WalkDTO walk) {
		this.walk = walk;
	}

	@Override
	public String toString() {
		return "HandoverDTO [from=" + from + ", to=" + to + ", latitude=" + latitude + ", longtitude=" + longtitude + ", timestamp=" + timestamp + ", walk="
				+ walk + "]";
	}

}

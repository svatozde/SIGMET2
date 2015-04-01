package cz.cvut.sigmet.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="signal")
public class SignalDTO {
	@DatabaseField(foreign = true, foreignAutoRefresh = false)
	private CellDTO cell;	
	//this value enables us to reconstruct walk with all measured signals
	//in case walk recording is notenbaled this value is null
	@DatabaseField(foreign = true, foreignAutoRefresh = false, canBeNull=true)
	private WalkDTO measure;	
	@DatabaseField
	private double latitude;
	@DatabaseField
	private double longtitude;
	@DatabaseField
	private int value;
	@DatabaseField
	private long timestamp;
	

	public SignalDTO() {
		// needed by ormlite
	}
	
	public SignalDTO(CellDTO id, double latitude, double longtitude, int value, long timestamp) {
		super();
		this.cell = id;
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.value = value;
		this.timestamp = timestamp;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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
	public CellDTO getCell() {
		return cell;
	}
	public void setCell(CellDTO id) {
		this.cell = id;
	}
}
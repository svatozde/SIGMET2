package cz.cvut.sigmet.model;

import android.view.View.MeasureSpec;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="location")
public class LocationDTO {
	@DatabaseField(id = true)
	private String geohash;
	@DatabaseField
	private int mesaureCount = 0;
	@DatabaseField
	private long sum = 0;
	@DatabaseField
	private double latitude;
	@DatabaseField
	private double longitude;
	
	
	public LocationDTO() {
		super();
	}
	public LocationDTO(String geohash) {
		super();
	}
	
	public LocationDTO(String geohash, int mesaureCount, long sum, double latitude, double longitude) {
		super();
		this.geohash = geohash;
		this.mesaureCount = mesaureCount;
		this.sum = sum;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	public String getGeohash() {
		return geohash;
	}
	public void setGeohash(String geohash) {
		this.geohash = geohash;
	}
	public int getMesaureCount() {
		return mesaureCount;
	}
	public void setMesaureCount(int mesaureCount) {
		this.mesaureCount = mesaureCount;
	}
	public long getSum() {
		return sum;
	}
	public void setSum(long sum) {
		this.sum = sum;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void addSignal(int value){
		sum+=value;
		mesaureCount++;
	}
	
	public int getAvg(){
		return (int) (sum/mesaureCount);
	}
}

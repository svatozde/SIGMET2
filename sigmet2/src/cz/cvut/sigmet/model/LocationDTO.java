package cz.cvut.sigmet.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="location")
public class LocationDTO {
	@DatabaseField(generatedIdSequence = "location_id_seq")
	private long id;
	@DatabaseField
	private String geohash;
	
	public LocationDTO(long id, String geohash) {
		super();
		this.id = id;
		this.geohash = geohash;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getGeohash() {
		return geohash;
	}
	public void setGeohash(String geohash) {
		this.geohash = geohash;
	}
	@Override
	public String toString() {
		return "LocationDTO [id=" + id + ", geohash=" + geohash + "]";
	}

}

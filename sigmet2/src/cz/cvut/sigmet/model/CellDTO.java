package cz.cvut.sigmet.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cells")
public class CellDTO {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField
	private double latitude;

	@DatabaseField
	private double longtitude;

	@DatabaseField(canBeNull = false)
	private String cid;

	@DatabaseField(canBeNull = false)
	private String lac;

	@DatabaseField
	private String hex;

	@DatabaseField
	private String bch;

	@DatabaseField
	private String bsic;

	@DatabaseField
	private String date;

	@DatabaseField
	private String addres;

	public CellDTO() {
		// needed by ormlite
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getCId() {
		return cid;
	}

	public void setCId(String cId) {
		cid = cId;
	}

	public String getHex() {
		return hex;
	}

	public void setHex(String hex) {
		this.hex = hex;
	}

	public String getLac() {
		return lac;
	}

	public void setLac(String lac) {
		this.lac = lac;
	}

	public String getBch() {
		return bch;
	}

	public void setBch(String bch) {
		this.bch = bch;
	}

	public String getBsic() {
		return bsic;
	}

	public void setBsic(String bsic) {
		this.bsic = bsic;
	}

	public String getAddres() {
		return addres;
	}

	public void setAddres(String addres) {
		this.addres = addres;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellDTO other = (CellDTO) obj;
		if (addres == null) {
			if (other.addres != null)
				return false;
		} else if (!addres.equals(other.addres))
			return false;
		if (bch == null) {
			if (other.bch != null)
				return false;
		} else if (!bch.equals(other.bch))
			return false;
		if (bsic == null) {
			if (other.bsic != null)
				return false;
		} else if (!bsic.equals(other.bsic))
			return false;
		if (cid == null) {
			if (other.cid != null)
				return false;
		} else if (!cid.equals(other.cid))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (hex == null) {
			if (other.hex != null)
				return false;
		} else if (!hex.equals(other.hex))
			return false;
		if (id != other.id)
			return false;
		if (lac == null) {
			if (other.lac != null)
				return false;
		} else if (!lac.equals(other.lac))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longtitude) != Double.doubleToLongBits(other.longtitude))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addres == null) ? 0 : addres.hashCode());
		result = prime * result + ((bch == null) ? 0 : bch.hashCode());
		result = prime * result + ((bsic == null) ? 0 : bsic.hashCode());
		result = prime * result + ((cid == null) ? 0 : cid.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((hex == null) ? 0 : hex.hashCode());
		result = prime * result + id;
		result = prime * result + ((lac == null) ? 0 : lac.hashCode());
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longtitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "CellDTO [id=" + id + ", latitude=" + latitude + ", longtitude=" + longtitude + ", cid=" + cid + ", lac=" + lac + ", hex=" + hex + ", bch="
				+ bch + ", bsic=" + bsic + ", date=" + date + ", addres=" + addres + "]";
	}

	public boolean isSameLocation(CellDTO dto) {
		return dto != null && (this.latitude == dto.getLatitude() && this.longtitude == dto.getLongtitude());
	}

}

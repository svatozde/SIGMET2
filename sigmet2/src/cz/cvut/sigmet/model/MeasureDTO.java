package cz.cvut.sigmet.model;

import com.j256.ormlite.field.DatabaseField;

public class MeasureDTO {
	@DatabaseField(foreign = true, foreignAutoRefresh = false,canBeNull=false)
	private CellDTO from;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, canBeNull=false)
	private WalkDTO measure;	
}

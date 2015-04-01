package cz.cvut.sigmet.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "walks")
public class WalkDTO {
	@DatabaseField(generatedId = true)
	private int id;
		
	@DatabaseField
	private int name;

}

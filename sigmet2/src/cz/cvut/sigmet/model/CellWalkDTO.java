package cz.cvut.sigmet.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="cellMeasure")
public class CellWalkDTO {
	@DatabaseField(foreign = true, foreignAutoRefresh = false,canBeNull=false)
	private CellDTO cell;
	
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false, canBeNull=false)
	private WalkDTO walk;

	public CellWalkDTO() {
		super();
	}

	public CellDTO getCell() {
		return cell;
	}

	public void setCell(CellDTO dto) {
		this.cell = dto;
	}

	public WalkDTO getWalk() {
		return walk;
	}

	public void setWalk(WalkDTO walk) {
		this.walk = walk;
	}	
}

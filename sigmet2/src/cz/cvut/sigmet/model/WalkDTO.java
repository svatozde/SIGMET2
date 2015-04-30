package cz.cvut.sigmet.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "walks")
public class WalkDTO {
	@DatabaseField(generatedId = true)
	private int id;
		
	@DatabaseField
	private String name;
	
	@DatabaseField
	private long start;
	
	@DatabaseField(canBeNull=true)
	private long stop;
	
	@ForeignCollectionField
	private ForeignCollection<HandoverDTO> handovers;
	
	@ForeignCollectionField
	private ForeignCollection<SignalDTO> signals;

	public WalkDTO() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStop() {
		return stop;
	}

	public void setStop(long stop) {
		this.stop = stop;
	}

	public ForeignCollection<HandoverDTO> getHandovers() {
		return handovers;
	}

	public void setHandovers(ForeignCollection<HandoverDTO> handovers) {
		this.handovers = handovers;
	}

	public ForeignCollection<SignalDTO> getSignals() {
		return signals;
	}

	public void setSignals(ForeignCollection<SignalDTO> signals) {
		this.signals = signals;
	}

}

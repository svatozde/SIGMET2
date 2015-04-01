package cz.cvut.sigmet.dbUtils;

import cz.cvut.sigmet.model.CellDTO;
import cz.cvut.sigmet.model.HandoverDTO;
import cz.cvut.sigmet.model.SignalDTO;

public interface SigmetDataListener {
	
	public void onCellChange(CellDTO dto);
	
	public void onLocationCange(SignalDTO dto);
	
	
}

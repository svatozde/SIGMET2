package cz.cvut.sigmet.gsmWebUtils;

import java.util.List;

import cz.cvut.sigmet.model.CellDTO;


public interface GsmWebManager {


	public CellDTO getGsmInfo(String cid, String lac) throws Exception;
	
	
	public List<CellDTO> getGsmInfo(String cid) throws Exception;
	

}

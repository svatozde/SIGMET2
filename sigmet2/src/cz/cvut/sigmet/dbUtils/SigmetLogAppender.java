package cz.cvut.sigmet.dbUtils;

import cz.cvut.sigmet.model.LogDTO;

public interface SigmetLogAppender {

	public void append(LogDTO log);

}

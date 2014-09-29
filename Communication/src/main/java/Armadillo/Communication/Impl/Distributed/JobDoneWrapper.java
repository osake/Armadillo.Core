package Armadillo.Communication.Impl.Distributed;

import org.joda.time.DateTime;

public class JobDoneWrapper {

	public boolean m_blnSucessDone;
	public DateTime m_dateCreated;
	
	public JobDoneWrapper(Boolean blnSucessDone) {
		m_blnSucessDone = blnSucessDone;
		m_dateCreated = DateTime.now();
	}

}

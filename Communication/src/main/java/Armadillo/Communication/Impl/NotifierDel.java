package Armadillo.Communication.Impl;

import Armadillo.Core.Logger;

public class NotifierDel {

	public void invoke(String str) {
		try {
			throw new Exception();
		} catch (Exception ex) {
			Logger.log(ex);
		}
		
	}

}

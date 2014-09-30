package Armadillo.Analytics.Base;

import Armadillo.Core.Logger;

public class EvaluateFunctionDel {
	public double evaluate(double dblValue){
		try {
			throw new Exception();
		} catch (Exception e) {
			Logger.log(e);
		}
		return Double.NaN;
	}
}

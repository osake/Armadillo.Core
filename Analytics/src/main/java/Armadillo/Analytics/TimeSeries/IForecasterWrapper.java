package Armadillo.Analytics.TimeSeries;

import java.util.List;

public interface IForecasterWrapper 
{

	double Forecast(double[] ds);

	List<Double> GetErrors();

	int length();

}

package Web.Chart;

import java.io.Serializable;
import java.util.Map;

import javax.faces.component.UIComponent;

import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
//import org.primefaces.component.chart.UIChart;
//import org.primefaces.component.chart.bubble.BubbleChart;
//import org.primefaces.component.chart.line.LineChart;
import org.primefaces.model.chart.BubbleChartModel;
import org.primefaces.model.chart.BubbleChartSeries;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.LineChartModel;
//import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.ChartSeries;

import Armadillo.Core.Logger;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiTableItem;
import Web.Base.WebHelper;
import Web.Dashboard.EnumChartType;
import Web.Dashboard.DynamicGuiInstanceWrapper;

public class ChartHelper {
	public static void reloadChart(AUiTableItem currUiItem) {
		try {
			AUiChartItem uiChartItem = ((AUiChartItem) currUiItem);
			uiChartItem.setChartSeries(null);
			loadChart(currUiItem);
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	private static UIComponent loadLineChart(String strTabName,
			DynamicGuiInstanceWrapper tabInstanceWrapper) {
		try {
			if (tabInstanceWrapper == null) {
				return null;
			}
			AUiItem uiItem = tabInstanceWrapper.getUiItem();
			if (uiItem == null) {
				return null;
			}
			AUiChartItem uiChartItem = (AUiChartItem) tabInstanceWrapper
					.getUiItem();
			LineChartModel cartesianChartModel = new LineChartModel();
			Chart lineChart = new Chart();
			lineChart.setType("line");
			lineChart.setModel(cartesianChartModel);
			uiChartItem.setChartModel(cartesianChartModel);
			Axis xAxis = cartesianChartModel.getAxis(AxisType.X);
			xAxis.setTickAngle(-50);
			
			formatChart(strTabName, uiChartItem, lineChart);
			cartesianChartModel.setZoom(true);
			// lineChart.setZoom(true);
			// LineChartModel cartesianChartModel = new
			// LineChartModel();
			// lineChart.setValue(cartesianChartModel);

			return lineChart;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	private static void formatChart(String strTabName,
			AUiChartItem uiChartItem, Chart lineChart) {
		try 
		{
			ChartModel chartModel = (ChartModel) uiChartItem
					.getChartModel();
			
			chartModel.setLegendPosition("e");
			chartModel.setShadow(true);
			lineChart.setId(WebHelper.getChartId(strTabName));
			uiChartItem.setChart(lineChart);
			lineChart.setRendered(true);

			lineChart.setValueExpression("dynamic", WebHelper
					.createValueExpression(((Object) true).toString(),
							Boolean.class));

			String strFunc = "function ext() {"
					+ "    this.cfg.axes.yaxis.tickOptions.formatString = '%.6s';"
					+ "    this.cfg.axes.yaxis.tickOptions.fontSize = '15pt';"
					+ "}";

			chartModel.setExtender(strFunc);
			// lineChart.setExtender(strFunc);

			lineChart.setValueExpression("style", WebHelper
					.createValueExpression("height: 800px", String.class));

			lineChart.setValueExpression("legendPosition",
					WebHelper.createValueExpression("e", String.class));
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public static void loadChart(AUiItem uiItem) {
		try 
		{
			AUiChartItem uiChartItem = ((AUiChartItem) uiItem);
			Map<String, Serializable> chartSeriesMap = uiChartItem
					.getChartSeries();
			if (uiChartItem.getChartType() == EnumChartType.BubbleChart) 
			{
				BubbleChartModel chartModel = (BubbleChartModel) uiChartItem
						.getChartModel();
				chartModel.clear();
				for (Serializable chartSeries : chartSeriesMap.values()) 
				{
					chartModel.getData().add((BubbleChartSeries) chartSeries);
				}
			} 
			else 
			{
				LineChartModel chartModel = (LineChartModel) uiChartItem
						.getChartModel();
				chartModel.clear();
				for (Serializable chartSeries : chartSeriesMap.values()) 
				{
					ChartSeries lineChartSeries = (ChartSeries) chartSeries;
					chartModel.addSeries(lineChartSeries);
				}
			}
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public static UIComponent loadChartUiComponent(String strTabName,
			AUiItem uiItem, DynamicGuiInstanceWrapper tabInstance) {
		try {
			UIComponent uiComponent;
			EnumChartType chartType = ((AUiChartItem) uiItem).getChartType();
			if (chartType == EnumChartType.BubbleChart) {

				uiComponent = loadBubbleChart(strTabName, tabInstance);
			} else {
				uiComponent = loadLineChart(strTabName, tabInstance);
			}
			return uiComponent;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	private static UIComponent loadBubbleChart(String strTabName,
			DynamicGuiInstanceWrapper tabInstanceWrapper) {
		try {
			if (tabInstanceWrapper == null) {
				return null;
			}
			AUiItem uiItem = tabInstanceWrapper.getUiItem();
			if (uiItem == null) {
				return null;
			}
			AUiChartItem uiChartItem = (AUiChartItem) tabInstanceWrapper
					.getUiItem();
			Chart chart = new Chart();
			chart.setType("bubble");
			formatChart(strTabName, uiChartItem, chart);

			BubbleChartModel cartesianChartModel = new BubbleChartModel();
			cartesianChartModel.setZoom(true);
			// chart.setZoom(true);
			uiChartItem.setChartModel(cartesianChartModel);
			chart.setModel(cartesianChartModel);
			// chart.setValue(cartesianChartModel);

			return chart;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}
}

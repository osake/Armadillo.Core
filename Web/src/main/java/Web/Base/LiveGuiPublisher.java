package Web.Base;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.Modifier;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Console;
import Armadillo.Core.Foo;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.ParserHelper;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Reflection.ClassHelper;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.AGuiCallback;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.TableRow;
import Armadillo.Core.UI.UiHelper;
import Utils.Gui.AUiItem;
import Utils.Gui.AUiParam;
import Utils.Gui.AUiTableItem;
import Web.Catalogue.AUiCatalogueTableItem;
import Web.Chart.AUiChartItem;
import Web.Dashboard.EnumChartType;

public class LiveGuiPublisher 
{
	private ConcurrentHashMap<String, AUiItem> m_guiItems;
	private static LiveGuiPublisher m_ownInstance;
	private static EfficientProducerConsumerQueue<PublishJob> m_tablePublishQueue;

	private static void loadCallback() 
	{
		try 
		{
			Armadillo.Core.UI.LiveGuiPublisher.addCallback(new AGuiCallback() 
			{
				@Override
				public void OnStr(String str) {
					PublishRow(str);
					str = null;
				}

				@Override
				public boolean PublishLineChartRow(
						String arg0, 
						String arg1,
						String arg2, 
						String arg3, 
						String arg4, 
						double arg5) 
				{
					try
					{
						PublishLineChartRow(
								arg0, 
								arg1,
								arg2, 
								arg3, 
								arg4, 
								arg5);
						
					  return true;
					}
					catch(Exception ex)
					{
						Logger.log(ex);
					}
					return false;
				}

				@Override
				public boolean PublishTableRow(
						String arg0, 
						String arg1,
						String arg2, 
						String arg3, 
						Object arg4) 
				{
					try
					{
						publishTableRow(
								arg0, 
								arg1,
								arg2, 
								arg3, 
								arg4);
								
						return true;
					}
					catch(Exception ex)
					{
						Logger.log(ex);
					}
					return false;
				}
			});
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	private LiveGuiPublisher() {
		m_guiItems = new ConcurrentHashMap<String, AUiItem>();
	}

	public static void doTest() {
		ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>() {

			private RngWrapper m_rng = new RngWrapper();

			@Override
			public void runTask(ObjectWrapper item) {
				while (true) {
					try {
						ArrayList<Foo> fooList = Foo.getFooList(1, false);
						publishTableRow("test1", "test1", m_rng.NextInt(1, 10)
								+ "_test", m_rng.NextInt(1, 10) + "_testKey",
								fooList.get(0));
						Thread.sleep(500);
					} catch (Exception ex) {
						Logger.log(ex);
					}
				}
			}
		};
		worker.work();
	}

	private static void loadUiItemsViaReflection() {
		try {
			//
			// find all reports in the framework
			//
			Set<Class<? extends AUiItem>> reports = ClassHelper
					.getSubTypes(AUiItem.class);

			for (Class<? extends AUiItem> reportClass : reports) 
			{
				if (reportClass.isInterface()
						|| Modifier.isAbstract(reportClass.getModifiers())
						|| reportClass.getName().contains("$")) 
				{
					continue;
				}
				if (!AUiParam.class.isAssignableFrom(reportClass)
						&& !AUiCatalogueTableItem.class
								.isAssignableFrom(reportClass)) {
					AUiItem report = (AUiItem) ReflectionCache.getReflector(
							reportClass).createInstance();
					loadUiItem(report);
				}
			}
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public static void ClearSeries(String str1, String str2, String str3) {
		try {
			if (StringHelper.IsNullOrEmpty(str1)
					|| StringHelper.IsNullOrEmpty(str2)
					|| StringHelper.IsNullOrEmpty(str3)) {
				return;
			}

			AUiChartItem uiChartItem = getChartUiItem(str1, str2, str3);
			uiChartItem.getChartSeries().clear();
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public static boolean publishTableRow(String str1, String str2,
			String str3, String strObjectKey, Object obj) {
		try {
			if (StringHelper.IsNullOrEmpty(str1)
					|| StringHelper.IsNullOrEmpty(str2)
					|| StringHelper.IsNullOrEmpty(str3)
					|| StringHelper.IsNullOrEmpty(strObjectKey) || obj == null) {
				return false;
			}

			AUiTableItem uiTableItem = getUiItem(str1, str2, str3, obj);

			uiTableItem.populateTableRow(strObjectKey, obj);
			return true;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return false;
	}

	public static void PublishRow(String str) {
		String[] toks;
		try {
			if (StringHelper.IsNullOrEmpty(str)) {
				return;
			}
			toks = str.split("\\|");

			if (toks.length < 5) {
				return;
			}

			if (toks[toks.length - 1].equals("IsChart")) {
				publishChart(toks);
			} else {
				publishTableRow(toks);
			}
		} catch (Exception ex) {
			Logger.log(ex);
		} finally {
			toks = null;
		}
	}

	private static void publishChart(String[] toks) {
		try {
			String str1 = toks[0];
			String str2 = toks[1];
			String str3 = toks[2];
			String strSeriesName = toks[3];
			String strTsRow = toks[4];
			String[] tsToks = strTsRow.split(";");
			if (tsToks.length < 2) {
				return;
			}
			double[] result = new double[1];
			if (ParserHelper.tryParseDoubleValue(tsToks[1], result)
					&& result != null) {
				PublishLineChartRow(str1, str2, str3, strSeriesName, tsToks[0],
						result[0]);
			} else {
				Console.writeLine("Could not parse to double token "
						+ tsToks[1]);
			}
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	private static void publishTableRow(String[] toks) {
		try {
			if (toks.length < 6) {
				return;
			}
			String str1 = toks[0];
			String str2 = toks[1];
			String str3 = toks[2];
			String strObjectKey = toks[3];
			String strCol = toks[4];
			String[] cols = strCol.split(";");
			String strVals = toks[5];
			String[] vals = strVals.split(";");

			PublishTableRow(str1, str2, str3, strObjectKey, cols, vals);
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public static boolean PublishTableRow(
			String str1, 
			String str2,
			String str3, 
			String strObjectKey,
			Object obj) 
	{
		try
		{
			if(obj == null)
			{
				return false;
			}
			Reflector reflection = ReflectionCache.getReflector(obj.getClass());
			String[] colNames = reflection.getColNames();
			Object[] colValues = reflection.getPropValues(obj);
			String[] colValuesStr = new String[colNames.length];
			for (int i = 0; i < colValuesStr.length; i++) {
				colValuesStr[i] = colValues[i] == null ? "" : colValues[i].toString();
			}
			PublishTableRow(
					str1, 
					str2,
					str3, 
					strObjectKey, 
					colNames, 
					colValuesStr);			
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}

	public static boolean PublishTableRow(
			String str1, 
			String str2,
			String str3, 
			String strObjectKey, 
			String[] cols, 
			String[] vals) 
	{
		try 
		{
			PublishJob publishJob = new PublishJob();
			publishJob.str1 = str1;
			publishJob.str2 = str2;
			publishJob.str3 = str3;
			publishJob.strObjectKey = strObjectKey;
			publishJob.cols = cols;
			publishJob.vals = vals;

			String strKey = str1 + "_" + str2 + "_" + str3 + "_" + strObjectKey;
			m_tablePublishQueue.add(strKey, publishJob);
			strKey = null;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return true;
	}

	public static boolean PublishTableRow0(
			String str1, 
			String str2,
			String str3, 
			String strObjectKey, 
			String[] cols, 
			String[] vals) 
	{
		try 
		{
			if (StringHelper.IsNullOrEmpty(str1)
					|| StringHelper.IsNullOrEmpty(str2)
					|| StringHelper.IsNullOrEmpty(str3)
					|| StringHelper.IsNullOrEmpty(strObjectKey) || vals == null) {
				return false;
			}

			AUiTableItem uiTableItem = getTableUiItem(str1, str2, str3, cols);
			if (uiTableItem.getColCount() < cols.length) 
			{
				ArrayList<ColumnModel> columns = WebHelper.getColumnItemsList(
						cols, null);
				uiTableItem.setColumns(columns);
			}

			uiTableItem.populateTableRow(strObjectKey, vals, cols);
			return true;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return false;
	}

	public static boolean PublishLineChartRow(
			String str1, 
			String str2,
			String str3, 
			String strSeries, 
			String strX, 
			double dblY) 
	{
		try 
		{
			if (StringHelper.IsNullOrEmpty(str1)
					|| StringHelper.IsNullOrEmpty(str2)
					|| StringHelper.IsNullOrEmpty(str3)
					|| StringHelper.IsNullOrEmpty(strX)
					|| StringHelper.IsNullOrEmpty(strSeries)
					|| Double.isNaN(dblY) || Double.isInfinite(dblY)) {
				return false;
			}

			AUiChartItem uiChartItem = getChartUiItem(str1, str2, str3);

			uiChartItem.populateLineChartRow(strSeries, strX, dblY);
			return true;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return false;
	}

	public static boolean PublishBubbleChartRow(String str1, String str2,
			String str3, String strSeries, int intX, int intY, int intRadious,
			String strLabel) {
		try {
			if (StringHelper.IsNullOrEmpty(str1)
					|| StringHelper.IsNullOrEmpty(str2)
					|| StringHelper.IsNullOrEmpty(str3)
					|| StringHelper.IsNullOrEmpty(strSeries)) {
				return false;
			}

			AUiChartItem uiChartItem = getChartUiItem(str1, str2, str3);
			uiChartItem.setChartType(EnumChartType.BubbleChart);

			uiChartItem.populateBubbleChartRow(strSeries, intX, intY,
					intRadious, strLabel);
			return true;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return false;
	}

	private static AUiTableItem getUiItem(String str1, String str2,
			String str3, Object obj) {
		try {
			String strTreeKey = UiHelper.getNodeKey(str1, str2, str3);

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				synchronized (LockHelper.GetLockObject(strTreeKey
						+ "_publisherLock")) {
					if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
						String[] treeLabels = new String[] { str1, str2, str3 };
						Reflector reflector = ReflectionCache.getReflector(obj
								.getClass());
						String[] colNames = reflector.getColNames();
						AUiTableItem uiTableItem = generateCustomUiTableItem(
								strTreeKey, treeLabels, colNames);
						m_ownInstance.m_guiItems.put(strTreeKey, uiTableItem);
					}
				}
			}

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				return null;
			}

			AUiTableItem uiTableItem = (AUiTableItem) m_ownInstance.m_guiItems
					.get(strTreeKey);
			return uiTableItem;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	public static AUiTableItem getUiItem(String str1, String str2, String str3) {
		try {
			String strTreeKey = UiHelper.getNodeKey(str1, str2, str3);

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				return null;
			}

			AUiTableItem uiTableItem = (AUiTableItem) m_ownInstance.m_guiItems
					.get(strTreeKey);
			return uiTableItem;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	private static AUiTableItem getTableUiItem(String str1, String str2,
			String str3, String[] colNames) {
		try {
			String strTreeKey = UiHelper.getNodeKey(str1, str2, str3);

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				synchronized (LockHelper.GetLockObject(strTreeKey
						+ "_publisherLock")) {
					if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
						String[] treeLabels = new String[] { str1, str2, str3 };
						AUiTableItem uiTableItem = generateCustomUiTableItem(
								strTreeKey, treeLabels, colNames);
						m_ownInstance.m_guiItems.put(strTreeKey, uiTableItem);
					}
				}
			}

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				return null;
			}

			AUiTableItem uiTableItem = (AUiTableItem) m_ownInstance.m_guiItems
					.get(strTreeKey);
			return uiTableItem;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	private static AUiChartItem getChartUiItem(String str1, String str2,
			String str3) {
		try {
			String strTreeKey = UiHelper.getNodeKey(str1, str2, str3);

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				synchronized (LockHelper.GetLockObject(strTreeKey
						+ "_publisherLock")) {
					if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
						String[] treeLabels = new String[] { str1, str2, str3 };
						AUiTableItem uiTableItem = generateCustomUiChartItem(
								strTreeKey, treeLabels);
						m_ownInstance.m_guiItems.put(strTreeKey, uiTableItem);
					}
				}
			}

			if (!m_ownInstance.m_guiItems.containsKey(strTreeKey)) {
				return null;
			}

			AUiChartItem uiChartItem = (AUiChartItem) m_ownInstance.m_guiItems
					.get(strTreeKey);
			return uiChartItem;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	private static AUiTableItem generateCustomUiChartItem(
			final String strTreeKey, final String[] treeLabels) {
		try {
			AUiChartItem chartItem = new AUiChartItem() {

				@Override
				public String[] getReportTreeLabels() {
					return treeLabels;
				}

				@Override
				public String getReportTitle() {
					return strTreeKey;
				}

				@Override
				protected Map<String, Serializable> generateChartSeries() {
					return new ConcurrentHashMap<String, Serializable>();
				}

				@Override
				public Type[] getFieldTypes() {
					return null;
				}
			};
			return chartItem;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	private static AUiTableItem generateCustomUiTableItem(
			final String strTreeKey, final String[] treeLabels,
			final String[] colNames) {
		AUiTableItem uiTableItem = new AUiTableItem() {

			@Override
			public String[] getReportTreeLabels() {
				return treeLabels;
			}

			@Override
			public String getReportTitle() {
				return strTreeKey;
			}

			@Override
			protected Class<?> getParamsClass() {
				return null;
			}

			@Override
			protected String getObjKey(TableRow obj) {
				return obj.getKey();
			}

			@Override
			public String[] getFieldNames() {
				return colNames;
			}

			@Override
			public List<TableRow> generateTableRows() {
				return new ArrayList<TableRow>();
			}

			@Override
			public Type[] getFieldTypes() {
				return null;
			}
		};
		return uiTableItem;
	}

	public static void loadUiItem(AUiItem uiItem) {
		try {
			String strKey = uiItem.getTreeKey();
			if (!m_ownInstance.m_guiItems.containsKey(strKey)) {
				m_ownInstance.m_guiItems.put(strKey, uiItem);
			}
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public ConcurrentHashMap<String, AUiItem> getGuiItems() {
		return m_guiItems;
	}

	public static LiveGuiPublisher getOwnInstance() {
		return m_ownInstance;
	}

	public static void initialize() 
	{
		try 
		{
			m_ownInstance = new LiveGuiPublisher();
			m_tablePublishQueue = new EfficientProducerConsumerQueue<PublishJob>(
					1, 10000) 
			{
				@Override
				public void runTask(PublishJob item) 
				{
					PublishTableRow0(
							item.str1, 
							item.str2, 
							item.str3,
							item.strObjectKey, 
							item.cols, 
							item.vals);

					item.dispose();
				}
			};
			loadUiItemsViaReflection();
			loadCallback();
			// doTest();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
}
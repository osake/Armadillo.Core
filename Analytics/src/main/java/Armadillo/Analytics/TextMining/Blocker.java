package Armadillo.Analytics.TextMining;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Armadillo.Core.PrintToScreen;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.TokenWrapper;

public class Blocker 
{
    private int m_intBlockingColumnCount;
    private String[][] m_dataArrayCoded;
    private List<Map<String, List<Integer>>> m_htCodedList;
    private Integer[] m_intBlockingColumnsArray;

    public Blocker(DataWrapper strDataArray)
    {
        // get the database with blocking keys
        GetCodedDataset(strDataArray);
        // select the most appropriate columns to block
        GetBlockingColumns();
        // count the number of columns
        m_intBlockingColumnCount = m_intBlockingColumnsArray.length;
    }

    public void InitializeCodedListTable()
    {
        m_htCodedList = new ArrayList<Map<String, List<Integer>>>();
        for (int i = 0; i < m_intBlockingColumnCount; i++)
        {
            m_htCodedList.add(new Hashtable<String, List<Integer>>());
        }

        int N = m_dataArrayCoded.length;
        List<Integer> indexList;
        for (int i = 0; i < N; i++)
        {
            for (int column = 0; column < m_intBlockingColumnCount; column++)
            {
                int indexColumn = m_intBlockingColumnsArray[column];
                String currentToken = m_dataArrayCoded[i][indexColumn];
                if (!m_htCodedList.get(column).containsKey(currentToken))
                {
                    indexList = new ArrayList<Integer>();
                    indexList.add(i);
                    m_htCodedList.get(column).put(currentToken, indexList);
                }
                else
                {
                    indexList = m_htCodedList.get(column).get(currentToken);
                    indexList.add(i);
                }
            }
        }
    }

    public Integer[] GetBlockIndexes(int intRowIndex)
    {
        String[] tokenArray = m_dataArrayCoded[intRowIndex];
        return GetBlockIndexes(tokenArray, intRowIndex);
    }

    public Integer[] GetBlockIndexes(String[] strTokenArray, int intRowIndex)
    {
        List<Integer> indexList;
        List<Integer> resultList = new ArrayList<Integer>();
        Map<Integer, Object> tmpTable = new Hashtable<Integer, Object>();
        for (int column = 0; column < m_intBlockingColumnCount; column++)
        {
            int index = m_intBlockingColumnsArray[column];
            String currentToken = strTokenArray[index];
            if (m_htCodedList.get(column).containsKey(currentToken))
            {
                indexList = m_htCodedList.get(column).get(currentToken);
                // iterate the list
                for (int intCurrentIndex : indexList)
                {
                    if (!tmpTable.containsKey(intCurrentIndex) && intCurrentIndex > intRowIndex)
                    {
                        tmpTable.put(intCurrentIndex, null);
                        resultList.add(intCurrentIndex);
                    }
                }
            }
        }
        return resultList.toArray(new Integer[resultList.size()]);
    }

    private void GetCodedDataset(DataWrapper strDataArray)
    {
        // create a new coded dataset
        int N = strDataArray.length();
        int columnCount = strDataArray.getDataArray()[0].Columns.length;
        m_dataArrayCoded = new String[N][];
        for (int i = 0; i < N; i++)
        {
            m_dataArrayCoded[i] = GetCodedRowArray(
                strDataArray.getDataArray()[i].Columns, 
                columnCount);
        }
    }

    public static String[] GetCodedRowArray(
        TokenWrapper[][] strRow,
        int intColumnCount)
    {
        String[] codedRow = new String[intColumnCount];
        for (int column = 0; column < intColumnCount; column++)
        {
            if (strRow[column].length > 0 && !StringHelper.IsNullOrEmpty(strRow[column][0].Token))
            {
                String soundexCode =
                    Soundex.CalcSoundEx(strRow[column][0].Token);
                codedRow[column] = soundexCode;
            }
            else
            {
                codedRow[column] = "";
            }
        }
        return codedRow;
    }

    public boolean CheckCodeMatch(int intTIdex, int intUIndex)
    {
        return CheckCodeMatch(m_dataArrayCoded[intTIdex],
                              m_dataArrayCoded[intUIndex]);
    }

    public boolean CheckCodeMatch(String[] tArray, int uIndex)
    {
        return CheckCodeMatch(tArray,
                              m_dataArrayCoded[uIndex]);
    }

    public boolean CheckCodeMatch(String[] strTArray, String[] strUArray)
    {
        for (int column = 0; column < m_intBlockingColumnCount; column++)
        {
            int index = m_intBlockingColumnsArray[column];
            if (strTArray[index].equals(strUArray[index]))
            {
                return true;
            }
        }
        return false;
    }

    private void GetBlockingColumns()
    {
        int N = m_dataArrayCoded.length;
        int columnCount = m_dataArrayCoded[0].length;
        // for a small dataset return the same columns
        if (N <= 500)
        {
            m_intBlockingColumnsArray = new Integer[columnCount];
            for (int column = 0; column < columnCount; column++)
            {
                m_intBlockingColumnsArray[column] = column;
            }
            return;
        }

        List<Map<String, Object>> tokenTable = new ArrayList<Map<String, Object>>();
        for (int column = 0; column < columnCount; column++)
        {
            tokenTable.add(new Hashtable<String, Object>());
        }

        for (int i = 0; i < N; i++)
        {
            for (int column = 0; column < columnCount; column++)
            {
                String currentToken = m_dataArrayCoded[i][column];
                if (!tokenTable.get(column).containsKey(currentToken))
                {
                    tokenTable.get(column).put(currentToken, new Object());
                }
            }
        }
        List<Integer> fieldList = new ArrayList<Integer>();
        for (int column = 0; column < columnCount; column++)
        {
            PrintToScreen.WriteLine("column " + column +
                                    ", block count " + tokenTable.get(column).size());
            if (tokenTable.get(column).size() > 10)
            {
                fieldList.add(column);
            }
        }
        // if there is no field, then select the most adecuate column
        if (fieldList.size() == 0)
        {
            int maxCount = -1, maxCoumnIndex = -1;
            for (int column = 0; column < columnCount; column++)
            {
                int currentCount = tokenTable.get(column).size();
                if (currentCount > maxCount)
                {
                    maxCoumnIndex = column;
                }
            }
            fieldList.add(maxCoumnIndex);
        }
        m_intBlockingColumnsArray = fieldList.toArray(new Integer[fieldList.size()]);
    }

}

package Armadillo.Analytics.TextMining;

import Armadillo.Core.Text.TokenWrapper;

public class RowWrapper
{
    public TokenWrapper[][] Columns;
    public Object Handle;

    public String toString()
    {
    	StringBuilder parentSb = new StringBuilder();
        //
        // load first row
        //
        String strParentCol =
            GetColumnString(Columns[0]);

        parentSb.append(strParentCol);

        for (int i = 1; i < Columns.length; i++)
        {
            strParentCol =
                GetColumnString(Columns[i]);
            parentSb.append("_" + strParentCol);
        }
        String strRowDesc = parentSb.toString();
        return strRowDesc;
    }

    public static String GetColumnString(TokenWrapper[] tokens)
    {
    	StringBuilder sb = new StringBuilder();
        sb.append(tokens[0]);

        for (int i = 1; i < tokens.length; i++)
        {
            sb.append(" " + tokens[i]);
        }
        return sb.toString();
    }
}

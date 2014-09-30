package Armadillo.Analytics.Mathematics;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Core.HCException;

public class MathHelper
{
	public static boolean isAValidNumber(double dblNum)
	{
		return !Double.isNaN(dblNum) && !Double.isInfinite(dblNum);
	}
	
    /// <summary>
    /// Returns true if search value is between values 1 and 2
    /// </summary>
    /// <param name="dblValue1"></param>
    /// <param name="dblValue2"></param>
    /// <param name="dblSearchValue"></param>
    /// <returns></returns>
    public static boolean ValueIsBetween(double dblValue1, double dblValue2, double dblSearchValue)
    {
        if (dblValue2 < dblValue1)
        {
            double dblTmp = dblValue1;
            dblValue1 = dblValue2;
            dblValue2 = dblTmp;
        }
        return (dblSearchValue >= dblValue1 && dblSearchValue <= dblValue2);
    }


    public static double rawCopySign(double magnitude, double sign)
    {
        return magnitude*Math.signum(sign);
    }

    public static String GetInequalitySymbol(
        InequalityType inequalityType)
    {
        if (inequalityType == InequalityType.EQUALS)
        {
            return "=";
        }
        if (inequalityType == InequalityType.GREATER_OR_EQUAL)
        {
            return ">=";
        }
        if (inequalityType == InequalityType.GREATER_THAN)
        {
            return ">";
        }
        if (inequalityType == InequalityType.LESS_OR_EQUAL)
        {
            return "<=";
        }
        if (inequalityType == InequalityType.LESS_THAN)
        {
            return "<";
        }
        if (inequalityType == InequalityType.LIKE)
        {
            return "LIKE";
        }
        throw new HCException("Inequality not supported");
    }

    public static boolean CheckInequality(
        InequalityType inequalityType,
        double dblValue1,
        double dblValue2)
    {
        dblValue1 = Precision.round(dblValue1,
                               MathConstants.ROUND_DECIMALS);
        dblValue2 = Precision.round(dblValue2,
                               MathConstants.ROUND_DECIMALS);

        if (inequalityType == InequalityType.EQUALS)
        {
            return Math.abs(dblValue1 - dblValue2) <=
                   MathConstants.ROUND_ERROR;
        }
        if (inequalityType == InequalityType.GREATER_OR_EQUAL)
        {
            return dblValue1 >= dblValue2; // -Base.Constants.ROUND_ERROR;
        }
        if (inequalityType == InequalityType.GREATER_THAN)
        {
            return dblValue1 > dblValue2; // -Base.Constants.ROUND_ERROR;
        }
        if (inequalityType == InequalityType.LESS_OR_EQUAL)
        {
            return dblValue1 <= dblValue2; // +Base.Constants.ROUND_ERROR;
        }
        if (inequalityType == InequalityType.LESS_THAN)
        {
            return dblValue1 < dblValue2; // +Base.Constants.ROUND_ERROR;
        }
        if (inequalityType == InequalityType.LIKE)
        {
            return Math.abs(dblValue1 - dblValue2) <=
                   MathConstants.ROUND_ERROR;
        }
        throw new HCException("Inequality not supported");
    }

    public static double Sqr(double dblX)
    {
        return dblX*dblX;
    }

    /** sqrt(a^2 + b^2) without under/overflow. **/

    public static double hypot(double a, double b)
    {
        double r;
        if (Math.abs(a) > Math.abs(b))
        {
            r = b/a;
            r = Math.abs(a)*Math.sqrt(1 + r*r);
        }
        else if (b != 0)
        {
            r = a/b;
            r = Math.abs(b)*Math.sqrt(1 + r*r);
        }
        else
        {
            r = 0.0;
        }
        return r;
    }

    public static double GetHyperCube(double[][] dblIntegrationLimits)
    {
        double dblVolume = 1.0;
        for (int i = 0; i < dblIntegrationLimits.length; i++)
        {
            dblVolume *= (dblIntegrationLimits[i][1] - dblIntegrationLimits[i][0]);
        }
        return dblVolume;
    }

    public static double Round(double dblValue)
    {
        if (Double.isInfinite(dblValue) ||
            -Double.MAX_VALUE < dblValue - 1)
        {
            return dblValue;
        }
        if (dblValue == 0)
        {
            return 0.0;
        }
        if (dblValue > 1.0)
        {
            return Precision.round(dblValue, 2);
        }
        else
        {
            int intDecimals = 0;
            double dblValue2 = dblValue;
            while (dblValue2 < 1.0)
            {
                dblValue2 *= 10.0;
                intDecimals++;
            }
            return Precision.round(dblValue, intDecimals);
        }
    }

    /// <summary>
    /// Linear interpolation. Return a value in Y for a given set of previous/next values
    /// </summary>
    /// <param name="dblTargetXValue"></param>
    /// <param name="dblPreviousX"></param>
    /// <param name="dblPreviousY"></param>
    /// <param name="dblNextX"></param>
    /// <param name="dblNextY"></param>
    /// <returns></returns>
    public static double DoLinearInterpolation(
        double dblTargetXValue,
        double dblPreviousX,
        double dblPreviousY,
        double dblNextX,
        double dblNextY)
    {
        //
        // interpolate values
        //
        double dblDeltaTargetX = dblTargetXValue - dblNextX;
        if (dblDeltaTargetX == 0)
        {
            return dblPreviousY;
        }
        double dblDeltaX = dblPreviousX - dblNextX;
        double dblDeltaY = dblPreviousY - dblNextY;
        double dblDeltaTargetY = dblDeltaTargetX*dblDeltaY/dblDeltaX;
        double dblTargetYValue = dblDeltaTargetY + dblNextY;

        if (dblTargetYValue > Math.max(dblNextY, dblPreviousY) ||
            dblTargetYValue < Math.min(dblNextY, dblPreviousY))
        {
            throw new HCException("Interpolaion error.");
        }
        return dblTargetYValue;
    }

    public static InequalityType ParseInequalitySymbol(String strConstraint)
    {
        if (strConstraint.contains("="))
        {
            return InequalityType.EQUALS;
        }
        if (strConstraint.contains(">="))
        {
            return InequalityType.GREATER_OR_EQUAL;
        }
        if (strConstraint.contains(">"))
        {
            return InequalityType.GREATER_THAN;
        }
        if(strConstraint.contains("<="))
        {
            return InequalityType.LESS_OR_EQUAL;
        }
        if (strConstraint.contains("<"))
        {
            return InequalityType.LESS_THAN;
        }
        if (strConstraint.contains("LIKE"))
        {
            return InequalityType.LIKE;
        }
        throw new HCException("Inequality not supported");
    }

	public static boolean isBetween(double dblWeight, double i, double j) 
	{
		return dblWeight >= i && dblWeight <= j;
	}
}

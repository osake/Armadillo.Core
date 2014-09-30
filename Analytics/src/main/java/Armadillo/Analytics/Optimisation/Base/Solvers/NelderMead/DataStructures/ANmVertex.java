package Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

/// <summary>
///   Vertex representation for Nelder-Mead algorithm
/// </summary>
public abstract class ANmVertex
{
    protected double[] m_dblCoordinatesArr;

    protected HeuristicProblem m_heuristicProblem;

    /// <summary>
    ///   Coordenates
    /// </summary>
    protected Individual m_individual;

    public Individual Individual_()
    {
        return m_individual;
    }


    /// <summary>
    ///   Vertex value
    /// </summary>
    public double Value;

    public ANmVertex(
        Individual individual,
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_dblCoordinatesArr = GetChromosomeCopy(individual);
        m_individual = individual;
    }

    /// <summary>
    ///   Combine a vector based on a weight
    /// </summary>
    /// <param name = "dblOwnt">
    ///   Weight
    /// </param>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <returns>
    ///   Combined vertex
    /// </returns>
    public ANmVertex Combine(double dblOwnt, ANmVertex vertex)
    {
        int i;
        double othert = 1.0 - dblOwnt;

        ANmVertex nv = CreateNmVertex();
        for (i = 0; i < m_dblCoordinatesArr.length; i++)
        {
            double dblChromosomeValye = GetVertexValue(i)*dblOwnt +
                                     vertex.GetVertexValue(i)*othert;
            nv.SetVertexValue(i, dblChromosomeValye);
        }
        return nv;
    }

    /// <summary>
    ///   Substract vertex
    /// </summary>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <returns>
    ///   Vertex
    /// </returns>
    public ANmVertex Sub(ANmVertex vertex)
    {
        ANmVertex nv = CreateNmVertex();
        for (int i = 0; i < m_dblCoordinatesArr.length; i++)
        {
            double dblValue = GetVertexValue(i) - vertex.GetVertexValue(i);
            nv.SetVertexValue(i, dblValue);
        }
        return nv;
    }


    /// <summary>
    ///   Add vertex
    /// </summary>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <returns>
    ///   Vertex
    /// </returns>
    public ANmVertex Add(ANmVertex vertex)
    {
    	ANmVertex nv = CreateNmVertex();
        for (int i = 0; i < m_dblCoordinatesArr.length; i++)
        {
            double dblValue = GetVertexValue(i) + vertex.GetVertexValue(i);

            nv.SetVertexValue(i, dblValue);
        }
        return nv;
    }

    /// <summary>
    ///   Distrance to vertex
    /// </summary>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <returns>
    ///   Distance to vertex
    /// </returns>
    public double DistanceTo(ANmVertex vertex)
    {
        double dblSum = 0.0;
        double dblDifference;

        for (int i = 0; i < m_dblCoordinatesArr.length; i++)
        {
            dblDifference = GetVertexValue(i) - vertex.GetVertexValue(i);
            dblSum += dblDifference*dblDifference;
        }
        return Math.sqrt(dblSum);
    }

    /// <summary>
    ///   String representation of current vertex
    /// </summary>
    /// <returns>
    ///   String representation
    /// </returns>
    @Override
    public String toString()
    {
        String txt = "(";
        for (int i = 0; i < m_dblCoordinatesArr.length; i++)
        {
            if (i > 0)
            {
                txt += ",";
            }
            txt += " " + Precision.round(GetVertexValue(i), 4);
        }
        txt += " ) = " + Precision.round(Value, 4);
        return txt;
    }


    public void Dispose()
    {
        m_dblCoordinatesArr = null;
        m_heuristicProblem = null;
        m_individual = null;
    }

    /// <summary>
    ///   Get diameter
    /// </summary>
    /// <param name = "vertexArray">
    ///   Vertex array
    /// </param>
    /// <returns>
    ///   Diameter value
    /// </returns>
    public double GetDiameter(ANmVertex[] vertexArray)
    {
        double dblDiameter;
        double dblDmax = 0.0;

        int m = vertexArray.length;
        if (m <= 1)
        {
            return 0.0;
        }

        for (int i = 0; i < m; i++)
        {
            for (int j = i + 1; j < m; j++)
            {
                dblDiameter = vertexArray[i].DistanceTo(vertexArray[j]);
                if (dblDmax < dblDiameter)
                {
                    dblDmax = dblDiameter;
                }
            }
        }
        return dblDmax;
    }

    public double GetVertexValue(
        int intIndex)
    {
        return m_dblCoordinatesArr[intIndex];
    }

    public void SetVertexValue(
        int intIndex,
        double dblValue)
    {
        m_dblCoordinatesArr[intIndex] = dblValue;
        if (dblValue >= 0 && dblValue <= 1)
        {
            SetChromosomeValue(intIndex, dblValue);
        }
        else if (dblValue < 0)
        {
            SetChromosomeValue(intIndex, 0);
        }
        else if (dblValue > 1)
        {
            SetChromosomeValue(intIndex, 1);
        }
    }

    protected abstract ANmVertex CreateNmVertex();
    protected abstract void SetChromosomeValue(int intIndex, double dblValue);
    protected abstract double[] GetChromosomeCopy(Individual individual);

}

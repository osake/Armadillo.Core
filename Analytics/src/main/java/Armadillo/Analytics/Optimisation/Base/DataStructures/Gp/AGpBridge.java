package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

/// <summary>
///   Template which serves to define a regression model.
///   It is used as the objetive function of the GP problem
/// </summary>
public abstract class AGpBridge 
{
    protected GpOperatorsContainer m_gpOperatorsContainer;
    protected int m_intNumbTestCases;

    public GpOperatorsContainer GpOperatorsContainer()
    {
        return m_gpOperatorsContainer;
    }
    
    public void setGpOperatorsContainer(GpOperatorsContainer gpOperatorsContainer)
    {
    	m_gpOperatorsContainer = gpOperatorsContainer;
    }

    public AGpBridge(
        GpOperatorsContainer gpOperatorsContainer)
    {
        m_gpOperatorsContainer = gpOperatorsContainer;
    }

    protected AGpVariable GetParameterValue(
        Object value)
    {
        //
        // clone parameter and assign value
        //
        AGpVariable gpVariable =
            m_gpOperatorsContainer.GpVariable.Clone();
        gpVariable.SetValue(value);
        return gpVariable;
    }

    public abstract double GetRegressionFit(
        Individual gpIndividual);

    public void Dispose()
    {
        if (m_gpOperatorsContainer != null)
        {
            m_gpOperatorsContainer.Dispose();
        }
    }
}

package org.fit.layout.patterns.eval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fit.layout.patterns.AreaUtils;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.Metric;
import org.fit.layout.patterns.model.TagConnection;

/**
 * This class represents the metric statistics for a given tag connection. It is a set of 
 * sets of area connections where each set corresponds to the area connections from
 * a single match. Over these sets, different statistics for the connections may be
 * computed.
 * 
 * @author burgetr
 */
public class ConnectionStats extends HashSet<Set<AreaConnection>>
{
    private static final long serialVersionUID = 1L;
    
    private TagConnection tcon;
    private Map<Metric, Float> metricMin;
    private Map<Metric, Float> metricMax;
    private Map<Metric, Float> metricAvg;
    private Map<Metric, Float> metricSigma;
    private Metric bestMetric;
    private float bestMetricValue;

    /**
     * Creates tag statistics for a given tag connection.
     * @param tcon The tag connection.
     */
    public ConnectionStats(TagConnection tcon)
    {
        this.tcon = tcon;
    }

    /**
     * Obtains the metrics that are evaluated for this tag connections.
     * @return A set of evaluated metrics.
     */
    public Set<Metric> getMetrics()
    {
        return tcon.getRelation().metrics();
    }
    
    /**
     * Obtains the minimal value of the given metric among the tag connections.
     * @param m the metric
     * @return the minimal value of the given metric
     */
    public float getMetricMin(Metric m)
    {
        if (metricMin == null)
            evaluateMetrics();
        Float ret = metricMin.get(m);
        return ret == null ? -1.0f : ret;
    }
    
    /**
     * Obtains the maximal value of the given metric among the tag connections.
     * @param m the metric
     * @return the maximal value of the given metric
     */
    public float getMetricMax(Metric m)
    {
        if (metricMax == null)
            evaluateMetrics();
        Float ret = metricMax.get(m);
        return ret == null ? -1.0f : ret;
    }
    
    /**
     * Obtains the average value of the given metric among the tag connections.
     * @param m the metric
     * @return the average value of the given metric
     */
    public float getMetricAvg(Metric m)
    {
        if (metricAvg == null)
            evaluateMetrics();
        Float ret = metricAvg.get(m);
        return ret == null ? -1.0f : ret;
    }
    
    /**
     * Obtains the sigma value of the given metric among the tag connections.
     * @param m the metric
     * @return the sigma value of the given metric
     */
    public float getMetricSigma(Metric m)
    {
        if (metricSigma == null)
            evaluateMetrics();
        Float ret = metricSigma.get(m);
        return ret == null ? -1.0f : ret;
    }

    /**
     * Finds the best metric. Currently, the one with the lowest sigma value is considered
     * to be the best one.
     * @return the best metric
     */
    public Metric getBestMetric()
    {
        if (bestMetric == null)
            evaluateMetrics();
        return bestMetric;
    }
    
    /**
     * Finds the sigma value of the best metric returned by {@link #getBestMetric()}.
     * @return the sigma value of the best metric
     */
    public float getBestMetricValue()
    {
        if (bestMetric == null)
            evaluateMetrics();
        return bestMetricValue;
    }
    
    //==================================================================================================
    
    /**
     * Computes the min, max, avg and sigma values for all the metrics.
     */
    private void evaluateMetrics()
    {
        metricMin = new HashMap<>();
        metricMax = new HashMap<>();
        metricAvg = new HashMap<>();
        metricSigma = new HashMap<>();
        
        if (size() > 0)
        {
            for (Metric m : getMetrics())
            {
                float[] values = computeMetricValues(m);
                //compute statistics
                float min, max, sum;
                min = max = sum = values[0];
                for (int i = 1; i < values.length; i++)
                {
                    if (values[i] < min)
                        min = values[i];
                    if (values[i] > max)
                        max = values[i];
                    sum += values[i];
                }
                float avg = sum / values.length;
                //sigma
                float difsum = 0;
                for (int i = 0; i < values.length; i++)
                {
                    float dif = values[i] - avg;
                    difsum += dif * dif;
                }
                float sigma = (float) Math.sqrt(difsum / values.length);
                //save the results
                metricMin.put(m, AreaUtils.statRound(min));
                metricMax.put(m, AreaUtils.statRound(max));
                metricAvg.put(m, AreaUtils.statRound(avg));
                metricSigma.put(m, AreaUtils.statRound(sigma));
            }
            //find the metric the best value
            bestMetricValue = 0;
            bestMetric = null;
            for (Metric m : getMetrics())
            {
                float val = metricSigma.get(m);
                if (bestMetric == null || val < bestMetricValue)
                {
                    bestMetric = m;
                    bestMetricValue = val;
                }
            }
        }
        
    }
    
    /**
     * Computes the values of the given metrics for all the area connections represented
     * by this connection statistics object.
     * @param m the metric to be computed
     * @return The array containing the values of the given metric for the indidividual
     * area connections. The length of the array corresponds to the size of the
     * connection stats set.
     */
    private float[] computeMetricValues(Metric m)
    {
        float[] ret = new float[this.size()];
        int i = 0;
        for (Set<AreaConnection> con : this)
            ret[i++] = m.compute(con);
        return ret;
    }
    
    //=============================================================================================
    
    private static class Values
    {
        public float min;
        public float max;
        public float sum;
    }
    
    
}
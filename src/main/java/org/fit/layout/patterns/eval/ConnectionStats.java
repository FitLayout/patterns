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
public class ConnectionStats
{
    private TagConnection tcon;
    private int count;
    private Map<Metric, Values> metricValues;

    /**
     * Creates tag statistics for a given tag connection.
     * @param tcon The tag connection.
     */
    public ConnectionStats(TagConnection tcon)
    {
        this.tcon = tcon;
        count = 0;
        metricValues = new HashMap<>();
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
        final Values vals = metricValues.get(m);
        return vals == null ? -1.0f : vals.min;
    }
    
    /**
     * Obtains the maximal value of the given metric among the tag connections.
     * @param m the metric
     * @return the maximal value of the given metric
     */
    public float getMetricMax(Metric m)
    {
        final Values vals = metricValues.get(m);
        return vals == null ? -1.0f : vals.max;
    }
    
    /**
     * Obtains the average value of the given metric among the tag connections.
     * @param m the metric
     * @return the average value of the given metric
     */
    public float getMetricAvg(Metric m)
    {
        final Values vals = metricValues.get(m);
        return vals == null ? -1.0f : (vals.sum / count);
    }
    
    /**
     * Obtains the sigma value of the given metric among the tag connections.
     * @param m the metric
     * @return the sigma value of the given metric
     */
    public float getMetricSigma(Metric m)
    {
        final Values vals = metricValues.get(m);
        return (float) (vals == null ? -1.0f : Math.sqrt(vals.sum_sq / count));
    }

    /**
     * Finds the best metric. Currently, the one with the lowest sigma value is considered
     * to be the best one.
     * @return the best metric
     */
    public Metric getBestMetric()
    {
        float min = Float.MAX_VALUE;
        Metric best = null;
        for (Metric m : getMetrics())
        {
            final float val = getMetricSigma(m);
            if (val >= 0 && val < min)
            {
                min = val;
                best = m;
            }
        }
        return best;
    }
    
    /**
     * Finds the sigma value of the best metric returned by {@link #getBestMetric()}.
     * @return the sigma value of the best metric
     */
    public float getBestMetricValue()
    {
        float min = Float.MAX_VALUE;
        for (Metric m : getMetrics())
        {
            final float val = getMetricSigma(m);
            if (val >= 0 && val < min)
            {
                min = val;
            }
        }
        return min;
    }
    
    //==================================================================================================

    public void add(Set<AreaConnection> cons)
    {
        count++;
        for (Metric m : getMetrics())
        {
            final float val = m.compute(cons);
            Values vals = metricValues.get(m);
            if (vals == null)
            {
                vals = new Values(val);
                metricValues.put(m, vals);
            }
            else
            {
                vals.add(val, count);
            }
        }        
    }
    
    //=============================================================================================
    
    private static class Values
    {
        public float min;
        public float max;
        public float sum;
        public float sum_sq;
        
        public Values(float val)
        {
            min = max = sum = val;
            sum_sq = 0;
        }
        
        public void add(float val, int newcount)
        {
            if (val < min)
                min = val;
            if (val > max)
                max = val;
            
            final float d1 = val - (sum / (newcount - 1));
            sum += val;
            final float d2 = val - (sum / newcount);
            sum_sq = sum_sq + d1 * d2;
        }
        
    }
    
}
/**
 * MatchResult.java
 *
 * Created on 9. 7. 2017, 22:14:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AreaUtils;
import org.fit.layout.patterns.DefaultRelationProbabilitySource;
import org.fit.layout.patterns.RelationAnalyzer;
import org.fit.layout.patterns.RelationProbabilitySource;

/**
 * A result of matching a single matcher configuration. The result holds the statistics
 * about the areas covered by the matches which allows to compare the different results.
 * 
 * @author burgetr
 */
public class MatchResult
{
    private RelationProbabilitySource probs;
    private Collection<Match> matches;
    private Set<Area> matchedAreas;
    private RelationAnalyzer pa;
    private Map<Area, List<AreaConnection>> connsM; //map of 1:M connections to their source areas
    private Map<Area, Float> avgM; //average weights of 1:M connections sharing a single source area
    private Map<TagConnection, ConnectionStats> conStats; //area connection stats for every tag connection
    private Map<Tag, StyleCounter<AreaStyle>> styleStats; //different style statistics for the individual tags
    private MatchResultScore score;
    
    public MatchResult(Collection<Match> matches, Set<Area> matchedAreas, RelationAnalyzer pa)
    {
        this.probs = new DefaultRelationProbabilitySource();
        this.matches = matches;
        this.matchedAreas = matchedAreas;
        this.pa = pa;
        score = new MatchResultScore();
    }
    
    public void setRelationProbabilitySource(RelationProbabilitySource src)
    {
        probs = src;
    }

    public Collection<Match> getMatches()
    {
        return matches;
    }

    public Set<Area> getMatchedAreas()
    {
        return matchedAreas;
    }
    
    public RelationAnalyzer getRelationAnalyzer()
    {
        return pa;
    }
    
    /**
     * Finds the minimal value of the weights of the matches conatined in this result.
     * @return The minimal weight of the matches or 0 for an empty match result
     */
    public float getMinConnectionWeight()
    {
        if (matches.size() > 0)
        {
            float min = 1.0f;
            for (Match match : matches)
                min = Math.min(getMatchAverage(match), min);
            return min;
        }
        else
            return 0;
    }
    
    /**
     * Finds the maximal value of the weights of the matches conatined in this result.
     * @return The maximal weight of the matches or 0 for an empty match result
     */
    public float getMaxConnectionWeight()
    {
        if (matches.size() > 0)
        {
            float max = -1.0f;
            for (Match match : matches)
                max = Math.max(getMatchAverage(match), max);
            return max;
        }
        else
            return 0;
    }
    
    /**
     * Computes the average value of the weights of the matches conatined in this result.
     * @return The average weight of the matches or 0 for an empty match result
     */
    public float getAverageConnectionWeight()
    {
        if (matches.size() > 0)
        {
            float sum = 0;
            for (Match match : matches)
                sum += getMatchAverage(match);
            return AreaUtils.statRound(sum / matches.size());
        }
        else
            return 0;
    }
    
    /**
     * Computes the standard deviation of the weights of the matches conatined in this result.
     * @return The standard deviation of the matches or 0 for an empty match result
     */
    public float getConnectionWeightSigma()
    {
        if (matches.size() > 0)
        {
            final float e = getAverageConnectionWeight();
            float sum = 0;
            for (Match match : matches)
            {
                float dif = getMatchAverage(match) - e;
                sum += dif * dif;
            }
            return AreaUtils.statRound((float) Math.sqrt(sum / matches.size()));
        }
        else
            return 0;
    }
    
    public float getMinMetric()
    {
        int size = getConnStats().keySet().size();
        if (size > 0)
        {
            float sum = 0;
            for (TagConnection tcon : getConnStats().keySet())
            {
                sum += getConnStats().get(tcon).getBestMetricValue();
            }
            return sum / size;
        }
        else
            return 0;
    }
    
    public Map<Tag, StyleCounter<AreaStyle>> getStyleStats()
    {
        if (styleStats == null)
            styleStats = computeStyleStats();
        return styleStats;
    }
    
    /**
     * The style consistency is computed as the average of the percentages of the most frequent style
     * for each matched tag.
     * @return style consistency value
     */
    public float getStyleConsistency()
    {
        int cnt = 0;
        float sum = 0;
        for (StyleCounter<AreaStyle> style : getStyleStats().values())
        {
            sum += style.getPercentage(style.getMostFrequent());
            cnt++;
        }
        return sum / cnt;
    }
    
    public MatchResultScore getScore()
    {
        return score;
    }
    
    public MatchResultScore evaluateScore()
    {
        score.setMatches(getMatches().size());
        score.setMatchedAreas(getMatchedAreas().size());
        score.setMinConnectionWeight(getMinConnectionWeight());
        score.setMaxConnectionWeight(getMaxConnectionWeight());
        score.setAverageConnectionWeight(getAverageConnectionWeight());
        score.setConnectionWeightSigma(getConnectionWeightSigma());
        score.setMinMetric(getMinMetric());
        score.setStyleConsistency(getStyleConsistency());
        return score;
    }
    
    @Override
    public String toString()
    {
        return getScore().toString();
    }

    //==================================================================================================
    
    private Map<Area, List<AreaConnection>> getConnsM()
    {
        if (connsM == null)
        {
            connsM = new HashMap<>();
            for (Match match : matches)
            {
                for (Match.ConnectionMatch con : match.getAreaConnectionsM1())
                {
                    Area a = con.getAreaConnection().getA2();
                    List<AreaConnection> dest = connsM.get(a);
                    if (dest == null)
                    {
                        dest = new ArrayList<>();
                        connsM.put(a, dest);
                    }
                    dest.add(con.getAreaConnection());
                }
            }
        }
        return connsM;
    }
    
    private Map<Area, Float> getAvgM()
    {
        if (avgM == null)
        {
            avgM = new HashMap<>();
            for (Map.Entry<Area, List<AreaConnection>> entry : getConnsM().entrySet())
            {
                float avg = computeMaxWeight(entry.getValue());
                avgM.put(entry.getKey(), avg);
            }
        }
        return avgM;
    }
    
    private float computeMaxWeight(Collection<AreaConnection> conns)
    {
        float max = 0;
        for (AreaConnection con : conns)
            max = Math.max(max, con.getWeight());
        return max;
    }
    
    private float getMatchAverage(Match match)
    {
        int cnt = match.getAreaConnections1().size() + match.getAreaConnectionsM1().size();
        if (cnt > 0)
        {
            float sum = 0;
            //1:1 connection counted normally
            for (Match.ConnectionMatch con : match.getAreaConnections1())
                sum += con.getAreaConnection().getWeight() * probs.get11Probability(con.getAreaConnection().getRelation());
            //1:M connection use the average for all the connections from the same "1" area
            for (Match.ConnectionMatch con : match.getAreaConnectionsM1())
                //sum += getAvgM().get(con.getAreaConnection().getA2()) * probs.get1MProbability(con.getAreaConnection().getRelation());
                sum += con.getAreaConnection().getWeight() * probs.get1MProbability(con.getAreaConnection().getRelation());
            return sum / cnt;
        }
        else
            return 0;
    }
    
    public void dumpMatchAverages()
    {
        for (Match match : getMatches())
        {
            float f = getMatchAverage(match);
            System.out.println("Match " + match + " avg " + f);
        }
    }
    
    public void dumpMinMetric()
    {
        for (TagConnection tcon : getConnStats().keySet())
        {
            System.out.println(tcon  + " best:" + getConnStats().get(tcon).getBestMetric() + "=" + getConnStats().get(tcon).getBestMetricValue());
            /*for (Set<AreaConnection> con : getConnStats().get(tcon))
            {
                Metric m = getConnStats().get(tcon).getBestMetric();
                float val = m.compute(con);
                System.out.println(val + " =  con: " + con);
            }*/
        }
    }
    
    public void dumpStyleStats()
    {
        Map<Tag, StyleCounter<AreaStyle>> styles = computeStyleStats();
        for (Tag t : styles.keySet())
        {
            StyleCounter<AreaStyle> style = styles.get(t);
            System.out.println(t + " style: " + style.getPercentage(style.getMostFrequent()));
        }
    }
    
    //==================================================================================================
    
    /**
     * Obtains the statistics of the metrics for all the tag connections.
     * @return A map that assigns connection statistics to the individual tag connections.
     */
    public Map<TagConnection, ConnectionStats> getConnStats()
    {
        if (conStats == null)
        {
            conStats = new HashMap<>();
            for (Match match : matches)
            {
                //create a local mapping from tag connections to area connections for the match
                Map<TagConnection, Set<AreaConnection>> mmap = new HashMap<>();
                for (Match.ConnectionMatch con : match.getAreaConnections1())
                {
                    Set<AreaConnection> cons = mmap.get(con.getTagConnection());
                    if (cons == null)
                    {
                        cons = new HashSet<>(1);
                        mmap.put(con.getTagConnection(), cons);
                    }
                    cons.add(con.getAreaConnection());
                }
                for (Match.ConnectionMatch con : match.getAreaConnectionsM1())
                {
                    Set<AreaConnection> cons = mmap.get(con.getTagConnection());
                    if (cons == null)
                    {
                        cons = new HashSet<>();
                        mmap.put(con.getTagConnection(), cons);
                    }
                    cons.add(con.getAreaConnection());
                }
                //distribute the map to global conStats
                for (Map.Entry<TagConnection, Set<AreaConnection>> entry : mmap.entrySet())
                {
                    ConnectionStats statEntry = conStats.get(entry.getKey());
                    if (statEntry == null)
                    {
                        statEntry = new ConnectionStats(entry.getKey());
                        conStats.put(entry.getKey(), statEntry);
                    }
                    statEntry.add(entry.getValue());
                }
            }
        }
        return conStats;
    }
    
    //==================================================================================================
    
    /**
     * Groups the matches by the values of the given key tag.
     * @param keyTag the tag to be used as the key.
     */
    public void groupByKey(Tag keyTag)
    {
        Map<Area, Match> groupMatches = new HashMap<>();
        for (Iterator<Match> it = matches.iterator(); it.hasNext();)
        {
            Match match = (Match) it.next();
            Area key = match.getSingle(keyTag);
            Match group = groupMatches.get(key);
            if (group == null)
                groupMatches.put(key, match);
            else
            {
                group.union(match);
                it.remove();
            }
        }
    }
    
    //==================================================================================================
    
    private Map<Tag, StyleCounter<AreaStyle>> computeStyleStats()
    {
        Map<Tag, StyleCounter<AreaStyle>> styles = new HashMap<>();
        
        for (Match match : matches)
        {
            for (Map.Entry<Tag, List<Area>> entry : match.entrySet())
            {
                //get or create a style counter for each tag
                StyleCounter<AreaStyle> counter = styles.get(entry.getKey());
                if (counter == null)
                {
                    counter = new StyleCounter<>();
                    styles.put(entry.getKey(), counter);
                }
                //add the styles of all areas assigned to the tag to the counter
                for (Area a : entry.getValue())
                    counter.add(new AreaStyle(a));
            }
        }
        return styles;
    }
    
    //==================================================================================================
    
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
        
    }
    
}

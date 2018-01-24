/**
 * MatchResult.java
 *
 * Created on 9. 7. 2017, 22:14:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AreaUtils;
import org.fit.layout.patterns.DefaultRelationProbabilitySource;
import org.fit.layout.patterns.RelationProbabilitySource;

/**
 * A result of matching a single matcher configuration. The result holds the statistics
 * about the areas covered by the matches which allows to compare the different results.
 * 
 * @author burgetr
 */
public class MatchResult implements Comparable<MatchResult>
{
    /** A list of comparators that are used for comparing the match result in the given order. */
    private static List<Comparator<MatchResult>> cclist;
    static {
        cclist = new ArrayList<>(5);
        cclist.add(new Comparator<MatchResult>() //number of matched areas above a threshold
        {
            @Override
            public int compare(MatchResult o1, MatchResult o2)
            {
                if (o1.getStats() != null && o1.getStats() == o2.getStats())
                {
                    final int threshold = o1.getStats().getMaxAreas() / 2;
                    int t1 = (o1.getMatchedAreas().size() > threshold) ? 1 : 0;
                    int t2 = (o2.getMatchedAreas().size() > threshold) ? 1 : 0;
                    return t1 - t2;
                }
                else
                    return 0; //cannot compare, use next comparators
            }
        });
        cclist.add(new Comparator<MatchResult>() //minimal metric standard deviation value (lower is better)
        {
            @Override
            public int compare(MatchResult o1, MatchResult o2)
            {
                if (o1.getMinMetric() < o2.getMinMetric())
                    return 1;
                else if (o1.getMinMetric() > o2.getMinMetric())
                    return -1;
                else
                    return 0;
            }
        });
        /*cclist.add(new Comparator<MatchResult>() //connection weight standard deviation (lower is better)
        {
            @Override
            public int compare(MatchResult o1, MatchResult o2)
            {
                if (o1.getConnectionWeightSigma() < o2.getConnectionWeightSigma())
                    return 1;
                else if (o1.getConnectionWeightSigma() > o2.getConnectionWeightSigma())
                    return -1;
                else
                    return 0;
            }
        });*/
        /*cclist.add(new Comparator<MatchResult>() //average connection weight (greater is better)
        {
            @Override
            public int compare(MatchResult o1, MatchResult o2)
            {
                if (o1.getAverageConnectionWeight() > o2.getAverageConnectionWeight())
                    return 1;
                else if (o1.getAverageConnectionWeight() < o2.getAverageConnectionWeight())
                    return -1;
                else
                    return 0;
            }
        });*/
        cclist.add(new Comparator<MatchResult>() //number of matched areas
        {
            @Override
            public int compare(MatchResult o1, MatchResult o2)
            {
                return o1.getMatchedAreas().size() - o2.getMatchedAreas().size();
            }
        });
        cclist.add(new Comparator<MatchResult>() //total number of matches found
        {
            @Override
            public int compare(MatchResult o1, MatchResult o2)
            {
                return o1.getMatches().size() - o2.getMatches().size();
            }
        });
    }

    
    private RelationProbabilitySource probs;
    private Collection<Match> matches;
    private Set<Area> matchedAreas;
    private MatchStatistics stats;
    private Map<Area, List<AreaConnection>> connsM; //map of 1:M connections to their source areas
    private Map<Area, Float> avgM; //average weights of 1:M connections sharing a single source area
    private Map<TagConnection, ConnectionStats> conStats; //area connection stats for every tag connection
   
    
    public MatchResult(Collection<Match> matches, Set<Area> matchedAreas)
    {
        this.probs = new DefaultRelationProbabilitySource();
        this.matches = matches;
        this.matchedAreas = matchedAreas;
        this.stats = null;
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
    
    public MatchStatistics getStats()
    {
        return stats;
    }

    public void setStats(MatchStatistics stats)
    {
        this.stats = stats;
        updateStats();
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
    
    @Override
    public String toString()
    {
        return matches.size() + " matches, " + matchedAreas.size() + " areas covered"
            + ", w=" + getAverageConnectionWeight() 
            + ", s=" + getConnectionWeightSigma()
            + ", mm=" + getMinMetric()
            //+ ", min=" + getMinConnectionWeight()
            //+ ", max=" + getMaxConnectionWeight()
            ;
    }

    @Override
    public int compareTo(MatchResult o)
    {
        for (Comparator<MatchResult> cc : cclist)
        {
            int comp = cc.compare(this, o);
            if (comp != 0)
                return comp;
        }
        return 0;
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
                sum += getAvgM().get(con.getAreaConnection().getA2()) * probs.get1MProbability(con.getAreaConnection().getRelation());
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
        }
    }
    
    //==================================================================================================
    
    public Map<TagConnection, ConnectionStats> getConnStats()
    {
        if (conStats == null)
        {
            conStats = new HashMap<>();
            for (Match match : matches)
            {
                //create a local map for the match
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
    
    public void updateStats()
    {
        stats.setMaxMatches(Math.max(stats.getMaxMatches(), getMatches().size()));
        stats.setMaxAreas(Math.max(stats.getMaxAreas(), getMatchedAreas().size()));
    }
    
    //==================================================================================================
    
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

        public ConnectionStats(TagConnection tcon)
        {
            this.tcon = tcon;
        }

        public Set<Metric> getMetrics()
        {
            return tcon.getRelation().metrics();
        }
        
        public float getMetricMin(Metric m)
        {
            if (metricMin == null)
                evaluateMetrics();
            Float ret = metricMin.get(m);
            return ret == null ? -1.0f : ret;
        }
        
        public float getMetricMax(Metric m)
        {
            if (metricMax == null)
                evaluateMetrics();
            Float ret = metricMax.get(m);
            return ret == null ? -1.0f : ret;
        }
        
        public float getMetricAvg(Metric m)
        {
            if (metricAvg == null)
                evaluateMetrics();
            Float ret = metricAvg.get(m);
            return ret == null ? -1.0f : ret;
        }
        
        public float getMetricSigma(Metric m)
        {
            if (metricSigma == null)
                evaluateMetrics();
            Float ret = metricSigma.get(m);
            return ret == null ? -1.0f : ret;
        }

        public Metric getBestMetric()
        {
            if (bestMetric == null)
                evaluateMetrics();
            return bestMetric;
        }
        
        public float getBestMetricValue()
        {
            if (bestMetric == null)
                evaluateMetrics();
            return bestMetricValue;
        }
        
        //==================================================================================================
        
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

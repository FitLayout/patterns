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
import org.fit.layout.patterns.eval.ConnectionStats;

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
        score.setConStats(getConnStats());
        //score.setStyleStats(getStyleStats());
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
    
    public void xdumpMatchAverages()
    {
        for (Match match : getMatches())
        {
            float f = getMatchAverage(match);
            System.out.println("Match " + match + " avg " + f);
        }
    }
    
    public void xdumpMinMetric()
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
    
    public void xdumpStyleStats()
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
    
    
}

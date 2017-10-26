/**
 * MatchResult.java
 *
 * Created on 9. 7. 2017, 22:14:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

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
        cclist.add(new Comparator<MatchResult>() //average connection weight (greater is better)
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
        });
        cclist.add(new Comparator<MatchResult>() //connection weight standard deviation (lower is better)
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
        });
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

    
    private List<Match> matches;
    private Set<Area> matchedAreas;
    private MatchStatistics stats;
    
    public MatchResult(List<Match> matches, Set<Area> matchedAreas)
    {
        this.matches = matches;
        this.matchedAreas = matchedAreas;
        this.stats = null;
    }

    public List<Match> getMatches()
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
                min = Math.min(match.getAverageConnectionWeight(), min);
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
                max = Math.max(match.getAverageConnectionWeight(), max);
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
                sum += match.getAverageConnectionWeight();
            return sum / matches.size();
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
                float dif = match.getAverageConnectionWeight() - e;
                sum += dif * dif;
            }
            return (float) Math.sqrt(sum / matches.size());
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
            + ", min=" + getMinConnectionWeight()
            + ", max=" + getMaxConnectionWeight();
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
    
}

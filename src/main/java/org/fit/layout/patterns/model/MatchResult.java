/**
 * MatchResult.java
 *
 * Created on 9. 7. 2017, 22:14:29 by burgetr
 */
package org.fit.layout.patterns.model;

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
    private List<Match> matches;
    private Set<Area> matchedAreas;
    
    public MatchResult(List<Match> matches, Set<Area> matchedAreas)
    {
        this.matches = matches;
        this.matchedAreas = matchedAreas;
    }

    public List<Match> getMatches()
    {
        return matches;
    }

    public Set<Area> getMatchedAreas()
    {
        return matchedAreas;
    }
    
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
    
    @Override
    public String toString()
    {
        return matches.size() + " matches, " + matchedAreas.size() + " areas covered, w=" + getAverageConnectionWeight();
    }

    @Override
    public int compareTo(MatchResult o)
    {
        if (this.getMatchedAreas().size() > o.getMatchedAreas().size())
            return 1;
        else if (this.getMatchedAreas().size() < o.getMatchedAreas().size())
            return -1;
        else
        {
            
            if (this.getMatches().size() > o.getMatches().size())
                return 1;
            else if (this.getMatches().size() < o.getMatches().size())
                return -1;
            else
            {
                if (this.getAverageConnectionWeight() > o.getAverageConnectionWeight())
                    return 1;
                else if (this.getAverageConnectionWeight() < o.getAverageConnectionWeight())
                    return -1;
                else
                    return 0;
            }
        }
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
    
}

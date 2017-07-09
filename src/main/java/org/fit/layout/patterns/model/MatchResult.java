/**
 * MatchResult.java
 *
 * Created on 9. 7. 2017, 22:14:29 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

/**
 * A result of matching a single matcher configuration.
 * @author burgetr
 */
public class MatchResult
{
    private List<Map<Tag, Area>> matches;
    private Set<Area> matchedAreas;
    
    public MatchResult(List<Map<Tag, Area>> matches, Set<Area> matchedAreas)
    {
        this.matches = matches;
        this.matchedAreas = matchedAreas;
    }

    public List<Map<Tag, Area>> getMatches()
    {
        return matches;
    }

    public Set<Area> getMatchedAreas()
    {
        return matchedAreas;
    }

    @Override
    public String toString()
    {
        return matches.size() + " matches, " + matchedAreas.size() + " areas covered";
    }
    
}

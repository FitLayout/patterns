/**
 * MatchAnalyzer.java
 *
 * Created on 1. 11. 2018, 10:46:14 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.HintWholeBox;
import org.fit.layout.patterns.model.Match;
import org.fit.layout.patterns.model.MatchResult;
import org.fit.layout.patterns.model.PresentationHint;
import org.fit.layout.patterns.model.TextChunkArea;

/**
 * Algorithms for analyzing the common visual presentation patterns in a given match result.
 * 
 * @author burgetr
 */
public class MatchAnalyzer
{
    private MatchResult matchResult;

    
    public MatchAnalyzer(MatchResult result)
    {
        this.matchResult = result;
    }
    
    public Set<PresentationHint> findPossibleHints(Tag tag)
    {
        float wholeBox = wholeBoxSupport(tag);
        System.out.println("Whole box support for " + tag + " : " + wholeBox);
        
        return null;
    }

    //============================================================================
    
    private float wholeBoxSupport(Tag tag)
    {
        List<Area> areas = getMatchesFor(tag);
        int cnt = 0;
        for (Area a : areas)
        {
            if (a instanceof TextChunkArea && HintWholeBox.usesWholeBox((TextChunkArea) a))
            {
                cnt++;
            }
        }
        return (float) cnt / areas.size();
    }
    
    //============================================================================
    
    private List<Area> getMatchesFor(Tag tag)
    {
        List<Area> ret = new ArrayList<>();
        for (Match match : matchResult.getMatches())
        {
            ret.addAll(match.get(tag));
        }
        return ret;
    }
    
    
}

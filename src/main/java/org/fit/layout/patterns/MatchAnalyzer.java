/**
 * MatchAnalyzer.java
 *
 * Created on 1. 11. 2018, 10:46:14 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        
        float inLine = inLineSupport(tag);
        //System.out.println("In line support for " + tag + " : " + inLine);
        
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
    
    private float inLineSupport(Tag tag)
    {
        int totalPairs = 0;
        int inLinePairs = 0;
        int totalMatches = 0;
        int multiLineMatches = 0;
        for (Match match : matchResult.getMatches())
        {
            if (match.get(tag).size() > 1)
            {
                boolean multiLine = false;
                List<Area> sorted = getSortedMatch(match, tag);
                Area a1 = sorted.get(0);
                for (int i = 1; i < sorted.size(); i++)
                {
                    Area a2 = sorted.get(i);
                    if (AreaUtils.isOnSameLine(a1, a2))
                        inLinePairs++;
                    else
                        multiLine = true;
                    totalPairs++;
                    a1 = a2;
                }
                if (multiLine)
                    multiLineMatches++;
                totalMatches++;
            } 
        }
        float inLine = (totalPairs == 0) ? 0.0f : (float) inLinePairs / totalPairs;
        float multiLine = (totalMatches == 0) ? 0.0f : (float) multiLineMatches / totalMatches;
        System.out.println("In line support for " + tag + " : " + inLine);
        System.out.println("Multiline support for " + tag + " : " + multiLine);
        return inLine;
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
    
    private List<Area> getSortedMatch(Match match, Tag tag)
    {
        List<Area> sorted = new ArrayList<>(match.get(tag));
        Collections.sort(sorted, new Comparator<Area>()
        {
            @Override
            public int compare(Area o1, Area o2)
            {
                if (o1.getY1() == o2.getY1())
                    return o1.getX1() - o2.getX1();
                else
                    return o1.getY1() - o2.getY1();
            }
        });
        return sorted;
    }
    
}

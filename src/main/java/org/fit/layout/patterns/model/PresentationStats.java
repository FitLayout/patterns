/**
 * PresentationStats.java
 *
 * Created on 21. 6. 2018, 10:59:23 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.Relation;
import org.fit.layout.patterns.RelationAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the statistics about the visual presentation of a given tag and relationship.
 * 
 * @author burgetr
 */
public class PresentationStats
{
    private static Logger log = LoggerFactory.getLogger(PresentationStats.class);

    private Tag tag;
    private RelationAnalyzer ra;
    
    private List<Area> areas;
    
    private StyleCounter<Relation> relCounts;
    
    
    public PresentationStats(Tag tag, RelationAnalyzer ra)
    {
        this.tag = tag;
        this.ra = ra;
        areas = new ArrayList<>();
    }
    
    public void addArea(Area a)
    {
        areas.add(a);
    }
    
    public List<PresentationHint> getPresentationHints()
    {
        List<PresentationHint> ret = new ArrayList<>();
        
        //TODO add more attributes
        AttrSameLine sameLine = new AttrSameLine();
        ret.addAll(sameLine.inferHints(tag, ra));
        
        return ret;
    }

    //========================================================================
    
    private void analyzePairs()
    {
        relCounts = new StyleCounter<>();
        for (Area a1 : areas)
        {
            for (Area a2 : areas)
            {
                if (a1 != a2)
                {
                    Set<Relation> rels = ra.getRelationsFor(a1, a2, -1.0f);
                    for (Relation r : rels)
                        relCounts.add(r);
                }
            }
        }
    }
    
    
}

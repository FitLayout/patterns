/**
 * AttrSameLine.java
 *
 * Created on 26. 6. 2018, 11:30:31 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.Relation;
import org.fit.layout.patterns.RelationAnalyzer;
import org.fit.layout.patterns.RelationSameLine;

/**
 * 
 * @author burgetr
 */
public class AttrSameLine implements PresentationAttribute
{
    private static Relation rel = new RelationSameLine();
    

    public List<PresentationHint> inferHints(Tag tag, RelationAnalyzer ra)
    {
        StyleCounter<String> separators = scanOccurences(tag, ra);
        List<PresentationHint> hints = new ArrayList<>();
        for (String separator : separators.getFrequentStyles(0.7f))
        {
            hints.add(new HintSeparator(tag, separator));
        }
        return hints;
    }
    
    private StyleCounter<String> scanOccurences(Tag tag, RelationAnalyzer ra)
    {
        StyleCounter<String> separators = new StyleCounter<>(); 
        Set<Area> src = ra.getSourceAreas();
        for (Area a1 : src)
        {
            if (a1.hasTag(tag))
            {
                List<Area> pairs = ra.getAreasInBestRelation(a1, rel);
                for (Area a2 : pairs)
                {
                    if (a2.hasTag(tag))
                    {
                        String sep = getStringBetween(a1, a2, ra);
                        if (sep != null)
                        {
                            sep = sep.trim();
                            if (!sep.isEmpty())
                            {
                                separators.add(sep);
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("Separators " + tag + ": " + separators);
        return separators;
    }
    
    private String getStringBetween(Area a1, Area a2, RelationAnalyzer ra)
    {
        AreaTopology topology = ra.getTopology();
        Rectangular b1 = topology.getPosition(a1);
        Rectangular b2 = topology.getPosition(a2);
        if (b1.getX2() < b2.getX1())
        {
            List<Area> between = new ArrayList<>();
            for (int x = b1.getX2() + 1; x < b2.getX1(); x++)
            {
                for (int y = Math.min(b1.getY1(), b2.getY1()); y <= Math.max(b1.getY2(), b2.getY2()); y++)
                {
                    Area cand = topology.findAreaAt(x, y);
                    if (cand != null)
                    {
                        if (!between.contains(cand))
                            between.add(cand);
                        x = cand.getX2();
                        y = cand.getY2();
                    }
                }
            }
            StringBuilder ret = new StringBuilder();
            for (Area a : between)
            {
                ret.append(a.getText());
            }
            return ret.toString();
        }
        else
            return null;
    }
    
}

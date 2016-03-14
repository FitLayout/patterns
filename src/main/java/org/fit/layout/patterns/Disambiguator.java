/**
 * Disambiguator.java
 *
 * Created on 13. 3. 2016, 11:48:08 by burgetr
 */
package org.fit.layout.patterns;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.NodeStyle;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.ConsistentAreaAnalyzer.ChainList;

/**
 * A configurable disambiguator that is able to choose a single tag for the given area.
 * @author burgetr
 */
public class Disambiguator
{
    private Map<Tag, NodeStyle> styles;
    private ChainList chains;
    private float minSupport;

    
    public Disambiguator(Map<Tag, NodeStyle> styles, ChainList chains, float minSupport)
    {
        this.styles = styles;
        this.chains = chains;
        this.minSupport = minSupport;
    }

    public Tag getAreaTag(Area a)
    {
        /*if (a.getId() == 242)
            System.out.println("jo!");*/
        //tags originally assigned
        Set<Tag> orig = a.getSupportedTags(minSupport);
        //tags assigned by style
        Set<Tag> byStyle = new HashSet<>();
        NodeStyle astyle = new NodeStyle(a);
        for (Map.Entry<Tag, NodeStyle> entry : styles.entrySet())
        {
            if (entry.getValue().equals(astyle))
                byStyle.add(entry.getKey());
        }
        //do not assign new tags by style now, only consider those already assigned
        byStyle.retainAll(orig);
        //any chains
        Set<Tag> byChains = new HashSet<Tag>(chains.findChainTagsForArea(a));
        byChains.retainAll(orig);
        //Set<Tag> byChains = new HashSet<Tag>();
        //intersection
        Set<Tag> ret;
        if (byStyle.isEmpty())
            ret = byChains;
        else if (byChains.isEmpty())
            ret = byStyle;
        else
        {
            ret = byStyle;
            ret.retainAll(byChains);
        }
        //validate the result
        Iterator<Tag> it = ret.iterator();
        if (ret.size() == 0)
            return null;
        else if (ret.size() == 1)
            return it.next();
        else
        {
            float max = 0;
            Tag result = null;
            while (it.hasNext())
            {
                Tag t = it.next();
                float sup = a.getTagSupport(t);
                if (sup > max)
                {
                    result = t;
                    max = sup;
                }
            }
            return result;
        }
    }
    
}

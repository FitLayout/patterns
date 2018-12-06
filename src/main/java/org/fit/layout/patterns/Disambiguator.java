/**
 * Disambiguator.java
 *
 * Created on 13. 3. 2016, 11:48:08 by burgetr
 */
package org.fit.layout.patterns;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.ConsistentAreaAnalyzer.ChainList;

/**
 * A configurable disambiguator that is able to choose a single tag for the given area.
 * @author burgetr
 */
public class Disambiguator
{
    private StyleAnalyzer styles;
    private ChainList chains;
    private float minSupport;
    private boolean allowNewTags; //allow assigning the tags not assigned by text tagging
    
    public Disambiguator(StyleAnalyzer styles, ChainList chains, float minSupport)
    {
        this.styles = styles;
        this.chains = chains;
        this.minSupport = minSupport;
        this.allowNewTags = false;
    }

    public Disambiguator(StyleAnalyzer styles, ChainList chains, float minSupport, boolean allowNewTags)
    {
        this.styles = styles;
        this.chains = chains;
        this.minSupport = minSupport;
        this.allowNewTags = allowNewTags;
    }

    public StyleAnalyzer getStyleAnalyzer()
    {
        return styles;
    }

    public Tag getAreaTag(Area a)
    {
        //if (a.getId() == 119)
        //    System.out.println("jo!");
        //tags originally assigned
        Set<Tag> orig = a.getSupportedTags(minSupport);
        //tags assigned by style
        Set<Tag> byStyle = styles.inferTags(a);
        if (!allowNewTags)
            byStyle.retainAll(orig); //do not assign new tags by style now, only consider those already assigned
        //any chains: if necessary, try to disambiguate the information obtained by style
        if (byStyle.size() > 1 && chains != null)
        {
            Set<Tag> byChains = new HashSet<Tag>(chains.findChainTagsForArea(a));
            if (!byChains.isEmpty())
                byStyle.retainAll(byChains);
        }
        //the remaining tags are the result
        Set<Tag> ret = byStyle;
        //validate the result: we need only one tag
        Iterator<Tag> it = ret.iterator();
        if (ret.size() == 0)
            return null; //no tags decided
        else if (ret.size() == 1)
            return it.next(); //a single tag, return it
        else
        {
            float max = 0; //otherwise, choose the most supported tag
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((styles == null) ? 0 : styles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Disambiguator other = (Disambiguator) obj;
        if (styles == null)
        {
            if (other.styles != null) return false;
        }
        else if (!styles.equals(other.styles)) return false;
        return true;
    }
    
}

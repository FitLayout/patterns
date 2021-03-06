/**
 * StyleResolver.java
 *
 * Created on 13. 3. 2016, 11:48:08 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;


/**
 * This resolver assigns tags to areas based on their style.
 * 
 * @author burgetr
 */
public class StyleResolver implements TagResolver
{
    private StyleAnalyzer styles;
    private float minSupport;
    private boolean allowNewTags; //allow assigning the tags not assigned by text tagging
    
    
    public StyleResolver(StyleAnalyzer styles, float minSupport)
    {
        this.styles = styles;
        this.minSupport = minSupport;
        this.allowNewTags = false;
    }

    public StyleResolver(StyleAnalyzer styles, float minSupport, boolean allowNewTags)
    {
        this.styles = styles;
        this.minSupport = minSupport;
        this.allowNewTags = allowNewTags;
    }

    public StyleAnalyzer getStyleAnalyzer()
    {
        return styles;
    }

    @Override
    public Set<Tag> getAreaTags(Area a)
    {
        //tags originally assigned
        final Set<Tag> orig = a.getSupportedTags(minSupport);
        //tags assigned by style
        final Set<Tag> byStyle = styles.inferTags(a);
        if (!allowNewTags)
            byStyle.retainAll(orig); //do not assign new tags by style now, only consider those already assigned
        //the remaining tags are the result
        return byStyle;
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
        StyleResolver other = (StyleResolver) obj;
        if (styles == null)
        {
            if (other.styles != null) return false;
        }
        else if (!styles.equals(other.styles)) return false;
        return true;
    }
    
}

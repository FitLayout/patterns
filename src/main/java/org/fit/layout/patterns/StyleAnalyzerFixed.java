/**
 * StyleAnalyzerFixed.java
 *
 * Created on 19. 3. 2016, 20:22:54 by burgetr
 */
package org.fit.layout.patterns;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

/**
 * A style analyzer that assigns the tags by comparing the area
 * style with a fixed mapping of existing tags and their styles.
 * 
 * @author burgetr
 */
public class StyleAnalyzerFixed implements StyleAnalyzer
{
    private Map<Tag, AreaStyle> styles;
    
    
    /**
     * Creates a new style analyzer.
     * @param styles the tag to style mapping.
     */
    public StyleAnalyzerFixed(Map<Tag, AreaStyle> styles)
    {
        this.styles = styles;
    }

    @Override
    public Set<Tag> inferTags(Area a)
    {
        Set<Tag> ret = new HashSet<>();
        AreaStyle astyle = new AreaStyle(a);
        for (Map.Entry<Tag, AreaStyle> entry : styles.entrySet())
        {
            if (entry.getValue().isComparableTo((astyle)))
                ret.add(entry.getKey());
        }
        return ret;
    }

}

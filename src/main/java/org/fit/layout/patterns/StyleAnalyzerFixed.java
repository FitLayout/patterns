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
            if (isComparableStyle(entry.getValue(), (astyle)))
                ret.add(entry.getKey());
        }
        return ret;
    }

    /**
     * Decides whether the two styles do basically "look the same".
     * @param s1
     * @param s2
     * @return
     */
    private boolean isComparableStyle(AreaStyle s1, AreaStyle s2)
    {
        /*if (s1.equals(s2))
            return true; //basically "same" text style
        else //not the same style
        {
            //the same color background and similar size
            if (s1.getBgColor() != null && s2.getBgColor() != null && s1.getBgColor().equals(s2.getBgColor()))
            {
                float dw = Math.abs(s1.getWidth() - s2.getWidth()) / (float) s1.getWidth();
                float dh = Math.abs(s1.getHeight() - s2.getHeight()) / (float) s1.getHeight();
                if ((dw < 0.1f && dh < 0.5f) || (dh < 0.1f && dw < 0.5f))
                        return true;
            }
        }
        return false;*/
        return valueMatches(s1.getFontSize(), s2.getFontSize())
                && valueMatches(s1.getWeight(), s2.getWeight())
                && valueMatches(s1.getStyle(), s2.getStyle())
                && valueMatches(s1.getColor(), s2.getColor())
                && valueMatches(s1.getBgColor(), s2.getBgColor());
    }
    
    private boolean valueMatches(float val1, float val2)
    {
        return val1 == -1.0f || val2 == -1.0f || Math.abs(val2 - val1) < 0.001f;
    }
    
    private boolean valueMatches(Object o1, Object o2)
    {
        return o1 == null || o2 == null || o1.equals(o2);
    }
    
}

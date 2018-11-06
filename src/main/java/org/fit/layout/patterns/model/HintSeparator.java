/**
 * HintSeparator.java
 *
 * Created on 29. 6. 2018, 15:38:44 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class HintSeparator extends DefaultHint
{
    private String separator;
    private Tag tag;

    
    public HintSeparator(Tag tag, String separator)
    {
        this.tag = tag;
        this.separator = separator;
    }

    public Tag getTag()
    {
        return tag;
    }
    
    public String getSeparator()
    {
        return separator;
    }
    
    @Override
    public String toString()
    {
        return tag.toString() + " separated by '" + separator + "'";
    }
    
    @Override
    public List<Area> apply(List<Area> src)
    {
        //TODO a dummy implementation
        return src;
    }

}

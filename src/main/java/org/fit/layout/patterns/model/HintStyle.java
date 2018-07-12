/**
 * HintStyle.java
 *
 * Created on 10. 7. 2018, 15:37:07 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.Disambiguator;

/**
 * This hint forces the chunk source to use only the specific styles for the given tag.
 * 
 * @author burgetr
 */
public class HintStyle implements PresentationHint
{
    private Tag tag;
    private Disambiguator dis;
    

    public HintStyle(Tag tag, Disambiguator dis)
    {
        this.tag = tag;
        this.dis = dis;
    }

    public Tag getTag()
    {
        return tag;
    }

    public Disambiguator getDisambiguator()
    {
        return dis;
    }
    
    @Override
    public List<Area> apply(List<Area> areas)
    {
        for (Area a : areas)
        {
            if (a instanceof TextChunkArea)
            {
                Tag dtag = dis.getAreaTag(((TextChunkArea) a).getSourceArea());
                if (a.hasTag(tag) && dtag != null && !dtag.equals(tag))
                    a.removeTag(tag);
            }
        }
        return areas;
    }
    
}

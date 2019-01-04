/**
 * HintStyle.java
 *
 * Created on 10. 7. 2018, 15:37:07 by burgetr
 */
package org.fit.layout.patterns.chunks;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.Disambiguator;
import org.fit.layout.patterns.StyleAnalyzerFixed;

/**
 * This hint forces the chunk source to use only the specific styles for the given tag.
 * 
 * @author burgetr
 */
public class HintStyle extends DefaultHint
{
    private Tag tag;
    private Disambiguator dis;
    

    public HintStyle(Tag tag, Disambiguator dis)
    {
        super("Style");
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
    public List<Area> postprocessChunks(List<Area> areas)
    {
        List<Area> ret = new ArrayList<Area>(areas.size());
        for (Area a : areas)
        {
            if (a instanceof TextChunkArea)
            {
                Tag dtag = dis.getAreaTag(((TextChunkArea) a).getSourceArea());
                if (a.hasTag(tag) && (dtag == null || !dtag.equals(tag))) //TODO tag support?
                {
                    a.removeTag(tag);
                    a.setName("!" + a.getName());
                }
                else
                {
                    ret.add(a);
                }
            }
        }
        return ret;
    }
    
    @Override
    public String toString()
    {
        String style = "";
        if (dis.getStyleAnalyzer() instanceof StyleAnalyzerFixed)
            style = ((StyleAnalyzerFixed) dis.getStyleAnalyzer()).getStyleForTag(tag).toString();
        else
            style = dis.getStyleAnalyzer().toString();
            
        return "Style:" + style;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dis == null) ? 0 : dis.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        HintStyle other = (HintStyle) obj;
        if (dis == null)
        {
            if (other.dis != null) return false;
        }
        else if (!dis.equals(other.dis)) return false;
        return true;
    }
    
}

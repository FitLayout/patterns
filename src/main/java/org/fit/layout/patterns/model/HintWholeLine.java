/**
 * HintWholeLine.java
 *
 * Created on 31. 10. 2018, 13:24:31 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AreaUtils;

/**
 * A hint that forces using the whole line for the corresponding chunk even if only part
 * of the line was detected as the chunk. 
 * @author burgetr
 */
public class HintWholeLine implements PresentationHint
{
    private Tag tag;
    
    public HintWholeLine(Tag tag)
    {
        this.tag = tag;
    }

    @Override
    public List<Area> apply(List<Area> areas)
    {
        for (Area a : areas)
        {
            if (a instanceof TextChunkArea)
            {
                TextChunkArea chunk = (TextChunkArea) a;
                Area srcArea = chunk.getSourceArea();
                if (srcArea != null && srcArea.getParent() != null)
                {
                    AreaTopology topology = srcArea.getParent().getTopology();
                    List<Area> before = new ArrayList<>();
                    List<Area> after = new ArrayList<>();
                    AreaUtils.findAreasBeforeAfter(srcArea, topology, before, after);
                    //TODO
                }
            }
        }
        return areas;
    }

    @Override
    public String toString()
    {
        return "WholeLine";
    }

}

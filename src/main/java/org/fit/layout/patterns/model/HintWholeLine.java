/**
 * HintWholeLine.java
 *
 * Created on 31. 10. 2018, 13:24:31 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;

/**
 * A hint that forces using the whole line for the corresponding chunk even if only part
 * of the line was detected as the chunk. 
 * @author burgetr
 */
public class HintWholeLine implements PresentationHint
{

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
                    Rectangular gp = topology.getPosition(srcArea);
                    
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

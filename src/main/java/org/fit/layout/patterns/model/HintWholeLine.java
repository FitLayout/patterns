/**
 * HintWholeLine.java
 *
 * Created on 31. 10. 2018, 13:24:31 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.impl.DefaultContentLine;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.ContentLine;
import org.fit.layout.model.Tag;

/**
 * A hint that forces using the whole line for the corresponding chunk even if only part
 * of the line was detected as the chunk. This is similar to GetWholeBox hint but in addition
 * all the areas on the same line are considered.
 * @author burgetr
 */
public class HintWholeLine extends HintWholeBox
{
    
    public HintWholeLine(Tag tag)
    {
        super(tag);
    }

    @Override
    public List<Box> extractBoxes(Area a, List<Box> current, Set<Area> processed)
    {
        //TODO include all areas on the line as well
        return new ArrayList<>(a.getBoxes());
    }
    
    @Override
    public List<Area> processChunks(Area src, List<Area> areas)
    {
        //put all the resulting areas to a common logical content line
        ContentLine line = new DefaultContentLine(areas.size());
        line.addAll(areas);
        return areas;
    }

    @Override
    public String toString()
    {
        return "WholeLine";
    }

}

/**
 * HintInLine.java
 *
 * Created on 6. 11. 2018, 21:31:40 by burgetr
 */
package org.fit.layout.patterns.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.impl.DefaultContentLine;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.ContentLine;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AreaUtils;
import org.fit.layout.patterns.Disambiguator;

/**
 * This hint causes considering multiple subsequent lines to be treated as a single chunk source area.
 * It assumes that every line is a single source area with certain style.
 * 
 * @author burgetr
 */
public class HintInLine extends DefaultHint
{
    private static final int STEPDIF_THRESHOLD = 2; //pixels tolerance in the step difference between lines
    
    private Tag tag;
    private Disambiguator dis;

    
    public HintInLine(Tag tag, Disambiguator dis)
    {
        super("InLine");
        this.tag = tag;
        this.dis = dis;
    }

    @Override
    public SourceBoxList extractBoxes(Area a, SourceBoxList current, Set<Area> processed)
    {
        Tag dtag = dis.getAreaTag(a);
        if (tag.equals(dtag)) //the disambiguator must assign the destination tag to this area
        {
            if (a.getParent() != null) //the area must have a parent in order to work with the topology
            {
                //try to discover subsequent lines
                AreaTopology topology = a.getParent().getTopology();
                List<Area> lines = findConsistentLines(a, topology);
                if (lines.size() > 1) //more than the source line found
                {
                    //System.out.println("Consistent lines for A=" + a);
                    for (Area aa : lines)
                    {
                        //System.out.println("    " + aa);
                        if (aa != a)
                        {
                            current.addAll(aa.getBoxes());
                            processed.add(aa);
                        }
                    }
                    //the chunks are spread over multiple lines - they may use an inline layout
                    current.setBlockLayout(false);
                }
                return current;
            }
            else
                return current; //no operation
        }
        else
            return current;
    }
    
    @Override
    public List<Area> processChunks(Area src, List<Area> areas)
    {
        //put all the resulting areas to a common logical content line
        ContentLine line = new DefaultContentLine(areas.size());
        line.addAll(areas);
        return areas;
    }

    //===================================================================================================================
    
    private List<Area> findConsistentLines(Area a, AreaTopology topology)
    {
        List<Area> ret = new ArrayList<>();
        ret.add(a);
        Area last = a;
        Rectangular lastr = topology.getPosition(last);
        int laststep = -1;
        boolean found = true;
        while (found)
        {
            found = false;
            Area next = findLineBelow(last, topology);
            if (next != null)
            {
                Rectangular nextr = topology.getPosition(last);
                
                int step = nextr.getY1() - lastr.getY2();
                int stepdif = Math.abs(laststep - step);
                
                Tag dtag = dis.getAreaTag(next);
                if ((next.hasTag(tag) || tag.equals(dtag)) //assigned or inferred tag corresponds to the target tag
                        && (laststep == -1 || stepdif <= STEPDIF_THRESHOLD)) //TODO should the assigned tag be used?
                {
                    ret.add(next);
                    last = next;
                    lastr = nextr;
                    laststep = step;
                    found = true;
                }
            }
        }
        return ret;
    }
    
    private Area findLineBelow(Area a, AreaTopology topology)
    {
        List<Area> cands = new ArrayList<>(); 
        AreaUtils.findAreasBelow(a, topology, cands);
        if (cands.size() == 1)
            return cands.get(0);
        else
            return null;
    }

}

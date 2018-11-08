/**
 * RelationLineBelow.java
 *
 * Created on 13. 3. 2018, 14:42:14 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.DefaultMetrics;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public class RelationLineBelow extends LineRelation implements BulkRelation
{

    public RelationLineBelow()
    {
        super("lineBelow");
    }
    
    @Override
    public Set<AreaConnection> findRelations(AreaTopology topology, Collection<Area> areas)
    {
        Set<AreaConnection> ret = new HashSet<>();
        for (Area a : areas)
            findLineBelow(a, topology, areas, ret);
        return ret;
    }
    
    public Set<Metric> metrics()
    {
        return DefaultMetrics.allMetrics;
    }
    
    //========================================================================================
    
    private void findLineBelow(Area a, AreaTopology t, Collection<Area> areas, Set<AreaConnection> dest)
    {
        //find the closest area
        float maxW = 0;
        Area closest = null;
        for (Area cand : areas)
        {
            float w = computeWeight(cand, a, t);
            if (w > maxW)
            {
                closest = cand;
                maxW = w;
            }
        }
        //find all on the same line
        if (closest != null)
        {
            Set<Area> used = new HashSet<>();
            for (Area cand : areas)
            {
                if ((cand == closest || AreaUtils.isOnSameLine(cand, closest)) && !used.contains(cand))
                {
                    float w = computeWeight(cand, a, t);
                    AreaConnection con = new AreaConnection(cand, a, this, w);
                    dest.add(con);
                    used.add(cand);
                    //try to use the chunks on the same logical line (if any)
                    if (cand.getLine() != null)
                    {
                        for (Area sibl : cand.getLine())
                        {
                            if (!used.contains(sibl))
                            {
                                AreaConnection scon = new AreaConnection(sibl, a, this, w);
                                dest.add(scon);
                                used.add(sibl);
                            }
                        }
                    }
                }   
            }
        }
    }
    
    private float computeWeight(Area a1, Area a2, AreaTopology topology)
    {
        //a2 is the heading, a1 should be under the heading
        float distX = Math.abs(a1.getBounds().getX1() - a2.getBounds().getX1());
        float distY = a1.getBounds().getY1() - a2.getBounds().getY2();
        float em = a1.getFontSize();
        if (distY >= -0.5f*em)
        {
            int tw = topology.getTopologyPosition().getWidth();
            int th = topology.getTopologyPosition().getHeight();
            float ww = 1.0f - distX / tw;
            float wh = 1.0f - distY / th;
            float w = ww * wh;
            return w;
        }
        else
            return 0;
    }

}

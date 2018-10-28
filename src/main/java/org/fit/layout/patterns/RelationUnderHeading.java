/**
 * RelationUnderHeading.java
 *
 * Created on 4. 12. 2017, 15:01:04 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.DefaultMetrics;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public class RelationUnderHeading extends BaseRelation implements BulkRelation
{

    public RelationUnderHeading()
    {
        super("underHeading");
    }

    @Override
    public Set<AreaConnection> findRelations(AreaTopology topology, Collection<Area> areas)
    {
        Set<AreaConnection> ret = new HashSet<>();
        for (Area a : areas)
            findSubordinate(a, topology, areas, ret);
        return ret;
    }
    
    public Set<Metric> metrics()
    {
        return DefaultMetrics.allMetrics;
    }
    
    private float getMarkedness(Area a)
    {
        //simplified markedness version, use font size and weight only
        return a.getFontSize() * 10 + a.getFontWeight();
    }
    
    private float computeWeight(Area a1, Area a2, AreaTopology topology)
    {
        //a2 is the heading, a1 should be under the heading
        float distX = a1.getBounds().getX1() - a2.getBounds().getX1();
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
    
    private void findSubordinate(Area a, AreaTopology t, Collection<Area> areas, Set<AreaConnection> dest)
    {
        float m1 = getMarkedness(a);
        Rectangular gp = new Rectangular(t.getPosition(a));
        
        List<Area> candidates = new ArrayList<>();
        while (expandDown(gp, t, m1, candidates))
            ;
        
        for (Area c : candidates)
        {
            float w = computeWeight(c, a, t);
            AreaConnection con = new AreaConnection(c, a, this, w);
            dest.add(con);
        }
        
        /*if (!candidates.isEmpty())
        {
            System.out.println("Heading: " + a);
            for (Area c : candidates)
                System.out.println("    " + c);
        }*/
    }
    
    private boolean expandDown(Rectangular gp, AreaTopology t, float m1, List<Area> destAreas)
    {
        int nextY = gp.getY2() + 1;
        if (nextY < t.getTopologyHeight())
        {
            boolean found = false;
            int x = gp.getX1();
            while (x <= gp.getX2())
            {
                Collection<Area> cands = t.findAllAreasAt(x, nextY);
                if (!cands.isEmpty())
                {
                    Rectangular cgp = null;
                    for (Area cand : cands)
                    {
                        if (getMarkedness(cand) < m1) //acceptable candidate
                        {
                            found = true;
                            destAreas.add(cand);
                            if (cgp == null)
                                cgp = t.getPosition(cand);
                            else
                                cgp.expandToEnclose(t.getPosition(cand));
                        }
                        else
                        {
                            return false; //unacceptable candidate found - cannot expand
                        }
                    }
                    if (cgp != null)
                    {
                        gp.expandToEnclose(cgp);
                        x += cgp.getWidth();
                    }
                }
                else
                    x++;
            }
            if (!found) //empty row, try the next one
                gp.setY2(nextY);
            return true;
        }
        else
            return false;
    }
 
}

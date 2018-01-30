/**
 * RelationUnderHeading.java
 *
 * Created on 4. 12. 2017, 15:01:04 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.patterns.model.DefaultMetrics;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public class RelationUnderHeading extends Relation
{

    public RelationUnderHeading()
    {
        super("underHeading");
    }

    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology, Collection<Area> areas)
    {
        if (a2.getId() == 6 && a1.getId() == 45)
            System.out.println("jo!");
        if (a2.getId() == 39 && a1.getId() == 45)
            System.out.println("jo!");
        //a2 is the heading, a1 should be under the heading
        if (a1.getBounds().getY1() >= a2.getBounds().getY2())
        {
            float m1 = getMarkedness(a1);
            float m2 = getMarkedness(a2);
            if (m2 >= m1) //the heading must have at least the same markedness
            {
                //check whether the heading really works as heading
                List<Area> before = new ArrayList<>();
                List<Area> after = new ArrayList<>();
                AreaUtils.findAreasBeforeAfter(a2, areas, topology, before, after);
                for (Area a : before)
                {
                    if (getMarkedness(a) > m2)
                        return 0; //not a heading
                }
                for (Area a : after)
                {
                    if (getMarkedness(a) > m2)
                        return 0; //not a heading
                }
                //is in relationship, compute the weight
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
            else
                return 0;
        }
        else
            return 0;
    }
    
    public Set<Metric> metrics()
    {
        return DefaultMetrics.widthMetrics;
    }
    
    private float getMarkedness(Area a)
    {
        //simplified markedness version, use font size and weight only
        return a.getFontSize() * 10 + a.getFontWeight();
    }
    
    
    
}
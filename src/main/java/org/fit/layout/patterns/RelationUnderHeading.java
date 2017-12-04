/**
 * RelationUnderHeading.java
 *
 * Created on 4. 12. 2017, 15:01:04 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;

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
                float dist = a1.getBounds().getY1() - a2.getBounds().getY2();
                float em = Math.max(a2.getFontSize(), a1.getFontSize());
                if (dist >= -0.5f*em)
                {
                    int tw = topology.getTopologyPosition().getHeight();
                    float w = 1.0f - dist / tw;
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
    
    private float getMarkedness(Area a)
    {
        //simplified markedness version, use font size and weight only
        return a.getFontSize() * 10 + a.getFontWeight();
    }
    
    
    
}

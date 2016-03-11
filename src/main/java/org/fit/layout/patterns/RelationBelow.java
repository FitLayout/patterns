/**
 * RelationBelow.java
 *
 * Created on 29. 2. 2016, 0:00:11 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class RelationBelow extends Relation
{

    public RelationBelow()
    {
        super("below");
    }

    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology)
    {
        if (a1.getId() == 116 && a2.getId() == 583)
            System.out.println("jo!");
        //here a1 is the bottom area, a2 is the top area
        //we say that a1 is below a2
        final Rectangular gp1 = topology.getPosition(a1);
        final Rectangular gp2 = topology.getPosition(a2);
        Rectangular inter = gp1.intersection(new Rectangular(gp2.getX1(), gp1.getY1(), gp2.getX2(), gp1.getY2()));
        if (inter.getWidth() > Math.min(gp1.getWidth(), gp2.getWidth()) / 2) //at least 1/2 of the smaller area overlaps
        {
            float dist = a1.getBounds().getY1() - a2.getBounds().getY2();
            float em = Math.max(a2.getFontSize(), a1.getFontSize());
            if (dist >= 0.0f && dist < 0.8*em)
                return 1.0f;
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

}

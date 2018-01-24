/**
 * RelationBelow.java
 *
 * Created on 17. 3. 2016, 23:37:55 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;
import org.fit.layout.patterns.model.DefaultMetrics;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public class RelationBelow extends Relation
{
    private boolean inverse;
    
    public RelationBelow()
    {
        this(false);
    }
    
    public RelationBelow(boolean inverse)
    {
        super(inverse ? "above" : "below");
        this.inverse = inverse;
    }

    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology, Collection<Area> areas)
    {
        //here a1 is the bottom area, a2 is the top area
        //we say that a1 is below a2
        if (inverse)
        {
            Area x = a2; a2 = a1; a1 = x;
        }
        final Rectangular gp1 = topology.getPosition(a1);
        final Rectangular gp2 = topology.getPosition(a2);
        Rectangular inter = gp1.intersection(new Rectangular(gp2.getX1(), gp1.getY1(), gp2.getX2(), gp1.getY2()));
        if (inter.getWidth() > Math.min(gp1.getWidth(), gp2.getWidth()) / 2) //at least 1/2 of the smaller area overlaps
        {
            float dist = a1.getBounds().getY1() - a2.getBounds().getY2();
            float em = Math.max(a2.getFontSize(), a1.getFontSize());
            if (dist >= -0.5f*em)
            {
                int tw = topology.getTopologyPosition().getHeight();
                float w = 1.0f - dist / tw;
                return w;
            }
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

    public Set<Metric> metrics()
    {
        return DefaultMetrics.widthMetrics;
    }

}

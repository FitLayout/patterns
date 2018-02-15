/**
 * SideRelationship.java
 *
 * Created on 28. 2. 2016, 18:02:49 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.patterns.model.DefaultMetrics;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public class RelationSide extends LineRelation implements SimpleRelation
{
    private boolean inverse;

    public RelationSide()
    {
        this(false);
    }

    public RelationSide(boolean inverse)
    {
        super(inverse ? "onRight" : "onLeft");
        this.inverse = inverse;
    }

    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology, Collection<Area> areas)
    {
        //here a1 is the right area, a2 is the left area
        //we say that a1 is on side of a2
        if (isOnSameLine(a2, a1))
        {
            float dist = inverse ?
                            a2.getBounds().getX1() - a1.getBounds().getX2()
                            : a1.getBounds().getX1() - a2.getBounds().getX2();
            float em = Math.max(a2.getFontSize(), a1.getFontSize());
            if (dist > -0.2*em && dist < 0.9*em)
                return 1.0f;
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

    public Set<Metric> metrics()
    {
        return DefaultMetrics.heightMetrics;
    }
    
}

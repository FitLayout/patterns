/**
 * RelationAfter.java
 *
 * Created on 15. 3. 2016, 10:41:43 by burgetr
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
public class RelationAfter extends LineRelation implements SimpleRelation
{
    private boolean inverse;

    public RelationAfter()
    {
        this(false);
    }
    
    public RelationAfter(boolean inverse)
    {
        super(inverse ? "before" : "after");
        this.inverse = inverse;
    }

    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology, Collection<Area> areas)
    {
        //here a1 is the right area, a2 is the left area
        //we say that a1 is after a2
        if (AreaUtils.isOnSameLine(a2, a1))
        {
            int dist = inverse ?
                            a2.getBounds().getX1() - a1.getBounds().getX2()
                            : a1.getBounds().getX1() - a2.getBounds().getX2();
            if (dist >= 0)
            {
                int tw = topology.getTopologyPosition().getWidth();
                float w = 1.0f - (dist / 3) * 3.0f / tw;
                /*if (a1.getId() == 40)
                    System.out.println("Found: " + a1 + " " + getName() + " " + a2 + " w=" + w);*/
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
        return DefaultMetrics.heightMetrics;
    }
    
}

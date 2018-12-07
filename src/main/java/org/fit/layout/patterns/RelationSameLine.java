/**
 * RelationSameLine.java
 *
 * Created on 29. 4. 2016, 11:45:19 by burgetr
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
public class RelationSameLine extends LineRelation implements SimpleRelation
{

    public RelationSameLine()
    {
        super("sameLine");
    }
    
    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology, Collection<Area> areas)
    {
        //here a1 is the right area, a2 is the left area
        //we say that a1 is after a2
        if (AreaUtils.isOnSameLine(a2, a1))
        {
            float dist = Math.max(a2.getBounds().getX1() - a1.getBounds().getX1(),
                            a1.getBounds().getX1() - a2.getBounds().getX1());
            if (dist >= 0)
            {
                int tw = a1.getPage().getWidth();
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
        return DefaultMetrics.heightMetrics;
    }
    
}

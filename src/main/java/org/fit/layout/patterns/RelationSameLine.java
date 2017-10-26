/**
 * RelationSameLine.java
 *
 * Created on 29. 4. 2016, 11:45:19 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;

/**
 * 
 * @author burgetr
 */
public class RelationSameLine extends LineRelation
{

    public RelationSameLine()
    {
        super("sameLine");
    }
    
    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology)
    {
        //here a1 is the right area, a2 is the left area
        //we say that a1 is after a2
        if (isOnSameLine(a2, a1))
        {
            float dist = Math.max(a2.getBounds().getX1() - a1.getBounds().getX2(),
                            a1.getBounds().getX1() - a2.getBounds().getX2());
            if (dist >= 0)
            {
                //int tw = topology.getTopologyPosition().getWidth();
                //float w = 1.0f - dist / tw;
                return 0.6f;
            }
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

}

/**
 * SideRelationship.java
 *
 * Created on 28. 2. 2016, 18:02:49 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;

/**
 * 
 * @author burgetr
 */
public class RelationSide extends Relation
{

    public RelationSide(AreaTopology topology)
    {
        super("side", topology);
    }

    @Override
    public float isInRelationship(Area a1, Area a2)
    {
        //here a1 is the right area, a2 is the left area
        //we say that a1 is on side of a2
        if (AreaUtils.isOnSameLineRoughly(a2, a1))
        {
            float dist = a1.getBounds().getX1() - a2.getBounds().getX2();
            float em = Math.max(a2.getFontSize(), a1.getFontSize());
            if (dist > -0.2*em && dist < 0.9*em)
                return 1.0f;
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

    
}

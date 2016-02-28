/**
 * RelationBelow.java
 *
 * Created on 29. 2. 2016, 0:00:11 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
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
    public float isInRelationship(Area a1, Area a2)
    {
        //here a1 is the bootom area, a2 is the top area
        //we say that a1 is below a2
        if (a1.getParentArea() != null && a1.getParentArea() == a2.getParentArea())
        {
            final Rectangular gp1 = a1.getTopology().getPosition();
            final Rectangular gp2 = a2.getTopology().getPosition();
            if (gp1.getX1() == gp2.getX1())
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
        else
            return 0.0f;
    }

}

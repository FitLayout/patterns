/**
 * RelationAfter.java
 *
 * Created on 15. 3. 2016, 10:41:43 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class RelationAfter extends Relation
{

    public RelationAfter()
    {
        super("after");
    }

    @Override
    public float isInRelationship(Area a1, Area a2, AreaTopology topology)
    {
        //here a1 is the right area, a2 is the left area
        //we say that a1 is after a2
        if (isOnSameLine(a2, a1))
        {
            float dist = a1.getBounds().getX1() - a2.getBounds().getX2();
            if (dist >= 0)
            {
                float w = 1.0f - dist / topology.getTopologyWidth();
                //if (a2.getId() == 395)
                //    System.out.println("Found: " + a1 + " after + " + a2 + " w=" + w);
                return w;
            }
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

    private boolean isOnSameLine(Area a1, Area a2)
    {
        Rectangular r1 = a1.getBounds();
        Rectangular r2 = a2.getBounds();
        return (r2.getY1() < r1.midY() && r2.getY2() > r1.midY());
    }
    
}

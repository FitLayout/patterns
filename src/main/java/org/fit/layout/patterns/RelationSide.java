/**
 * SideRelationship.java
 *
 * Created on 28. 2. 2016, 18:02:49 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class RelationSide extends Relation
{

    public RelationSide()
    {
        super("side");
    }

    @Override
    public float isInRelationship(Area a1, Area a2)
    {
        if (a1.getParentArea() != null && a1.getParentArea() == a2.getParentArea())
        {
            final Area parent = a1.getParentArea();
            Area cand = findClosestOnLeft(parent, a2);
            if (cand == a1)
                return 1.0f;
            else
                return 0.0f;
        }
        else
            return 0.0f;
    }

    protected Area findClosestOnLeft(Area parent, Area refArea)
    {
        final Rectangular gp = refArea.getTopology().getPosition();
        final Rectangular leftArea = new Rectangular(0, gp.getY1(), gp.getX1() - 1, gp.getY2());
        return findClosestInRegion(parent, refArea, leftArea);
    }
    
    protected Area findClosestInRegion(Area parent, Area refArea, Rectangular r)
    {
        Area ret = null;
        int minDist = Integer.MAX_VALUE;
        
        for (int i = 0; i < parent.getChildCount(); i++)
        {
            Area n = parent.getChildArea(i);
            if (n.getTopology().getPosition().intersects(r))
            {
                int dist = distance(n, refArea);
                if (dist < minDist)
                {
                    ret = n;
                    minDist = dist;
                }
            }
        }
        return ret;
    }

    protected int distance(Area a1, Area a2)
    {
        //only the horizontal distance is interesting for this relationship
        return a2.getBounds().getX1() - a1.getBounds().getX2();
    }
    
}

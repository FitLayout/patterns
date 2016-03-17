/**
 * LineRelation.java
 *
 * Created on 17. 3. 2016, 13:07:22 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.Rectangular;

/**
 * A common parent class for the relations operating on a single line (before/after) 
 * @author burgetr
 */
public abstract class LineRelation extends Relation
{

    public LineRelation(String name)
    {
        super(name);
    }

    protected boolean isOnSameLine(Area a1, Area a2)
    {
        Rectangular r1 = a1.getBounds();
        Rectangular r2 = a2.getBounds();
        return (r2.getY1() < r1.midY() && r2.getY2() > r1.midY());
    }
    
}

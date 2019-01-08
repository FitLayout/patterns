/**
 * SimpleGridRelation.java
 *
 * Created on 8. 1. 2019, 14:18:04 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;

/**
 * A relation that may be discovered by analyzing any pair of areas and providing the grid coordinates.
 * 
 * @author burgetr
 */
public interface SimpleGridRelation extends Relation
{
    
    /**
     * Decides whether the area a1 is in the given relationship with a2.
     * @param a1 the first area
     * @param r1 the coordinates of a1 within the topology
     * @param a2 the second area
     * @param r2 the coordinates of a2 within the topology
     * @param topology The area topology to be used for comparing the area positions
     * @param areas A collection of all the sibling areas, both {@code a1} and {@code a2} must be contained in {@code areas}. This may be used
     * e.g. for deciding whether an area is alone on the line etc.
     * @return A positive value when {@code a1} is in the relationship with {@code a2}. Otherwise zero is returned.
     */
    public abstract float isInRelationship(Area a1, Rectangular r1, Area a2, Rectangular r2, AreaTopology topology, Collection<Area> areas);

}

/**
 * RelationSimple.java
 *
 * Created on 15. 2. 2018, 15:01:28 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;

/**
 * A relation that may be discovered by analyzing any pair of areas.
 * 
 * @author burgetr
 */
public interface SimpleRelation extends Relation
{

    /**
     * Decides whether the area a1 is in the given relationship with a2.
     * @param a1 the first area
     * @param a2 the second area
     * @param topology The area topology to be used for comparing the area positions
     * @param areas A collection of all the sibling areas, both {@code a1} and {@code a2} must be contained in {@code areas}. This may be used
     * e.g. for deciding whether an area is alone on the line etc.
     * @return A positive value when {@code a1} is in the relationship with {@code a2}. Otherwise zero is returned.
     */
    public abstract float isInRelationship(Area a1, Area a2, AreaTopology topology, Collection<Area> areas);

    
}

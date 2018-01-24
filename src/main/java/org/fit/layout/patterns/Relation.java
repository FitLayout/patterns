/**
 * Relationship.java
 *
 * Created on 28. 2. 2016, 17:57:28 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public abstract class Relation
{
    private String name;

    public Relation(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    
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

    /**
     * The set of metrics that should be used for evaluating this relation.
     * @return the set of metrics
     */
    public Set<Metric> metrics()
    {
        //TODO testing only; move this to the particular relations
        return Collections.unmodifiableSet(Stream.of(
                Metric.widthLL,
                Metric.widthRL,
                Metric.heightBB
                ).collect(Collectors.toSet()));
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Relation other = (Relation) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "rel:" + name;
    }
    
}

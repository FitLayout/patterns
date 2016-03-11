/**
 * Relationship.java
 *
 * Created on 28. 2. 2016, 17:57:28 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;

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
     * @param a1
     * @param a2
     * @return A positive value when a1 is in relationship with a2. Otherwise zero is returned.
     */
    public abstract float isInRelationship(Area a1, Area a2, AreaTopology topology);

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

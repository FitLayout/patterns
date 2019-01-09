/**
 * Relationship.java
 *
 * Created on 28. 2. 2016, 17:57:28 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Set;

import org.fit.layout.patterns.model.Metric;

/**
 * A common base implementation of all the relations.
 *  
 * @author burgetr
 */
public class BasicRelation implements Relation
{
    private String name;
    private Set<Metric> usedMetrics;
    

    public BasicRelation(String name, Set<Metric> usedMetrics)
    {
        this.name = name;
        this.usedMetrics = usedMetrics;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Set<Metric> metrics()
    {
        return usedMetrics;
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
        BasicRelation other = (BasicRelation) obj;
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
        return name;
    }

}

/**
 * Relationship.java
 *
 * Created on 28. 2. 2016, 17:57:28 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Area;

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
    
    public abstract float isInRelationship(Area a1, Area a2);

}

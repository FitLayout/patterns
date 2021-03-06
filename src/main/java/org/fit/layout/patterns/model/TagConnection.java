/**
 * TagConnection.java
 *
 * Created on 28. 2. 2016, 19:48:30 by burgetr
 */
package org.fit.layout.patterns.model;

import org.fit.layout.model.Tag;
import org.fit.layout.patterns.Relation;

/**
 * 
 * @author burgetr
 */
public class TagConnection extends Connection<Tag>
{

    public TagConnection(Tag a1, Tag a2, Relation relation, float weight)
    {
        super(a1, a2, relation, weight);
    }

    @Override
    public TagPair toPair()
    {
        return new TagPair(getA1(), getA2());
    }

    @Override
    public String toString()
    {
        return getA1().toString() + "-" + getRelation().getName() + "-" + getA2().toString(); 
    }
    
}

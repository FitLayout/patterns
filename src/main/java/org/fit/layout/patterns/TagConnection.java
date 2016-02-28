/**
 * TagConnection.java
 *
 * Created on 28. 2. 2016, 19:48:30 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Tag;

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

}

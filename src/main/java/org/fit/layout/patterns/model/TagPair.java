/**
 * TagPair.java
 *
 * Created on 28. 6. 2017, 10:11:55 by burgetr
 */
package org.fit.layout.patterns.model;

import org.fit.layout.model.Tag;

/**
 * A generic pair of tags.
 * 
 * @author burgetr
 */
public class TagPair extends Pair<Tag>
{

    public TagPair(Tag o1, Tag o2)
    {
        super(o1, o2);
    }

    public TagPair reverse()
    {
        return new TagPair(getO2(), getO1());
    }
    
}

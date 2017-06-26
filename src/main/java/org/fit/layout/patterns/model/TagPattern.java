/**
 * TagPattern.java
 *
 * Created on 26. 6. 2017, 13:34:04 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.fit.layout.model.Tag;

/**
 * A configuration of tag connections that represents a possible tag extraction pattern.
 * This is a specific case of a list of tag connections
 * @author burgetr
 */
public class TagPattern extends ArrayList<TagConnection>
{
    private static final long serialVersionUID = 1L;
    
    private Set<Tag> tags;
    
    /**
     * Creates a new tag pattern with the given expected size (number of tag connections).
     * @param size the expected number of connections
     */
    public TagPattern(int size)
    {
        super(size);
        tags = new HashSet<>(size + 1);
    }
    
    public TagPattern(TagPattern src)
    {
        super(src);
        tags = new HashSet<>(src.tags);
    }
    
    public Set<Tag> getTags()
    {
        return tags;
    }

    @Override
    public boolean add(TagConnection e)
    {
        tags.add(e.getA1());
        tags.add(e.getA2());
        return super.add(e);
    }
    
    public boolean mayAdd(TagConnection e)
    {
        return tags.contains(e.getA1()) !=  tags.contains(e.getA2());
    }
    

}

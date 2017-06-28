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
 * This is a basically a list of tag connections. The pattern should be connected - i.e. the tags
 * and their connections should create a connected graph.
 * @author burgetr
 */
public class TagPattern extends ArrayList<TagPair>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The tags covered by this pattern.
     */
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
    
    /**
     * Obtains all tags covered by this pattern.
     * @return a set of tags used in the connections
     */
    public Set<Tag> getTags()
    {
        return tags;
    }

    /**
     * Adds a new tag pair to the pattern. No checking is performed. If the pattern should
     * remain connected, the {@link TagPattern#mayAdd(TagConnection)} function should be used
     * for checking.
     */
    @Override
    public boolean add(TagPair e)
    {
        tags.add(e.getO1());
        tags.add(e.getO2());
        return super.add(e);
    }
    
    /**
     * Checks whether the tag pattern remains connected when a new tag pair is added. 
     * @param e the tag pair to test
     * @return
     */
    public boolean mayAdd(TagPair e)
    {
        return tags.contains(e.getO1()) !=  tags.contains(e.getO2());
    }

}

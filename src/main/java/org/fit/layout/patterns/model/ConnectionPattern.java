/**
 * ConnectionPattern.java
 *
 * Created on 29. 6. 2017, 14:29:25 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fit.layout.model.Tag;

/**
 * A configuration of tag connections that represents a possible tag extraction pattern.
 * This is a basically a set of tag connections. This is a special instance of a tag
 * pattern with particular relations added to the individual tag pairs.
 * @author burgetr
 */
public class ConnectionPattern extends LinkedHashSet<TagConnection>
{
    private static final long serialVersionUID = 1L;

    
    public ConnectionPattern(int size)
    {
        super(size);
    }
    
    public Set<Tag> getTags()
    {
        Set<Tag> ret = new HashSet<>();
        for (TagConnection con : this)
        {
            ret.add(con.getA1());
            ret.add(con.getA2());
        }
        return ret;
    }
    
    public Set<TagPair> getPairs()
    {
        Set<TagPair> ret = new HashSet<>();
        for (TagConnection con : this)
            ret.add(con.toPair());
        return ret;
    }
    
    public TagConnection findForPair(TagPair pair)
    {
        for (TagConnection con : this)
        {
            if (con.getA1().equals(pair.getO1()) && con.getA2().equals(pair.o2))
                return con;
        }
        return null;
    }
    
}

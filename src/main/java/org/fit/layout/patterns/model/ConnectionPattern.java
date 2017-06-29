/**
 * ConnectionPattern.java
 *
 * Created on 29. 6. 2017, 14:29:25 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.HashSet;

/**
 * A configuration of tag connections that represents a possible tag extraction pattern.
 * This is a basically a set of tag connections. This is a special instance of a tag
 * pattern with particular relations added to the individual tag pairs.
 * @author burgetr
 */
public class ConnectionPattern extends HashSet<TagConnection>
{
    private static final long serialVersionUID = 1L;

    
    public ConnectionPattern(int size)
    {
        super(size);
    }
    
}

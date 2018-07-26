/**
 * Matcher.java
 *
 * Created on 15. 11. 2017, 20:07:33 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.Match;

/**
 * A generic matcher interface.
 * 
 * @author burgetr
 */
public interface Matcher
{
    
    public int getUseStyleWildcards();

    public void setUseStyleWildcards(int useStyleWildcards);

    /**
     * Applies the matcher on a list of areas and returns the matches.
     * @param areas The list of areas to be matched.
     * @return A list of discovered matches (data records) that assign the areas to the individual tags. 
     */
    public Collection<Match> match(Area root);

    /**
     * Checks if the tag corresponds to an extracted attribute with the cardinality M (many).
     * @param tag the tag to check
     * @return {@code true} when the tag corresponds to a M attribute
     */
    public boolean isTagMany(Tag tag);
    
}

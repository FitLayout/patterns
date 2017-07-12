/**
 * BaseMatcher.java
 *
 * Created on 7. 4. 2017, 11:23:45 by burgetr
 */
package org.fit.layout.patterns;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.patterns.model.Match;

/**
 * A base class for matcher implementations.
 * 
 * @author burgetr
 */
public abstract class BaseMatcher
{
    private int useStyleWildcards;
    
    public BaseMatcher()
    {
        useStyleWildcards = 1;
    }

    public int getUseStyleWildcards()
    {
        return useStyleWildcards;
    }

    public void setUseStyleWildcards(int useStyleWildcards)
    {
        this.useStyleWildcards = useStyleWildcards;
    }

    //=====================================================================
    
    /**
     * Applies the matcher on a list of areas and returns the matches.
     * @param areas The list of areas to be matched.
     * @return A list of discovered matches (data records) that assign the areas to the individual tags. 
     */
    public abstract List<Match> match(List<Area> areas);
    
}

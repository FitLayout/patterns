/**
 * BaseMatcher.java
 *
 * Created on 7. 4. 2017, 11:23:45 by burgetr
 */
package org.fit.layout.patterns;

import java.util.List;

import org.fit.layout.model.Area;

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
    
    public abstract List<List<Area>> match(List<Area> areas);
    
}

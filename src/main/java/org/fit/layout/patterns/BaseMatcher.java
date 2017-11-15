/**
 * BaseMatcher.java
 *
 * Created on 7. 4. 2017, 11:23:45 by burgetr
 */
package org.fit.layout.patterns;

/**
 * A base class for matcher implementations.
 * 
 * @author burgetr
 */
public abstract class BaseMatcher implements Matcher
{
    private int useStyleWildcards;
    
    public BaseMatcher()
    {
        useStyleWildcards = 1;
    }

    @Override
    public int getUseStyleWildcards()
    {
        return useStyleWildcards;
    }

    @Override
    public void setUseStyleWildcards(int useStyleWildcards)
    {
        this.useStyleWildcards = useStyleWildcards;
    }

}

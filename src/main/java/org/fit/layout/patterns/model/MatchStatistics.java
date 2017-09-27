/**
 * MatchStatistics.java
 *
 * Created on 27. 9. 2017, 16:19:34 by burgetr
 */
package org.fit.layout.patterns.model;

/**
 * Global statistics about a set od match results.
 * 
 * @author burgetr
 */
public class MatchStatistics
{
    private int maxMatches = 0;
    private int maxAreas = 0;
    
    public int getMaxMatches()
    {
        return maxMatches;
    }
    
    public void setMaxMatches(int maxMatches)
    {
        this.maxMatches = maxMatches;
    }
    
    public int getMaxAreas()
    {
        return maxAreas;
    }
    
    public void setMaxAreas(int maxAreas)
    {
        this.maxAreas = maxAreas;
    }
    
}

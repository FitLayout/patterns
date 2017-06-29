/**
 * MatcherConfiguration.java
 *
 * Created on 29. 6. 2017, 15:20:36 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.Map;

import org.fit.layout.model.Tag;

/**
 * A matcher configuration that involves a tag connection pattern and the styles
 * assigned to the individual tags.
 * 
 * @author burgetr
 */
public class MatcherConfiguration
{
    private Map<Tag, AreaStyle> styleMap;
    private ConnectionPattern pattern;
    private int coverage;
    
    public MatcherConfiguration(Map<Tag, AreaStyle> styleMap, ConnectionPattern pattern, int coverage)
    {
        this.styleMap = styleMap;
        this.pattern = pattern;
        this.coverage = coverage;
    }

    public Map<Tag, AreaStyle> getStyleMap()
    {
        return styleMap;
    }

    public ConnectionPattern getPattern()
    {
        return pattern;
    }

    public int getCoverage()
    {
        return coverage;
    }
    
    public void setCoverage(int coverage)
    {
        this.coverage = coverage;
    }

    @Override
    public String toString()
    {
        return getPattern() + " " + getStyleMap() + " (" + getCoverage() + " matches)";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        result = prime * result + ((styleMap == null) ? 0 : styleMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MatcherConfiguration other = (MatcherConfiguration) obj;
        if (pattern == null)
        {
            if (other.pattern != null) return false;
        }
        else if (!pattern.equals(other.pattern)) return false;
        if (styleMap == null)
        {
            if (other.styleMap != null) return false;
        }
        else if (!styleMap.equals(other.styleMap)) return false;
        return true;
    }
    
    
}

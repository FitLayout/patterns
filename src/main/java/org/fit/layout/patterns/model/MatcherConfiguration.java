/**
 * MatcherConfiguration.java
 *
 * Created on 29. 6. 2017, 15:20:36 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.Map;
import java.util.Set;

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
    private Set<Tag> tags;
    private MatchResult result;
    
    public MatcherConfiguration(Map<Tag, AreaStyle> styleMap, ConnectionPattern pattern, MatchResult result)
    {
        this.styleMap = styleMap;
        this.pattern = pattern;
        this.result = result;
        this.tags = pattern.getTags();
    }

    public Map<Tag, AreaStyle> getStyleMap()
    {
        return styleMap;
    }

    public ConnectionPattern getPattern()
    {
        return pattern;
    }

    public MatchResult getResult()
    {
        return result;
    }

    public void setResult(MatchResult result)
    {
        this.result = result;
    }

    public Set<Tag> getTags()
    {
        return tags;
    }

    @Override
    public String toString()
    {
        String rs = (getResult() == null) ? "not checked" : getResult().toString(); 
        return getPattern() + " " + getStyleMap() + " (" + rs + ")";
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

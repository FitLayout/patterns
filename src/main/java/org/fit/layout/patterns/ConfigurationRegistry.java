/**
 * ConfigurationRegistry.java
 *
 * Created on 10. 1. 2019, 16:22:43 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.patterns.model.ConnectionPattern;
import org.fit.layout.patterns.model.MatcherConfiguration;

/**
 * A registry of configurations used for discovering equivalent configurations based on the
 * relation properties (inverse relations and symmetric relations).
 * 
 * @author burgetr
 */
public class ConfigurationRegistry
{
    private List<MatcherConfiguration> known;
    
    public ConfigurationRegistry()
    {
        known = new ArrayList<>();
    }

    public void add(MatcherConfiguration conf)
    {
        known.add(conf);
    }
    
    public boolean isKnown(MatcherConfiguration conf)
    {
        for (MatcherConfiguration c : known)
        {
            if (isEquivalent(c, conf))
                return true;
        }
        return false;
    }
    
    //================================================================================
    
    private boolean isEquivalent(MatcherConfiguration c1, MatcherConfiguration c2)
    {
        return c1.getStyleMap().equals(c2.getStyleMap())
                && isPatternEquivalent(c1.getPattern(), c2.getPattern());
    }
    
    public boolean isPatternEquivalent(ConnectionPattern p1, ConnectionPattern p2)
    {
        return false; //TODO
    }
    
}

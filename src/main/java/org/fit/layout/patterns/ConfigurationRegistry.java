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
import org.fit.layout.patterns.model.TagConnection;

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
    
    public MatcherConfiguration findEquivalent(MatcherConfiguration conf)
    {
        for (MatcherConfiguration c : known)
        {
            if (isEquivalent(c, conf))
                return c;
        }
        return null;
    }
    
    //================================================================================
    
    private boolean isEquivalent(MatcherConfiguration c1, MatcherConfiguration c2)
    {
        return c1.getStyleMap().equals(c2.getStyleMap())
                && isPatternEquivalent(c1.getPattern(), c2.getPattern());
    }
    
    private boolean isPatternEquivalent(ConnectionPattern p1, ConnectionPattern p2)
    {
        for (TagConnection tc1 : p1)
        {
            //find the corresponding tag pair in p2
            TagConnection found = null;
            for (TagConnection tc2 : p2)
            {
                if (isConnectionEquivalent(tc1, tc2))
                {
                    found = tc2;
                    break;
                }
            }
            if (found == null)
                return false;
        }
        //all corresponding tag pairs found
        return true;
    }
    
    private boolean isConnectionEquivalent(TagConnection tc1, TagConnection tc2)
    {
        return
                (tc2.getA1().equals(tc1.getA1()) && tc2.getA2().equals(tc1.getA2()) //equal tags
                        && tc2.getRelation().equals(tc1.getRelation()))             //  and the equal relation
                || 
                (tc2.getA1().equals(tc1.getA2()) && tc2.getA2().equals(tc1.getA1()) //reverse tags
                        && ((tc2.getRelation().equals(tc1.getRelation()) && tc1.getRelation().isSymmetric()) //the same symmetric relation
                                || (tc2.getRelation().getInverse() != null                                   //inverse relation
                                        && tc2.getRelation().getInverse().equals(tc1.getRelation()))));
    }
    
}

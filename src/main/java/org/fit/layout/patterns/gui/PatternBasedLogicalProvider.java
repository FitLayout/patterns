/**
 * PatternBasedLogicalProvider.java
 *
 * Created on 17. 5. 2017, 23:43:31 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.patterns.AttributeGroupMatcher;

/**
 * A logical provider that is based on the pattern discovery.
 * 
 * @author burgetr
 */
public interface PatternBasedLogicalProvider
{

    /**
     * Creates the matcher and performs its autoconfiguration on a given list of areas.
     * @param areas The list of areas used for configuration.
     */
    public void configureMatcher(List<Area> areas);
    
    /**
     * Obtains the matcher used by the logical provider.
     * @return the matcher
     */
    public AttributeGroupMatcher getMatcher();
    
}

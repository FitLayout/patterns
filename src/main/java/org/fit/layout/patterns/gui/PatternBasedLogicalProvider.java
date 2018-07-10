/**
 * PatternBasedLogicalProvider.java
 *
 * Created on 17. 5. 2017, 23:43:31 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.util.List;

import org.fit.layout.patterns.AttributeGroupMatcher;
import org.fit.layout.patterns.PresentationBasedChunksSource;

/**
 * A logical provider that is based on the pattern discovery.
 * 
 * @author burgetr
 */
public interface PatternBasedLogicalProvider
{

    /**
     * Creates the matcher and performs its autoconfiguration on a given list of areas.
     * @param matcher The matcher to be trained. It must be one of the matchers obtained by {@link #getMatchers()}.
     * @param source The used area source
     */
    public void configureMatcher(AttributeGroupMatcher matcher, PresentationBasedChunksSource source);
    
    /**
     * Obtains the matchers used by the logical provider.
     * @return the list of matchers
     */
    public List<AttributeGroupMatcher> getMatchers();
    
}

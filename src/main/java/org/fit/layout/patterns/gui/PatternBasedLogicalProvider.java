/**
 * PatternBasedLogicalProvider.java
 *
 * Created on 17. 5. 2017, 23:43:31 by burgetr
 */
package org.fit.layout.patterns.gui;

import org.fit.layout.patterns.AttributeGroupMatcher;

/**
 * A logical provider that is based on the pattern discovery.
 * 
 * @author burgetr
 */
public interface PatternBasedLogicalProvider
{

    /**
     * Obtains the matcher used by the logical provider.
     * @return the matcher
     */
    public AttributeGroupMatcher getMatcher();
    
}

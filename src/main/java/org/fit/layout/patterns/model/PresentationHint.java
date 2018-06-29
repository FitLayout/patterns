/**
 * PresentationHint.java
 *
 * Created on 29. 6. 2018, 15:33:48 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;

import org.fit.layout.model.Area;

/**
 * 
 * @author burgetr
 */
public interface PresentationHint
{
    
    /**
     * Applies the hint to the current list of areas.
     * @param src
     * @return
     */
    public List<Area> apply(List<Area> src);

}

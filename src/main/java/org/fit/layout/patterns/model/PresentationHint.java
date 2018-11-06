/**
 * PresentationHint.java
 *
 * Created on 29. 6. 2018, 15:33:48 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Box;

/**
 * 
 * @author burgetr
 */
public interface PresentationHint
{
    
    /**
     * Extracts boxes from the given area and/or modifies the already existing list of boxes (preprocessing).
     * If some additional areas were processed, they should be added to the 'processed' set.
     * @param a
     * @param current
     * @param processed
     * @return
     */
    public List<Box> extractBoxes(Area a, List<Box> current, Set<Area> processed);
    
    /**
     * Applies the hint to the current list of chunks (postprocessing)
     * @param src
     * @return
     */
    public List<Area> apply(List<Area> src);

}

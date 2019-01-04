/**
 * PresentationHint.java
 *
 * Created on 29. 6. 2018, 15:33:48 by burgetr
 */
package org.fit.layout.patterns.chunks;

import java.util.List;
import java.util.Set;

import org.fit.layout.classify.TagOccurrence;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;

/**
 * A hint that influences the chunk extraction in different points of the chunk extraction phases
 * as defined by the {@link PresentationBasedChunksSource}.
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
     * Applies the hint to a list of occurences extracted from the given box text (postprocessing a list of
     * occurences)
     * @param boxText the source box text
     * @param occurrences the current list of occurrences
     * @return the new list of occurrences
     */
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences);
    
    /**
     * Applies the hint to the list of chunks for the given area (postprocessing a list for a given area)
     * @param src
     * @param chunks
     * @return
     */
    public List<Area> processChunks(Area src, List<Area> chunks);
    
    /**
     * Applies the hint to the current list of chunks (postprocessing the whole list)
     * @param chunks the list of chunks (typically for a given tag)
     * @return
     */
    public List<Area> postprocessChunks(List<Area> chunks);

}

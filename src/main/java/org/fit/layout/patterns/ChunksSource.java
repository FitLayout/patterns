/**
 * AreaListSource.java
 *
 * Created on 9. 3. 2018, 23:27:32 by burgetr
 */
package org.fit.layout.patterns;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.PresentationHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base of an area list source for pattern matching.
 * 
 * @author burgetr
 */
public abstract class ChunksSource
{
    private static Logger log = LoggerFactory.getLogger(ChunksSource.class);
    
    private Area root;
    
    public ChunksSource(Area root)
    {
        this.root = root;
    }
    
    public Area getRoot()
    {
        return root;
    }
    
    public void addHint(Tag tag, PresentationHint hint)
    {
        log.warn("Using addHint() where hints are not supported");
    }
    
    public abstract List<Area> getAreas();


}

/**
 * AreaListSource.java
 *
 * Created on 9. 3. 2018, 23:27:32 by burgetr
 */
package org.fit.layout.patterns.chunks;

import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.RelationAnalyzer;
import org.fit.layout.patterns.RelationAnalyzerSymmetric;
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
    private RelationAnalyzer pa;
    
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

    /**
     * Obtains the relation analyzer for this source.
     * @return the corresponding relation analyzer
     */
    public RelationAnalyzer getPA()
    {
        if (pa == null)
        {
            //pa = new RelationAnalyzer(getAreas());
            pa = new RelationAnalyzerSymmetric(getAreas());
        }
        return pa;
    }
    
}

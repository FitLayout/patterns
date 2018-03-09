/**
 * AreaListSource.java
 *
 * Created on 9. 3. 2018, 23:27:32 by burgetr
 */
package org.fit.layout.patterns;

import java.util.List;

import org.fit.layout.model.Area;

/**
 * An abstract base of an area list source for pattern matching.
 * 
 * @author burgetr
 */
public abstract class AreaListSource
{

    public abstract List<Area> getAreas();
    
}

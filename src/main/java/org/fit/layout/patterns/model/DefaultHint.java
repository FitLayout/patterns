/**
 * DefaultHint.java
 *
 * Created on 6. 11. 2018, 13:47:56 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Box;

/**
 * A default no-op presentation hint.
 * @author burgetr
 */
public class DefaultHint implements PresentationHint
{

    @Override
    public List<Box> extractBoxes(Area a, List<Box> current, Set<Area> processed)
    {
        //no changes are performed 
        return current;
    }

    @Override
    public List<Area> apply(List<Area> src)
    {
        //no changes are performed
        return src;
    }

}

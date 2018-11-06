/**
 * HintMultiLine.java
 *
 * Created on 6. 11. 2018, 21:31:40 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Box;

/**
 * 
 * @author burgetr
 */
public class HintMultiLine extends DefaultHint
{

    @Override
    public List<Box> extractBoxes(Area a, List<Box> current, Set<Area> processed)
    {
        if (a.getParent() != null)
        {
            // also try to discover subsequent lines
            AreaTopology topology = a.getParent().getTopology();
            
            
            
            
            
            return super.extractBoxes(a, current, processed);
        }
        else
            return current; //no operation
    }

}

/**
 * LogicalTreeBuilder.java
 *
 * Created on 1. 4. 2016, 15:30:47 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.util.List;

import org.fit.layout.impl.DefaultLogicalArea;
import org.fit.layout.impl.DefaultLogicalAreaTree;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.LogicalAreaTree;

/**
 * A builder that creates the logical area tree from the lauout tree using the configured task.
 *  
 * @author burgetr
 */
public class PatternTreeBuilder
{
    private Task task;

    public PatternTreeBuilder(Task task)
    {
        this.task = task;
    }
    
    public LogicalAreaTree buildTree(AreaTree areaTree)
    {
        List<LogicalArea> outAreas = task.match(areaTree.getRoot());
        
        //create the root area
        LogicalArea lroot = new DefaultLogicalArea(areaTree.getRoot());
        for (LogicalArea la : outAreas)
            lroot.appendChild(la);
        DefaultLogicalAreaTree ret = new DefaultLogicalAreaTree(areaTree);
        ret.setRoot(lroot);
        return ret;
    }
}

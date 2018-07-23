/**
 * LeafAreaSource.java
 *
 * Created on 9. 3. 2018, 23:29:35 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;

/**
 * An area list source that simply takes the leaf areas in a tree with a given root area.
 * 
 * @author burgetr
 */
public class LeafAreaSource extends ChunksSource
{
    
    public LeafAreaSource(Area root)
    {
        super(root);
    }

    @Override
    public List<Area> getAreas()
    {
        List<Area> leaves = new ArrayList<Area>();
        findLeaves(getRoot(), leaves);
        return leaves;
    }

    private void findLeaves(Area root, List<Area> dest)
    {
        if (root.isLeaf())
        {
            if (!root.isSeparator())
                dest.add(root);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                findLeaves(root.getChildAt(i), dest);
        }
    }

}

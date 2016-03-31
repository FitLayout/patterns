/**
 * OneToMany.java
 *
 * Created on 24. 3. 2016, 11:37:14 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.impl.DefaultLogicalArea;
import org.fit.layout.model.Area;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.LogicalAreaGroup;
import org.fit.layout.patterns.OneToManyMatcher;

/**
 * 
 * @author burgetr
 */
public class OneToMany extends Task
{
    /**
     * Output 
     * 
     * @author burgetr
     */
    public enum OutputStructure 
    {
        /** The output is represented as a sequence of areas with the given output tag and the subareas
         * with both the source tags. */
        FLAT,
        /** The output is a sequence of areas with the output tag corresponding to the "one" matches
         * with the sub-areas with the "many" matches. */
        STRUCTURED 
    }
    
    private Tag oneTag;
    private Tag manyTag;
    private OutputStructure outstruct;
    
    public OneToMany(Tag oneTag, Tag manyTag, Tag outputTag)
    {
        super(outputTag);
        this.oneTag = oneTag;
        this.manyTag = manyTag;
        this.outstruct = OutputStructure.FLAT;
    }

    
    
    @Override
    public List<LogicalArea> match(Area root)
    {
        //recursively match subtasks
        List<List<LogicalArea>> sublists = new ArrayList<>(getSubtasks().size());
        for (Task task : getSubtasks())
            sublists.add(task.match(root));

        //create source area list
        List<Area> nodes = new ArrayList<Area>();
        findLeaves(root, nodes, getSubtaskTags()); //get leaf nodes and all the tagged nodes
        //replace the detected pairs with logical groups
        for (List<LogicalArea> sublist : sublists)
        {
            for (LogicalArea la : sublist)
            {
                for (Area a : la.getAreas())
                    nodes.remove(a);
                nodes.add(new LogicalAreaGroup(la));
            }
        }

        //find the matches on the current level
        OneToManyMatcher matcher = new OneToManyMatcher(oneTag, manyTag, 0.2f, true);
        List<List<Area>> matches = matcher.match(nodes);
        switch (outstruct)
        {
            case FLAT:
                return createOutputFlat(matches); 
            case STRUCTURED:
                return createOutputStructured(matches);
        }
        return null;
    }
    
    protected List<LogicalArea> createOutputFlat(List<List<Area>> matches)
    {
        List<LogicalArea> timeAreas = new ArrayList<>();
        for (List<Area> match : matches)
        {
            //System.out.println("Match: " + match);
            //create the item's root node
            LogicalArea la = new DefaultLogicalArea();
            StringBuilder text = new StringBuilder();
            la.addArea(match.get(0));
            text.append(extractAreaText(oneTag, match.get(0)));
            text.append(':');
            la.addArea(match.get(1));
            text.append(extractAreaText(manyTag, match.get(1)));
            la.setText(text.toString());
            la.setMainTag(getOutputTag());
            timeAreas.add(la);
            
            //add sub-areas for one and many
            la.appendChild(createLogicalArea(oneTag, match.get(0)));
            la.appendChild(createLogicalArea(manyTag, match.get(1)));
        }
        //System.out.println("Total:"+ matches.size());
        return timeAreas;
        
    }
    
    protected List<LogicalArea> createOutputStructured(List<List<Area>> matches)
    {
        Map<Area, LogicalArea> oneRoots = new HashMap<>();
        for (List<Area> match : matches)
        {
            //System.out.println("DMatch: " + match);
            
            LogicalArea parent = oneRoots.get(match.get(0));
            if (parent == null)
            {
                parent = createLogicalArea(getOutputTag(), match.get(0));
                oneRoots.put(match.get(0), parent);
            }
            
            LogicalArea manyLa = createLogicalArea(manyTag, match.get(1));
            parent.appendChild(manyLa);
        }
        return new ArrayList<LogicalArea>(oneRoots.values());
    }

    /**
     * Creates a logical area for the given layout area or reuses the existing logical area
     * if the layout area was already created from a logical area.
     * @param tag
     * @param src
     * @return
     */
    protected LogicalArea createLogicalArea(Tag tag, Area src)
    {
        if (src instanceof LogicalAreaGroup)
        {
            return ((LogicalAreaGroup) src).getLogicalArea();
        }
        else
        {
            LogicalArea ret = new DefaultLogicalArea();
            ret.addArea(src);
            ret.setMainTag(tag);
            ret.setText(extractAreaText(tag, src));
            return ret;
        }
    }
    
    protected String extractAreaText(Tag tag, Area area)
    {
        return area.getText(" ");
    }
    
    //====================================================================================
    
    private void findLeaves(Area root, List<Area> dest, Collection<Tag> tags)
    {
        boolean tagfound = false;
        if (tags != null)
        {
            Set<Tag> atags = root.getSupportedTags(0.2f);
            atags.retainAll(tags);
            tagfound = !atags.isEmpty();
        }
        if (tagfound || root.isLeaf())
        {
            if (!root.isSeparator())
                dest.add(root);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                findLeaves(root.getChildArea(i), dest, tags);
        }
    }
    
}

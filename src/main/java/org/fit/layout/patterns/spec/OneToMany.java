/**
 * OneToMany.java
 *
 * Created on 24. 3. 2016, 11:37:14 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fit.layout.classify.Tagger;
import org.fit.layout.impl.DefaultLogicalArea;
import org.fit.layout.model.Area;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.OneToManyMatcher;

/**
 * 
 * @author burgetr
 */
public class OneToMany extends Task
{
    private Tagger oneTagger;
    private Tagger manyTagger;
    
    public OneToMany(Tagger oneTagger, Tagger manyTagger, Tag outputTag)
    {
        super(outputTag);
        this.oneTagger = oneTagger;
        this.manyTagger = manyTagger;
    }

    @Override
    public List<LogicalArea> match(Area root)
    {
        List<Area> nodes = new ArrayList<Area>();
        findLeaves(root, null, nodes); //TODO get the tags from subtasks
        
       OneToManyMatcher matcher = new OneToManyMatcher(oneTagger.getTag(), manyTagger.getTag(), 0.2f, true);
        List<List<Area>> matches = matcher.match(nodes);
        List<LogicalArea> timeAreas = new ArrayList<>();
        for (List<Area> match : matches)
        {
            //System.out.println("Match: " + match);
            LogicalArea la = new DefaultLogicalArea();
            StringBuilder text = new StringBuilder();
            
            la.addArea(match.get(0));
            text.append(oneTagger.extract(match.get(0).getText()).get(0));
            text.append(':');
            la.addArea(match.get(1));
            text.append(manyTagger.extract(match.get(1).getText()).get(0));

            la.setText(text.toString());
            la.setMainTag(getOutputTag());
            timeAreas.add(la);
        }
        //System.out.println("Total:"+ matches.size());
        return timeAreas;
    }
    
    //====================================================================================
    
    private void findLeaves(Area root, Tag[] tags, List<Area> dest)
    {
        if (tags != null)
        {
            Set<Tag> tagSet = new HashSet<>(tags.length);
            for (Tag t : tags)
                tagSet.add(t);
            findLeaves(root, dest, tagSet);
        }
        else
            findLeaves(root, dest, null);
    }

    private void findLeaves(Area root, List<Area> dest, Set<Tag> tags)
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

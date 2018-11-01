/**
 * HintWholeLine.java
 *
 * Created on 31. 10. 2018, 11:29:21 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AttributeGroupMatcher;

/**
 * A hint that forces using the whole source box for the corresponding chunk even if only part
 * of the box was detected as the chunk. 
 * @author burgetr
 */
public class HintWholeBox implements PresentationHint
{
    private Tag tag;
    
    public HintWholeBox(Tag tag)
    {
        this.tag = tag;
    }

    @Override
    public List<Area> apply(List<Area> areas)
    {
        Collection<Area> modified = new HashSet<>();
        for (Area a : areas)
        {
            if (a instanceof TextChunkArea && a.hasTag(tag, AttributeGroupMatcher.MIN_TAG_SUPPORT_MATCH))
            {
                TextChunkArea chunk = (TextChunkArea) a;
                String ta = chunk.getText().trim();
                String boxText = chunk.getSourceBox().getOwnText();
                String tb = boxText.trim();
                if (ta.length() != tb.length())
                {
                    chunk.setText(tb);
                    chunk.setBounds(chunk.getSourceBox().getSubstringBounds(0, boxText.length()));
                    chunk.setName(chunk.getName() + "(ext)");
                    modified.add(chunk);
                    System.out.println("ADDED " + chunk);
                }
            }
        }
        //check for overlaps
        Collection<Area> retain = new HashSet<>(); //the chunks to be retained for each overlap
        for (Area mod : modified)
        {
            for (Iterator<Area> it = areas.iterator(); it.hasNext();)
            {
                Area a = it.next();
                if (a != mod
                        && a.hasTag(tag, AttributeGroupMatcher.MIN_TAG_SUPPORT_MATCH)
                        && a.getBounds().intersects(mod.getBounds())
                        && !retain.contains(a))
                {
                    System.out.println("REMOVED " + a + " for overlap with " + mod);
                    retain.add(mod);
                    it.remove();
                }
            }
        }
        return areas;
    }

    @Override
    public String toString()
    {
        return "WholeBox";
    }
    
}

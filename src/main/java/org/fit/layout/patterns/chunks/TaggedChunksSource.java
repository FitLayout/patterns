/**
 * TaggedChunksSource.java
 *
 * Created on 9. 3. 2018, 23:37:25 by burgetr
 */
package org.fit.layout.patterns.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.classify.TagOccurrence;
import org.fit.layout.classify.Tagger;
import org.fit.layout.classify.TextTag;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.gui.PatternsPlugin;

/**
 * An area list source that creates artificial areas by extracting tagged chunks from leaf areas.
 * @author burgetr
 */
public class TaggedChunksSource extends ChunksSource
{
    private List<Area> areas;
    
    public TaggedChunksSource(Area root)
    {
        super(root);
    }

    @Override
    public List<Area> getAreas()
    {
        if (areas == null)
        {
            areas = new ArrayList<Area>();
            recursiveScan(getRoot(), areas);
        }
        return areas;
    }

    private void recursiveScan(Area root, List<Area> dest)
    {
        if (root.isLeaf())
        {
            Set<Tag> supportedTags = root.getSupportedTags(PatternsPlugin.MIN_TAG_SUPPORT);
            if (!supportedTags.isEmpty())
            {
                for (Tag t : supportedTags)
                {
                    if (t instanceof TextTag)
                    {
                        List<Area> newAreas = createAreasFromTag(root, (TextTag) t);
                        //System.out.println(root + " : " + t + " : " + newAreas);
                        for (Area a : newAreas)
                        {
                            dest.add(a);
                        }
                    }
                }
            }
            else
            {
                //no tags, create untagged chunks
                List<Area> newAreas = createUntaggedAreas(root);
                for (Area a : newAreas)
                {
                    dest.add(a);
                }
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveScan(child, dest);
        }
    }

    private List<Area> createAreasFromTag(Area a, TextTag t)
    {
        List<Area> ret = new ArrayList<>();
        Tagger tg = t.getSource();
        for (Box box : a.getBoxes())
        {
            String text = box.getOwnText();
            List<TagOccurrence> occurences = tg.extract(text);
            int last = 0;
            for (TagOccurrence occ : occurences)
            {
                int pos = occ.getPosition();
                if (pos > last) //some substring between, create a chunk with no tag
                {
                    Area sepArea = createSubstringArea(a, box, null, text.substring(last, pos), last);
                    ret.add(sepArea);
                }
                Area newArea = createSubstringArea(a, box, t, occ.getText(), pos);
                ret.add(newArea);
                last = pos + occ.getLength();
            }
            if (text.length() > last)
            {
                Area sepArea = createSubstringArea(a, box, null, text.substring(last), last);
                ret.add(sepArea);
            }
        }
        return ret;
    }

    private List<Area> createUntaggedAreas(Area a)
    {
        List<Area> ret = new ArrayList<>();
        for (Box box : a.getBoxes())
        {
            String text = box.getOwnText();
            if (text != null && text.length() > 0)
            {
                Area sepArea = createSubstringArea(a, box, null, text, 0);
                ret.add(sepArea);
            }
        }
        return ret;
    }
    
    private Area createSubstringArea(Area a, Box box, TextTag tag, String occ, int pos)
    {
        Rectangular r = box.getSubstringBounds(pos, pos + occ.length());
        TextChunkArea newArea = new TextChunkArea(r, a, box);
        newArea.setText(occ);
        if (tag != null)
        {
            newArea.setName("<chunk:" + tag.getValue() + "> " + occ);
            newArea.addTag(tag, a.getTagSupport(tag));
        }
        else
        {
            newArea.setName("<---> " + occ);
        }
        newArea.setPage(a.getPage());
        return newArea;
    }

    
    
}

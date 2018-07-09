/**
 * PresentationBasedChunksSource.java
 *
 * Created on 29. 6. 2018, 15:16:19 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.Tagger;
import org.fit.layout.classify.TextTag;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.gui.PatternsPlugin;
import org.fit.layout.patterns.model.PresentationHint;
import org.fit.layout.patterns.model.TextChunkArea;

/**
 * A chunk source that follows some presentation patterns in order to improve the chunk extraction.
 * 
 * @author burgetr
 */
public class PresentationBasedChunksSource extends AreaListSource
{
    private Area root;

    private Map<Tag, List<Area>> areas;
    private Map<Tag, List<PresentationHint>> hints;
    
    public PresentationBasedChunksSource(Area root)
    {
        this.root = root;
        hints = new HashMap<>();
    }

    @Override
    public List<Area> getAreas()
    {
        if (areas == null)
        {
            areas = new HashMap<>();
            Set<Tag> supportedTags = root.getSupportedTags(PatternsPlugin.MIN_TAG_SUPPORT);
            if (!supportedTags.isEmpty())
            {
                for (Tag t : supportedTags)
                {
                    if (t instanceof TextTag)
                    {
                        List<Area> dest = new ArrayList<>();
                        recursiveScan(root, (TextTag) t, dest);
                    }
                }
            }
        }
        //TODO apply hints on the lists
        return disambiguateAreas(areas);
    }
    
    public void addHint(Tag tag, PresentationHint hint)
    {
        List<PresentationHint> list = hints.get(tag);
        if (list == null)
        {
            list = new ArrayList<>();
            hints.put(tag, list);
        }
        list.add(hint);
    }

    //==============================================================================================
    
    private List<Area> disambiguateAreas(Map<Tag, List<Area>> areas)
    {
        //TODO implement merging the individual lists to a single list of tagged areas
        return null;
    }
    
    //==============================================================================================
    
    private void recursiveScan(Area root, TextTag tag, List<Area> dest)
    {
        if (root.isLeaf())
        {
            List<Area> newAreas = createAreasFromTag(root, tag);
            //System.out.println(root + " : " + t + " : " + newAreas);
            for (Area a : newAreas)
            {
                dest.add(a);
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveScan(child, tag, dest);
        }
    }

    private List<Area> createAreasFromTag(Area a, TextTag t)
    {
        List<Area> ret = new ArrayList<>();
        Tagger tg = t.getSource();
        for (Box box : a.getBoxes())
        {
            String text = box.getOwnText();
            List<String> occurences = tg.extract(text);
            int last = 0;
            for (String occ : occurences)
            {
                int pos = text.indexOf(occ, last);
                if (pos != -1)
                {
                    if (pos > last) //some substring between, create a chunk with no tag
                    {
                        Area sepArea = createSubstringArea(a, box, null, text.substring(last, pos), last);
                        ret.add(sepArea);
                    }
                    Area newArea = createSubstringArea(a, box, t, occ, pos);
                    ret.add(newArea);
                    last = pos + occ.length();
                }
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
        TextChunkArea newArea = new TextChunkArea(r);
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

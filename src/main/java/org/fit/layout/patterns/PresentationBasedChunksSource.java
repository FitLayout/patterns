/**
 * PresentationBasedChunksSource.java
 *
 * Created on 29. 6. 2018, 15:16:19 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.TagOccurrence;
import org.fit.layout.classify.Tagger;
import org.fit.layout.classify.TextTag;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.gui.PatternsPlugin;
import org.fit.layout.patterns.model.BoxText;
import org.fit.layout.patterns.model.PresentationHint;
import org.fit.layout.patterns.model.TextChunkArea;

/**
 * A chunk source that follows some presentation patterns in order to improve the chunk extraction.
 * 
 * @author burgetr
 */
public class PresentationBasedChunksSource extends ChunksSource
{
    private float minTagSupport;
    private List<Area> areas;
    private Map<Tag, List<Area>> tagAreas;
    private Map<Tag, List<PresentationHint>> hints;
    
    public PresentationBasedChunksSource(Area root, float minTagSupport)
    {
        super(root);
        this.minTagSupport = minTagSupport;
        hints = new HashMap<>();
    }
    
    @Override
    public List<Area> getAreas()
    {
        if (areas == null)
        {
            tagAreas = new HashMap<>();
            Set<TextTag> supportedTags = findLeafTags(getRoot());
            if (!supportedTags.isEmpty())
            {
                for (TextTag t : supportedTags)
                {
                    List<Area> dest = new ArrayList<>();
                    Set<Area> processed = new HashSet<>();
                    recursiveScan(getRoot(), (TextTag) t, dest, processed);
                    if (hints.get(t) != null)
                        dest = applyHints(dest, hints.get(t));
                    tagAreas.put(t, dest);
                }
            }
            areas = disambiguateAreas(tagAreas);
        }
        return areas;
    }
    
    @Override
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

    @Override
    public String toString()
    {
        return "ChunkSource" + hints.toString();
    }
    
    //==============================================================================================
    
    private List<Area> disambiguateAreas(Map<Tag, List<Area>> areas)
    {
        //TODO implement merging the individual lists to a single list of tagged areas
        List<Area> ret = new ArrayList<>();
        for (List<Area> sub : areas.values())
            ret.addAll(sub);
        return ret;
    }
    
    private List<Area> applyHints(List<Area> areas, List<PresentationHint> hints)
    {
        List<Area> current = areas;
        for (PresentationHint hint : hints)
            current = hint.apply(current);
        return current;
    }
    
    //==============================================================================================
    
    private void recursiveScan(Area root, TextTag tag, List<Area> dest, Set<Area> processed)
    {
        if (root.isLeaf())
        {
            if (root.hasTag(tag, minTagSupport) && !processed.contains(root))
            {
                List<Area> newAreas = createAreasFromTag(root, tag, processed);
                //System.out.println(root + " : " + t + " : " + newAreas);
                for (Area a : newAreas)
                {
                    dest.add(a);
                }
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveScan(child, tag, dest, processed);
        }
    }

    private List<Area> createAreasFromTag(Area a, TextTag t, Set<Area> processed)
    {
        List<Area> ret = new ArrayList<>();
        Tagger tg = t.getSource();
        List<Box> boxes = extractBoxes(a, t, processed);
        BoxText boxText = new BoxText(boxes);
        List<TagOccurrence> occurences = tg.extract(boxText.getText());
        for (TagOccurrence occ : occurences)
        {
            Area newArea = createSubstringArea(a, t, boxText, occ);
            ret.add(newArea);
        }
        //apply hints on the particular list of chunks
        List<Area> current = ret;
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                current = hint.processChunks(a, current);
        }
        return current;
    }

    private Area createSubstringArea(Area a, TextTag tag, BoxText boxText, TagOccurrence occ)
    {
        //determine the substring bounds
        Rectangular r = boxText.getSubstringBounds(occ.getPosition(), occ.getPosition() + occ.getLength());
        //create the chunk area
        TextChunkArea newArea = new TextChunkArea(r, a, boxText.getBoxForPosition(occ.getPosition()));
        newArea.setText(occ.getText());
        newArea.setName("<chunk:" + tag.getValue() + "> " + occ);
        newArea.addTag(tag, a.getTagSupport(tag));
        newArea.setPage(a.getPage());
        return newArea;
    }
    
    //==============================================================================================
    
    private List<Box> extractBoxes(Area src, Tag t, Set<Area> processed)
    {
        processed.add(src);
        List<Box> current = new ArrayList<>(src.getBoxes());
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                current = hint.extractBoxes(src, current, processed);
        }
        return current;
    }
    
    //==============================================================================================
    
    private Set<TextTag> findLeafTags(Area root)
    {
        Set<TextTag> ret = new HashSet<>();
        recursiveCollectTags(root, ret);
        return ret;
    }

    private void recursiveCollectTags(Area root, Set<TextTag> dest)
    {
        if (root.isLeaf())
        {
            Set<Tag> all = root.getSupportedTags(PatternsPlugin.MIN_TAG_SUPPORT);
            for (Tag t : all)
            {
                if (t instanceof TextTag)
                    dest.add((TextTag) t);
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveCollectTags(child, dest);
        }
    }
    
}

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
import org.fit.layout.impl.AreaListGridTopology;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
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
                    List<Area> destChunks = new ArrayList<>();
                    List<Area> destAll = new ArrayList<>();
                    Set<Area> processed = new HashSet<>();
                    recursiveScan(getRoot(), (TextTag) t, destChunks, destAll, processed);
                    //apply hints on chunks
                    if (hints.get(t) != null)
                        destChunks = applyHints(destChunks, hints.get(t));
                    //store chunks for the tag
                    tagAreas.put(t, destChunks);
                    //create a layer topology for all the created areas
                    AreaTopology layerTopology = new AreaListGridTopology(destAll);
                    for (Area a : destAll)
                        ((TextChunkArea) a).setLayerTopology(layerTopology);
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
        List<Area> all = new ArrayList<>();
        for (List<Area> sub : areas.values())
            all.addAll(sub);
        
        /*AreaTopology t = new AreaListGridTopology(all);
        for (Area a : all)
        {
            Rectangular gp = t.getPosition(a);
            Collection<Area> isec = t.findAllAreasIntersecting(gp);
            for (Area other : isec)
            {
                if (other != a)
                {
                    System.out.println(a + " intersects with " + other);
                }
            }
        }*/
        
        return all;
    }
    
    private List<Area> applyHints(List<Area> areas, List<PresentationHint> hints)
    {
        List<Area> current = areas;
        for (PresentationHint hint : hints)
            current = hint.apply(current);
        return current;
    }
    
    //==============================================================================================
    
    private void recursiveScan(Area root, TextTag tag, List<Area> destChunks, List<Area> destAll, Set<Area> processed)
    {
        if (root.isLeaf())
        {
            if (root.hasTag(tag, minTagSupport) && !processed.contains(root))
            {
                createAreasFromTag(root, tag, destChunks, destAll, processed);
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveScan(child, tag, destChunks, destAll, processed);
        }
    }

    private void createAreasFromTag(Area a, TextTag t, List<Area> destChunks, List<Area> destAll, Set<Area> processed)
    {
        List<Area> chunks = new ArrayList<>();
        List<Area> all = new ArrayList<>();
        Tagger tg = t.getSource();
        List<Box> boxes = extractBoxes(a, t, processed);
        BoxText boxText = new BoxText(boxes);

        List<TagOccurrence> occurrences = tg.extract(boxText.getText());
        //apply hints on the particular list of chunks
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                occurrences = hint.processOccurrences(boxText, occurrences);
        }
        
        int last = 0;
        for (TagOccurrence occ : occurrences)
        {
            int pos = occ.getPosition();
            if (pos > last) //some substring between, create a chunk with no tag
            {
                String substr = boxText.getText().substring(last, pos);
                TagOccurrence between = new TagOccurrence(substr, last, 1);
                Area sepArea = createSubstringArea(a, t, false, boxText, between);
                all.add(sepArea);
            }
            Area newArea = createSubstringArea(a, t, true, boxText, occ);
            chunks.add(newArea);
            all.add(newArea);
            last = pos + occ.getLength();
        }
        if (boxText.length() > last) //there is something remaining after the last occurrence
        {
            String substr = boxText.getText().substring(last);
            TagOccurrence between = new TagOccurrence(substr, last, 1);
            Area sepArea = createSubstringArea(a, t, false, boxText, between);
            all.add(sepArea);
        }
        //apply hints on the particular list of chunks
        List<Area> current = chunks;
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                current = hint.processChunks(a, current);
        }
        destChunks.addAll(chunks);
        destAll.addAll(all);
    }

    private Area createSubstringArea(Area a, TextTag tag, boolean present, BoxText boxText, TagOccurrence occ)
    {
        //determine the substring bounds
        Rectangular r = boxText.getSubstringBounds(occ.getPosition(), occ.getPosition() + occ.getLength());
        //create the chunk area
        TextChunkArea newArea = new TextChunkArea(r, a, boxText.getBoxForPosition(occ.getPosition()));
        newArea.setText(occ.getText());
        if (present)
        {
            newArea.setName("<chunk:" + tag.getValue() + "> " + occ);
            newArea.addTag(tag, a.getTagSupport(tag));
        }
        else
        {
            newArea.setName("<chunk:!" + tag.getValue() + "> " + occ);
        }
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

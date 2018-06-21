/**
 * TaggedChunksSource.java
 *
 * Created on 9. 3. 2018, 23:37:25 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.classify.Tagger;
import org.fit.layout.classify.TextTag;
import org.fit.layout.impl.DefaultArea;
import org.fit.layout.model.Area;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.gui.PatternsPlugin;

/**
 * An area list source that creates artificial areas by extracting tagged chunks from leaf areas.
 * @author burgetr
 */
public class TaggedChunksSource extends AreaListSource
{
    private Area root;
    
    public TaggedChunksSource(Area root)
    {
        this.root = root;
    }

    @Override
    public List<Area> getAreas()
    {
        List<Area> ret = new ArrayList<Area>();
        recursiveScan(root, ret);
        return ret;
    }

    private void recursiveScan(Area root, List<Area> dest)
    {
        if (root.isLeaf())
        {
            for (Tag t : root.getSupportedTags(PatternsPlugin.MIN_TAG_SUPPORT))
            {
                if (t instanceof TextTag)
                {
                    List<Area> newAreas = createAreasFromTag(root, (TextTag) t);
                    System.out.println(root + " : " + t + " : " + newAreas);
                    for (Area a : newAreas)
                    {
                        dest.add(a);
                    }
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
            List<String> occurences = tg.extract(text);
            int last = 0;
            for (String occ : occurences)
            {
                int pos = text.indexOf(occ, last);
                if (pos != -1)
                {
                    Rectangular r = box.getSubstringBounds(pos, pos + occ.length());
                    DefaultArea newArea = new DefaultArea(r);
                    newArea.setName("<chunk:" + t.getValue() + "> " + occ);
                    newArea.addTag(t, a.getTagSupport(t));
                    newArea.setPage(a.getPage());

                    ret.add(newArea);
                    last = pos + occ.length();
                }
            }
        }
        return ret;
    }

}

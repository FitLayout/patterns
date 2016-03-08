/**
 * OneToManyMatcher.java
 *
 * Created on 28. 2. 2016, 17:07:22 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.classify.NodeStyle;
import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public class OneToManyMatcher
{
    private static Logger log = LoggerFactory.getLogger(OneToManyMatcher.class);

    private Tag[] srcTag;
    private boolean fixedOrder;
    
    private List<Area> areas;
    
    private List<StyleCounter<NodeStyle>> styleStats; //style statistics
    private List<List<NodeStyle>> styles;  //the most frequent styles for each tag
    private PatternAnalyzer pa;
    private PatternCounter<TagConnection> pc;
    
    
    public OneToManyMatcher(Tag srcTag1, Tag srcTag2, boolean fixedOrder)
    {
        srcTag = new Tag[2];
        srcTag[0] = srcTag1;
        srcTag[1] = srcTag2;
        this.fixedOrder = fixedOrder;
    }
    
    public void match(List<Area> areas)
    {
        this.areas = areas;
        gatherStatistics();
        
        log.debug("Statistics:");
        for (int i = 0; i < srcTag.length; i++)
        {
            log.debug("Styles {}: {}", srcTag[i], styleStats.get(i));
            log.debug("     used: {}", styles.get(i));
        }
        log.debug("Relations: {}", pc);
        
    }
    
    //===========================================================================================
    
    private boolean matchesAnyStyle(Area a, List<NodeStyle> styles)
    {
        final NodeStyle cstyle = new NodeStyle(a);
        return styles.contains(cstyle);
    }
    
    private void gatherStatistics()
    {
        //count styles
        styleStats = new ArrayList<>(srcTag.length);
        for (int i = 0; i < srcTag.length; i++)
            styleStats.add(new StyleCounter<NodeStyle>());
        for (Area a : areas)
        {
            for (int i = 0; i < srcTag.length; i++)
            {
                if (a.hasTag(srcTag[i]))
                    styleStats.get(i).add(new NodeStyle(a));
            }
        }
        //choose the most frequent styles for each tag
        styles = new ArrayList<List<NodeStyle>>();
        for (int i = 0; i < srcTag.length; i++)
            styles.add(new ArrayList<NodeStyle>(styleStats.get(i).getMostFrequentAll()));
        //choose the areas that match the style
        List<Area> selected = new ArrayList<>();
        for (Area a : areas)
        {
            for (int i = 0; i < srcTag.length; i++)
            {
                if (a.hasTag(srcTag[i]) && matchesAnyStyle(a, styles.get(i)))
                {
                    selected.add(a);
                    break;
                }
            }
        }
        //create pattern analyzer
        pa = new PatternAnalyzer(areas);
        pc = new PatternCounter<>();
        for (TagConnection con : pa.getTagConnections())
        {
            //System.out.println(con);
            pc.add(con, con.getWeight());
        }
    }

}

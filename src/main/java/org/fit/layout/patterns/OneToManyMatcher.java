/**
 * OneToManyMatcher.java
 *
 * Created on 28. 2. 2016, 17:07:22 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private float minSupport;
    
    private List<Area> areas;
    
    private List<StyleCounter<NodeStyle>> styleStats; //style statistics
    private List<List<NodeStyle>> styles;  //the most frequent styles for each tag
    private RelationAnalyzer pa;
    private PatternCounter<TagConnection> pc;
    
    
    public OneToManyMatcher(Tag srcTag1, Tag srcTag2, float minSupport, boolean fixedOrder)
    {
        srcTag = new Tag[2];
        srcTag[0] = srcTag1;
        srcTag[1] = srcTag2;
        this.minSupport = minSupport;
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
        
        for (TagConnection rel : pc.getAll().keySet())
        {
            if (rel.getA1().equals(srcTag[1]) && rel.getA2().equals(srcTag[0]))
            {
                log.debug("Relation {} : {} matches", rel, checkCovering(rel.getA1(), rel.getRelation(), rel.getA2()));
                log.debug("  by style {} {} {} : {}", styles.get(1).get(0), rel.getRelation(), styles.get(0).get(0),
                        checkCovering(styles.get(1).get(0), rel.getRelation(), styles.get(0).get(0)));
            }
        }
        
        log.debug("Combinations:");
        log.debug("------------:");
        for (NodeStyle style1 : styles.get(1))
        {
            for (NodeStyle style0 : styles.get(0))
            {
                for (TagConnection rel : pc.getAll().keySet())
                {
                    if (rel.getA1().equals(srcTag[1]) && rel.getA2().equals(srcTag[0]))
                    {
                        log.debug("{} with {} and {}", rel, style1, style0);
                        log.debug("  {}", checkCovering(rel.getA1(), style1, rel.getRelation(), rel.getA2(), style0));
                    }
                }
            }
        }
        
        log.debug("Chains below:");
        log.debug("-------");
        Set<Tag> consideredTags = new HashSet<Tag>(srcTag.length);
        for (Tag tag : srcTag)
            consideredTags.add(tag);
        ConsistentAreaAnalyzer ca = new ConsistentAreaAnalyzer(pa, consideredTags, minSupport);
        ca.findConsistentChains(new RelationBelow());
        log.debug("Chains side:");
        log.debug("-------");
        ca.findConsistentChains(new RelationSide());
        
     }
    
    //===========================================================================================
    
    private int checkCovering(Tag tag1, Relation relation, Tag tag2)
    {
        Set<Area> areas1 = new HashSet<Area>();
        Set<Area> areas2 = new HashSet<Area>();
        
        //start with all areas
        for (Area a : areas)
        {
            if (a.hasTag(tag1))
                areas1.add(a);
            if (a.hasTag(tag2))
                areas2.add(a);
        }
        
        return checkCovering(areas1, relation, areas2);
    }
    
    private int checkCovering(NodeStyle style1, Relation relation, NodeStyle style2)
    {
        Set<Area> areas1 = new HashSet<Area>();
        Set<Area> areas2 = new HashSet<Area>();
        
        //start with all areas
        for (Area a : areas)
        {
            NodeStyle astyle = new NodeStyle(a);
            if (astyle.equals(style1))
                areas1.add(a);
            if (astyle.equals(style2))
                areas2.add(a);
        }
        
        return checkCovering(areas1, relation, areas2);
    }
    
    private int checkCovering(Tag tag1, NodeStyle style1, Relation relation, Tag tag2, NodeStyle style2)
    {
        Set<Area> areas1 = new HashSet<Area>();
        Set<Area> areas2 = new HashSet<Area>();
        
        //start with all areas
        for (Area a : areas)
        {
            NodeStyle astyle = new NodeStyle(a);
            if (astyle.equals(style1) && a.hasTag(tag1))
                areas1.add(a);
            if (astyle.equals(style2) && a.hasTag(tag2))
                areas2.add(a);
        }
        
        return checkCovering(areas1, relation, areas2);
    }
    
    private int checkCovering(Set<Area> areas1, Relation relation, Set<Area> areas2)
    {
        int remainCnt1 = areas1.size();
        int remainCnt2 = areas2.size();
        int covered = 0;
        //remove matching pairs
        for (Area a : areas2)
        {
            List<Area> inrel = pa.getAreasInRelation(a, relation);
            for (Area b : inrel)
            {
                boolean matched = false;
                if (areas1.remove(b))
                {
                    remainCnt1--;
                    covered++;
                    matched = true;
                }
                if (matched)
                    remainCnt2--;
            }
        }
        
        //log.debug("  Remain {} {}", remainCnt1, remainCnt2);
        
        return covered;
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
        pa = new RelationAnalyzer(areas);
        pc = new PatternCounter<>();
        for (TagConnection con : pa.getTagConnections())
        {
            //System.out.println(con);
            pc.add(con, con.getWeight());
        }
    }

}

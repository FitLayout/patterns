/**
 * OneToManyMatcher.java
 *
 * Created on 28. 2. 2016, 17:07:22 by burgetr
 */
package org.fit.layout.patterns;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.classify.NodeStyle;
import org.fit.layout.classify.StyleCounter;
import org.fit.layout.model.Area;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.ConsistentAreaAnalyzer.ChainList;
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
    private RelationAnalyzer pa;
    private ChainList chains;
    
    
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
        
        log.debug("Styles:");
        for (int i = 0; i < srcTag.length; i++)
        {
            log.debug("Styles {}: {}", srcTag[i], styleStats.get(i));
        }
        dumpRelations(pa.getAreaConnections(), "/tmp/areas.arff");
        
        scanDisambiguations();
     }
    
    //===========================================================================================
    
    private void scanDisambiguations()
    {
        //lists of used styles for the individual tags
        NodeStyle styles[][] = new NodeStyle[srcTag.length][];
        for (int i = 0; i < srcTag.length; i++)
            styles[i] = styleStats.get(i).getDistinctStyles().toArray(new NodeStyle[0]);
        //generate style combinations
        int indices[] = new int[srcTag.length];
        Arrays.fill(indices, 0);
        while (indices[indices.length - 1] < styles[indices.length - 1].length)
        {
            //create the style map for this iteration
            Map<Tag, NodeStyle> curStyles = new HashMap<Tag, NodeStyle>(srcTag.length);
            for (int i = 0; i < srcTag.length; i++)
            {
                //System.out.println("Use " + srcTag[i] + " style " + styles[i][indices[i]]); 
                curStyles.put(srcTag[i], styles[i][indices[i]]);
            }
            log.debug("Style map: {}", curStyles);
            //if (curStyles.toString().equals("{minute=[fs:11.0 w:0.0 s:0.0 c:#000000 i:0], hour=[fs:15.0 w:0.0 s:0.0 c:#000000 i:0]}"))
            //{
            
            //build statistics for the selected disambiguations
            Disambiguator dis = new Disambiguator(curStyles, chains, minSupport);
            ConnectionList<TagConnection> cons = pa.getTagConnections(dis);
            PatternCounter<TagConnection> pc = new PatternCounter<>();
            for (TagConnection con : cons)
            {
                //System.out.println(con);
                pc.add(con, con.getWeight());
            }
            log.debug("Relations: {}", pc);
            
            //scan coverings
            for (TagConnection rel : pc.getAll().keySet())
            {
                if (rel.getA1().equals(srcTag[1]) && rel.getA2().equals(srcTag[0]))
                {
                    log.debug("Relation {} : {} matches", rel, checkCovering(rel.getA1(), rel.getRelation(), rel.getA2()));
                }
            }
            //}
            //increment the indices
            indices[0]++;
            for (int i = 0; i < indices.length - 1; i++)
            {
                if (indices[i] >= styles[i].length)
                {
                    indices[i] = 0;
                    indices[i+1]++;
                }
                else
                    break;
            }
        }
    }
    
    private int checkCovering(Tag tag1, Relation relation, Tag tag2)
    {
        Set<Area> areas1 = new HashSet<Area>();
        Set<Area> areas2 = new HashSet<Area>();
        
        //start with all areas
        for (Area a : areas)
        {
            if (a.hasTag(tag1, minSupport))
                areas1.add(a);
            if (a.hasTag(tag2, minSupport))
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
            if (astyle.equals(style1) && a.hasTag(tag1, minSupport))
                areas1.add(a);
            if (astyle.equals(style2) && a.hasTag(tag2, minSupport))
                areas2.add(a);
        }
        
        return checkCovering(areas1, relation, areas2);
    }
    
    private int checkCovering(Set<Area> areas1, Relation relation, Set<Area> areas2)
    {
        Set<Area> matchedAreas = new HashSet<Area>();
        //remove matching pairs
        for (Area a : areas2)
        {
            List<Area> inrel = pa.getAreasInRelation(a, relation);
            boolean matched = false;
            for (Area b : inrel)
            {
                if (areas1.remove(b))
                {
                    matchedAreas.add(b);
                    matched = true;
                }
            }
            if (matched)
                matchedAreas.add(a);
        }
        
        //log.debug("  Matched total {}", matchedAreas.size());
        //log.debug("Remain A1: {}", areas1);
        //log.debug("Remain A2: {}", areas2);
        
        return matchedAreas.size();
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
        //create pattern analyzer
        pa = new RelationAnalyzer(areas);
        /*pc = new PatternCounter<>();
        for (TagConnection con : pa.getTagConnections())
        {
            //System.out.println(con);
            pc.add(con, con.getWeight());
        }*/
        //discover tag chains used for disambiguation
        ConsistentAreaAnalyzer ca = new ConsistentAreaAnalyzer(pa, srcTag, minSupport);
        chains = ca.findConsistentChains(new RelationBelow());
        chains.addAll(ca.findConsistentChains(new RelationSide()));
    }

    private void dumpRelations(List<AreaConnection> connections, String outfile)
    {
        try
        {
            PrintWriter w = new PrintWriter(outfile);
            w.println("@RELATION areas");
            w.println("@ATTRIBUTE x NUMERIC");
            w.println("@ATTRIBUTE y NUMERIC");
            w.println("@data");
            for (AreaConnection con : connections)
            {
                if (con.getA1().hasTag(srcTag[1]) && con.getA2().hasTag(srcTag[0]))
                {
                    Rectangular bounds = new Rectangular(con.getA1().getBounds());
                    bounds.expandToEnclose(con.getA2().getBounds());
                    w.println("% " + con.toString());
                    w.println(bounds.getX1() + "," + bounds.getY1());
                    w.println(bounds.getX2() + "," + bounds.getY2());
                }
            }
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    
}

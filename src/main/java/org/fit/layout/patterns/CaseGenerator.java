/**
 * CaseGenerator.java
 *
 * Created on 13. 5. 2019, 14:48:05 by burgetr
 */
package org.fit.layout.patterns;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.Case;
import org.fit.layout.patterns.model.TagPattern;

/**
 * 
 * @author burgetr
 */
public class CaseGenerator
{
    private TagPattern pattern;
    private RelationAnalyzer pa;
    
    /** An index that assigns the best area connection to every source area and relation */
    private Map<Area, Map<Relation, AreaConnection>> areaIndex;
    
    
    public CaseGenerator(TagPattern pattern, RelationAnalyzer pa)
    {
        this.pattern = pattern;
        this.pa = pa;
        buildAreaIndex();
    }
    
    /**
     * Finds all the cases that correspond to the given tag pattern.
     * @return a set of particular cases
     */
    public Set<Case> generateCases()
    {
        Set<Case> ret = new HashSet<>();
        //TODO
        return ret;
    }
    
    
    //===================================================================================
    
    private void buildAreaIndex()
    {
        areaIndex = new HashMap<>();
        for (AreaConnection con : pa.getAreaConnections())
        {
            Map<Relation, AreaConnection> rels = areaIndex.get(con.getA2());
            if (rels != null)
            {
                AreaConnection current = rels.get(con.getRelation());
                if (current == null || con.getWeight() > current.getWeight())
                {
                    rels.put(con.getRelation(), con);
                }
            }
            else
            {
                rels = new HashMap<>();
                rels.put(con.getRelation(), con);
                areaIndex.put(con.getA2(), rels);
            }
        }
    }
    
    //===================================================================================
    // RDF output
    
    public void dumpIndex(String dest) throws IOException
    {
        PrintWriter out = new PrintWriter(dest);
        out.println("@prefix segm: <http://fitlayout.github.io/ontology/segmentation.owl#> .");
        out.println("@prefix p: <http://fitlayout.github.io/ontology/patterns.owl#> .");
        out.println("@prefix r: <http://fitlayout.github.io/resources/> .");
        for ( Map<Relation, AreaConnection> relmap : areaIndex.values())
        {
            for (AreaConnection con : relmap.values())
            {
                out.println(toUri(con.getA2()) + " " + toUri(con.getRelation()) + " " + toUri(con.getA1()) + " .");
                dumpTags(con.getA2(), out);
                dumpTags(con.getA1(), out);
            }
        }
        out.close();
    }
    
    private void dumpTags(Area a, PrintWriter out)
    {
        for (Tag t : a.getTags().keySet())
        {
            out.println(toUri(a) + " segm:hasTag " + toUri(t) + " .");
        }
    }
    
    private String toUri(Area a)
    {
        return "r:a" + a.getId();
    }
    
    private String toUri(Relation r)
    {
        return "p:" + r.getName();
    }
    
    private String toUri(Tag t)
    {
        return "r:tag-" + t.getValue();
    }
    
}

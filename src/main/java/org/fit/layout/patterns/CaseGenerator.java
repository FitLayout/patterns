/**
 * CaseGenerator.java
 *
 * Created on 13. 5. 2019, 14:48:05 by burgetr
 */
package org.fit.layout.patterns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.AreaConnection;

/**
 * 
 * @author burgetr
 */
public class CaseGenerator
{
    private List<Tag> tags;
    private RelationAnalyzer pa;
    
    /** An index that assigns the best area connection to every source area and relation */
    private Map<Area, Map<Relation, AreaConnection>> areaIndex;
    
    
    public CaseGenerator(List<Tag> tags, RelationAnalyzer pa)
    {
        this.tags = tags;
        this.pa = pa;
        buildAreaIndex();
    }
    
    public void generateCases()
    {
    }
    
    public void dumpIndex(String dest) throws IOException
    {
        PrintWriter out = new PrintWriter(dest);
        out.println("@prefix m: <http://fitlayout.github.io/ontology/patterns.owl#>");
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
            out.println(toUri(a) + " a:hasTag " + toUri(t) + " .");
        }
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
    
    private String toUri(Area a)
    {
        return "m:a" + a.getId();
    }
    
    private String toUri(Relation r)
    {
        return "m:" + r.getName();
    }
    
    private String toUri(Tag t)
    {
        return "t:" + t.getValue();
    }
    
}

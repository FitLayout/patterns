/**
 * PatternAnalyzer.java
 *
 * Created on 27. 2. 2016, 13:31:26 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.AreaConnectionList;
import org.fit.layout.patterns.model.TagConnection;
import org.fit.layout.patterns.model.TagConnectionList;

/**
 * 
 * @author burgetr
 */
public class RelationAnalyzer
{
    private static final float MIN_RELATION_WEIGHT = 0.1f;
    
    private List<Relation> analyzedRelations;
    private List<Area> areas;
    private AreaTopology topology;
    private AreaConnectionList areaConnections;
    private TagConnectionList tagConnections;
    
    public RelationAnalyzer(List<Area> areas)
    {
        this.areas = areas;
        
        topology = new AreaListGridTopology(areas);
        analyzedRelations = new ArrayList<>();
        initRelations();
    }

    protected void initRelations()
    {
        addRelation(new RelationSide(false));
        addRelation(new RelationUnder());
        addRelation(new RelationAfter(false));
        addRelation(new RelationBelow());
    }
    
    /**
     * Adds a new relation to the analyzed relations.
     * @param rel the relation to be added
     */
    public void addRelation(Relation rel)
    {
        analyzedRelations.add(rel);
    }
    
    public List<Area> getAreas()
    {
        return areas;
    }
    
    public AreaTopology getTopology()
    {
        return topology;
    }

    public AreaConnectionList getAreaConnections()
    {
        if (areaConnections == null)
        {
            areaConnections = new AreaConnectionList();
            for (Relation r : analyzedRelations)
                addConnectionsForRelation(areas, r, areaConnections);
        }
        return areaConnections;
    }
    
    private void addConnectionsForRelation(List<Area> areas, Relation relation, AreaConnectionList dest)
    {
        for (Area a1 : areas)
        {
            for (Area a2 : areas)
            {
                if (a1 != a2)
                {
                    float w = relation.isInRelationship(a1, a2, topology);
                    if (w >= MIN_RELATION_WEIGHT)
                    {
                        dest.add(new AreaConnection(a1, a2, relation, w));
                        //System.out.println(new AreaConnection(a1, a2, relation, w));
                    }
                }
            }
        }
    }

    /**
     * Obtains the area connection based on the given criteria.
     * @param dest the source area or {@code null} for any
     * @param r the relation or {@code null} for any
     * @param src the destination area or {@code null} for any
     * @param minWeight the minimal weight of the connection or a negative value for any
     * @return
     */
    public List<AreaConnection> getConnections(Area dest, Relation r, Area src, float minWeight)
    {
        List<AreaConnection> ret = new ArrayList<AreaConnection>();
        for (AreaConnection con : getAreaConnections())
        {
            if (con.getWeight() > minWeight
                    && (dest == null || con.getA1().equals(dest))
                    && (r == null || con.getRelation().equals(r))
                    && (src == null || con.getA2().equals(src)))
            {
                ret.add(con);
            }
        }
        return ret;
    }
    
    /**
     * Obtains all the area that are in the given relation with the given area.
     * I.e. where the destination is {@code a}. 
     * E.g. all areas below {@code a}. 
     * @param a the area to compare
     * @param r the relation to use.
     * @return the list of corresponding areas
     */
    public List<Area> getAreasInRelation(Area a, Relation r)
    {
        List<Area> ret = new ArrayList<Area>();
        for (AreaConnection con : getAreaConnections())
        {
            if (con.getA2().equals(a) && con.getRelation().equals(r))
            {
                ret.add(con.getA1());
            }
        }
        return ret;
    }

    /**
     * Obtains all the area that are in the given relation with the given area and there exists
     * no better source area for this with the same destination area and a higher weight.
     * E.g. all areas below {@code a}. 
     * @param a the area to compare
     * @param r the relation to use.
     * @return the list of corresponding areas
     */
    public List<Area> getAreasInBestRelation(Area a, Relation r)
    {
        List<AreaConnection> dest = getConnections(null, r, a, -1.0f);
        List<Area> ret = new ArrayList<Area>(dest.size());
        for (AreaConnection cand : dest)
        {
            //find the source nodes that are closer
            List<AreaConnection> better = getConnections(cand.getA1(), r, null, cand.getWeight());
            if (better.isEmpty())
                ret.add(cand.getA1()); //a1 has no "better" source area, use it
        }
        return ret;
    }
    
    public TagConnectionList getTagConnections()
    {
        if (tagConnections == null)
        {
            tagConnections = new TagConnectionList();
            for (AreaConnection ac : getAreaConnections())
            {
                Set<Tag> srcTags = ac.getA1().getTags().keySet();
                Set<Tag> dstTags = ac.getA2().getTags().keySet();
                if (!srcTags.isEmpty() && !dstTags.isEmpty())
                {
                    for (Tag src : srcTags)
                        for (Tag dest : dstTags)
                            tagConnections.add(new TagConnection(src, dest, ac.getRelation(), ac.getWeight()));
                }
            }
        }
        return tagConnections;
    }
    
    public TagConnectionList getTagConnections(Disambiguator dis)
    {
        TagConnectionList ret = new TagConnectionList();
        for (AreaConnection ac : getAreaConnections())
        {
            Tag src = dis.getAreaTag(ac.getA1());
            Tag dest = dis.getAreaTag(ac.getA2());
            if (src != null && dest != null)
                ret.add(new TagConnection(src, dest, ac.getRelation(), ac.getWeight()));
        }
        return ret;
    }
    
}

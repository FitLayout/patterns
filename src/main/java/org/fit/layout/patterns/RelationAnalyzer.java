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

/**
 * 
 * @author burgetr
 */
public class RelationAnalyzer
{
    private static final float MIN_WEIGHT = 0.1f;
    
    private List<Relation> analyzedRelations;
    private List<Area> areas;
    private AreaTopology topology;
    private ConnectionList<AreaConnection> areaConnections;
    private ConnectionList<TagConnection> tagConnections;
    
    public RelationAnalyzer(List<Area> areas)
    {
        this.areas = areas;
        
        topology = new AreaListGridTopology(areas);
        analyzedRelations = new ArrayList<>();
        analyzedRelations.add(new RelationSide());
        analyzedRelations.add(new RelationBelow());
    }
    
    public List<Area> getAreas()
    {
        return areas;
    }
    
    public AreaTopology getTopology()
    {
        return topology;
    }

    public ConnectionList<AreaConnection> getAreaConnections()
    {
        if (areaConnections == null)
        {
            areaConnections = new ConnectionList<>();
            for (Relation r : analyzedRelations)
                addConnectionsForRelation(areas, r, areaConnections);
        }
        return areaConnections;
    }
    
    private void addConnectionsForRelation(List<Area> areas, Relation relation, ConnectionList<AreaConnection> dest)
    {
        for (Area a1 : areas)
        {
            for (Area a2 : areas)
            {
                if (a1 != a2)
                {
                    float w = relation.isInRelationship(a1, a2, topology);
                    if (w >= MIN_WEIGHT)
                    {
                        dest.add(new AreaConnection(a1, a2, relation, w));
                        System.out.println(new AreaConnection(a1, a2, relation, w));
                    }
                }
            }
        }
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
        for (AreaConnection con : areaConnections)
        {
            if (con.getA2().equals(a) && con.getRelation().equals(r))
            {
                ret.add(con.getA1());
            }
        }
        return ret;
    }

    public ConnectionList<TagConnection> getTagConnections()
    {
        if (tagConnections == null)
        {
            tagConnections = new ConnectionList<>();
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
    
    
    
}

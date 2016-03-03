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
public class PatternAnalyzer
{
    private static final float MIN_WEIGHT = 0.1f;
    
    private List<Relation> analyzedRelations;
    private List<Area> areas;
    private ConnectionList<AreaConnection> areaConnections;
    private ConnectionList<TagConnection> tagConnections;
    
    public PatternAnalyzer(List<Area> areas)
    {
        this.areas = areas;
        
        AreaTopology topology = new AreaListGridTopology(areas);
        analyzedRelations = new ArrayList<>();
        analyzedRelations.add(new RelationSide(topology));
        analyzedRelations.add(new RelationBelow(topology));
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
                    float w = relation.isInRelationship(a1, a2);
                    if (w >= MIN_WEIGHT)
                        dest.add(new AreaConnection(a1, a2, relation, w));
                }
            }
        }
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

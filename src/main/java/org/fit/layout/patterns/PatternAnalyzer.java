/**
 * PatternAnalyzer.java
 *
 * Created on 27. 2. 2016, 13:31:26 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;

/**
 * 
 * @author burgetr
 */
public class PatternAnalyzer
{
    private static final float MIN_WEIGHT = 0.1f;
    
    private List<Relation> analyzedRelations;
    private List<Area> areas;
    
    public PatternAnalyzer(List<Area> areas)
    {
        this.areas = areas;
        
        analyzedRelations = new ArrayList<>();
        analyzedRelations.add(new RelationSide());
    }
    
    public ConnectionList<AreaConnection> findAreaConnections()
    {
        ConnectionList<AreaConnection> ret = new ConnectionList<>();
        for (Relation r : analyzedRelations)
            addConnectionsForRelation(areas, r, ret);
        return ret;
    }
    
    private void addConnectionsForRelation(List<Area> areas, Relation relation, ConnectionList<AreaConnection> dest)
    {
        for (Area a1 : areas)
        {
            for (Area a2 : areas)
            {
                float w = relation.isInRelationship(a1, a2);
                if (w >= MIN_WEIGHT)
                    dest.add(new AreaConnection(a1, a2, relation, w));
            }
        }
    }
    
}

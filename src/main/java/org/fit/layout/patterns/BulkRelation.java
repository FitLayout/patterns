/**
 * RelationBulk.java
 *
 * Created on 15. 2. 2018, 15:13:31 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.patterns.model.AreaConnection;

/**
 * A relation that may be discovered by a bulk scan of the area topology.
 * 
 * @author burgetr
 */
public interface BulkRelation extends Relation
{

    public Set<AreaConnection> findRelations(AreaTopology topology, Collection<Area> areas);
    
}

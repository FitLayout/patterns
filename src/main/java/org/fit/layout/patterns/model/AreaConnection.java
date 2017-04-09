/**
 * AreaConnection.java
 *
 * Created on 28. 2. 2016, 18:45:16 by burgetr
 */
package org.fit.layout.patterns.model;

import org.fit.layout.model.Area;
import org.fit.layout.patterns.Relation;

/**
 * 
 * @author burgetr
 */
public class AreaConnection extends Connection<Area>
{

    public AreaConnection(Area a1, Area a2, Relation relation, float weight)
    {
        super(a1, a2, relation, weight);
    }
    
}

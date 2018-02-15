/**
 * Relation.java
 *
 * Created on 15. 2. 2018, 15:08:37 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Set;

import org.fit.layout.patterns.model.Metric;

/**
 * A basic relation interface.
 * 
 * @author burgetr
 */
public interface Relation
{
    
    /**
     * Gets the name of the relation.
     * @return the relation name
     */
    public String getName();

    /**
     * The set of metrics that should be used for evaluating this relation.
     * @return the set of metrics
     */
    public abstract Set<Metric> metrics();
    

}

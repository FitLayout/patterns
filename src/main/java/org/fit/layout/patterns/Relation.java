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
    public static final Relation ONRIGHT = new RelationSide(false);
    public static final Relation ONLEFT = new RelationSide(true);
    public static final Relation AFTER = new RelationAfter(false);
    public static final Relation BEFORE = new RelationAfter(true);
    public static final Relation SAMELINE = new RelationSameLine();
    public static final Relation UNDER = new RelationUnder();
    public static final Relation UNDERHEADING = new RelationUnderHeading();
    public static final Relation BELOW = new RelationBelow(false);
    public static final Relation ABOVE = new RelationBelow(true);
    public static final Relation LINEBELOW = new RelationLineBelow();
    
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

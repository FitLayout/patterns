/**
 * Relation.java
 *
 * Created on 15. 2. 2018, 15:08:37 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Set;

import org.fit.layout.patterns.model.DefaultMetrics;
import org.fit.layout.patterns.model.Metric;

/**
 * A basic relation interface.
 * 
 * @author burgetr
 */
public interface Relation
{
    public static final Relation ONRIGHT = new BasicRelation("onRight", DefaultMetrics.heightMetrics);
    public static final Relation ONLEFT = new BasicRelation("onLeft", DefaultMetrics.heightMetrics);
    public static final Relation AFTER = new BasicRelation("after", DefaultMetrics.heightMetrics);
    public static final Relation BEFORE = new BasicRelation("before", DefaultMetrics.heightMetrics);
    public static final Relation SAMELINE = new BasicRelation("sameLine", DefaultMetrics.heightMetrics);
    public static final Relation UNDER = new BasicRelation("under", DefaultMetrics.widthMetrics);
    public static final Relation UNDERHEADING = new BasicRelation("underHeading", DefaultMetrics.allMetrics);
    public static final Relation BELOW = new BasicRelation("below", DefaultMetrics.widthMetrics);
    public static final Relation ABOVE = new BasicRelation("above", DefaultMetrics.widthMetrics);
    public static final Relation LINEBELOW = new BasicRelation("lineBelow", DefaultMetrics.allMetrics);
    
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

/**
 * RelationProbabilitySource.java
 *
 * Created on 29. 11. 2017, 15:03:47 by burgetr
 */
package org.fit.layout.patterns;

/**
 * This interface defines a source of relation probabilities. To each relation, it assigns a generic probability to create
 * the 1:1 or 1:M relationships.
 * 
 * @author burgetr
 */
public interface RelationProbabilitySource
{

    /**
     * Returns the probability of the given relation when applied to 1:1 relationships among areas.
     * @param relation the relation to be considered
     * @return the resulting probability between 0.0 and 1.0
     */
    public float get11Probability(Relation relation);
    
    /**
     * Returns the probability of the given relation when applied to 1:M relationships among areas.
     * @param relation the relation to be considered (e.g. 'below' means that we want the probability of M-below-1).
     * @return the resulting probability between 0.0 and 1.0
     */
    public float get1MProbability(Relation relation);
    
}

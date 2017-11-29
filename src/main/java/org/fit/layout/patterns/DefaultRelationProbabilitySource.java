/**
 * DefaultRelationProbabilitySource.java
 *
 * Created on 29. 11. 2017, 15:08:20 by burgetr
 */
package org.fit.layout.patterns;

import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation of the probability source that uses a hardcoded table of probabilities. When the relation is not contained
 * in the table, a maximal probability of 1.0 is returned.
 * 
 * @author burgetr
 */
public class DefaultRelationProbabilitySource implements RelationProbabilitySource
{
    private static final float DEFAULT_PROBABILITY = 1.0f;
    private static Map<String, Float> probs11;
    private static Map<String, Float> probs1M;
    
    static {
        probs11 = new HashMap<>();
        //no specific cases for 1:1 yet
        probs1M = new HashMap<>();
        probs1M.put("above", 0.2f); //it is not too probable to have the MANY part above 1 (e.g. the values above the key).
        probs1M.put("before", 0.2f);
        probs1M.put("onLeft", 0.2f);
    }

    @Override
    public float get11Probability(Relation relation)
    {
        return findWeight(probs11, relation);
    }

    @Override
    public float get1MProbability(Relation relation)
    {
        return findWeight(probs1M, relation);
    }

    private float findWeight(Map<String, Float> map, Relation relation)
    {
        Float ret = map.get(relation.getName());
        return (ret == null) ? DEFAULT_PROBABILITY : ret.floatValue();
    }
    
}

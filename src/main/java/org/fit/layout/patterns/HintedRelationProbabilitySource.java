/**
 * HintedRelationProbabilitySource.java
 *
 * Created on 18. 1. 2019, 10:49:28 by burgetr
 */
package org.fit.layout.patterns;

import java.util.List;

import org.fit.layout.model.Tag;
import org.fit.layout.patterns.chunks.PresentationHint;
import org.fit.layout.patterns.model.MatcherConfiguration;
import org.fit.layout.patterns.model.TagConnection;

/**
 * A source of relation probability that considers current matcher configuration, mainly the probabilities
 * of different relations based on the layout of the related chunks (block vs. inline)
 * 
 * @author burgetr
 */
public class HintedRelationProbabilitySource extends DefaultRelationProbabilitySource
{
    private MatcherConfiguration conf;
    

    public HintedRelationProbabilitySource(MatcherConfiguration conf)
    {
        this.conf = conf;
    }

    @Override
    public float get11Probability(Relation relation)
    {
        float ret = super.get11Probability(relation); 
        final TagConnection con = findTagConnection(relation);
        if (con != null)
            ret = ret * getFactor(con, false);
        return ret; 
    }

    @Override
    public float get1MProbability(Relation relation)
    {
        float ret = super.get1MProbability(relation); 
        final TagConnection con = findTagConnection(relation);
        if (con != null)
            ret = ret * getFactor(con, true);
        return ret; 
    }

    //======================================================================================
    
    private float getFactor(TagConnection con, boolean many)
    {
        //final boolean a1block = isBlock(con.getA1());
        final boolean a2block = isBlock(con.getA2());
        final boolean a1inline = isInline(con.getA1());
        //final boolean a2inline = isInline(con.getA2());
        
        if (a2block || a1inline)
        {
            switch (con.getRelation().getName())
            {
                case "below":
                    return 0.5f;
                case "above":
                    return 0.2f;
                default:
                    return 1.0f;
            }
        }
        else
            return 1.0f;
    }
    
    /**
     * Finds the corresponding pattern in the configuration that has the given relation.
     * @param relation
     * @return
     */
    private TagConnection findTagConnection(Relation relation)
    {
        for (TagConnection con : conf.getPattern())
        {
            if (con.getRelation().equals(relation))
                return con;
        }
        return null;
    }
    
    private boolean isBlock(Tag tag)
    {
        if (conf.getHints() != null)
        {
            final List<PresentationHint> hints = conf.getHints().get(tag);
            if (hints != null)
            {
                for (PresentationHint hint : hints)
                {
                    if (hint.impliesBlock())
                        return true;
                }
            }
            return false;
        }
        else
            return false;
    }
    
    private boolean isInline(Tag tag)
    {
        if (conf.getHints() != null)
        {
            final List<PresentationHint> hints = conf.getHints().get(tag);
            if (hints != null)
            {
                for (PresentationHint hint : hints)
                {
                    if (hint.impliesInline())
                        return true;
                }
            }
            return false;
        }
        else
            return false;
    }
    
}

/**
 * ConsistentAreaAnalyzer.java
 *
 * Created on 11. 3. 2016, 16:38:18 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class ConsistentAreaAnalyzer
{
    private RelationAnalyzer ra;
    private Set<Tag> consideredTags;
    
    public ConsistentAreaAnalyzer(RelationAnalyzer ra, Set<Tag> consideredTags)
    {
        this.ra = ra;
        this.consideredTags = consideredTags;
    }
    
    public List<AreaChain> findConsistentChains(Relation rel)
    {
        List<Area> areas = ra.getAreas();
        List<Area> remain = new ArrayList<Area>(areas);
        List<AreaChain> ret = new ArrayList<AreaChain>();
        
        while (!remain.isEmpty())
        {
            AreaChain newchain = findAreaChainFor(remain.get(0), rel);
            remain.removeAll(newchain);
            if (newchain.size() > 2 && !newchain.getTags().isEmpty())
            {
                System.out.println("Found: " + newchain);
                ret.add(newchain);
            }
        }
        return ret;
    }
    
    private AreaChain findAreaChainFor(Area a, Relation rel)
    {
        if (a.getId() == 582)
            System.out.println("jo!");
        AreaChain ret = new AreaChain(rel);
        ret.add(a);
        
        Set<Tag> curTags = new HashSet<Tag>(a.getTags().keySet());
        curTags.retainAll(consideredTags); //focus on the considered tags only
        
        List<Area> inrel;
        Area cur = a;
        do
        {
            inrel = ra.getAreasInRelation(cur, rel);
            if (!inrel.isEmpty())
            {
                Area next = inrel.get(0);
                if (!hasConsideredTag(next))
                    curTags.retainAll(next.getTags().keySet());
                ret.add(next);
                cur = next;
            }
        } while (!inrel.isEmpty());
        
        ret.setTags(curTags);
        return ret;
    }


    
    private boolean hasConsideredTag(Area a)
    {
        for (Tag t : consideredTags)
        {
            if (a.hasTag(t))
                return true;
        }
        return false;
    }
    
    //===================================================================================
    
    public class AreaChain extends ArrayList<Area>
    {
        private static final long serialVersionUID = 1L;
        private Relation relation;
        private Set<Tag> tags;

        public AreaChain(Relation relation)
        {
            super();
            this.relation = relation;
        }

        public Relation getRelation()
        {
            return relation;
        }

        public Set<Tag> getTags()
        {
            return tags;
        }

        public void setTags(Set<Tag> tags)
        {
            this.tags = tags;
        }

        @Override
        public String toString()
        {
            return tags.toString() + " " + super.toString();
        }
        
    }
    
}

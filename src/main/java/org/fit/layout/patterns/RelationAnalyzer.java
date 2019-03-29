/**
 * PatternAnalyzer.java
 *
 * Created on 27. 2. 2016, 13:31:26 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fit.layout.impl.AreaListGridTopology;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.AreaConnectionList;
import org.fit.layout.patterns.model.TagConnection;
import org.fit.layout.patterns.model.TagConnectionList;

/**
 * 
 * @author burgetr
 */
public abstract class RelationAnalyzer
{
    public static final float MIN_RELATION_WEIGHT = 0.1f;
    
    private List<Area> areas;
    private AreaTopology topology;
    private AreaConnectionList areaConnections;
    private TagConnectionList tagConnections;
    
    //different indices
    private Map<Area, Collection<AreaConnection>> indexA1;
    private Map<Area, Collection<AreaConnection>> indexA2;
    private Map<Relation, Collection<AreaConnection>> indexR;
    
    public RelationAnalyzer(List<Area> areas)
    {
        this.areas = areas;
        topology = new AreaListGridTopology(areas);
    }

    public abstract List<Relation> getAnalyzedRelations();

    /**
     * Adds all the connections based on the evaluated relations.
     */
    protected abstract void addConnections();
    
    public List<Area> getAreas()
    {
        return areas;
    }
    
    /**
     * Returns all the areas that have another area in some relation (e.g. the area has something above it)
     * @return A set of source areas
     */
    public Set<Area> getSourceAreas()
    {
        getAreaConnections();
        return indexA2.keySet();
    }
    
    /**
     * Returns all the areas that are in some relation with another area (e.g. the area is above something)
     * @return A set of destination areas
     */
    public Set<Area> getDestinationAreas()
    {
        getAreaConnections();
        return indexA1.keySet();
    }
    
    /**
     * Returns all relations that really occur among the areas.
     * @return the set of areas
     */
    public Set<Relation> getDistinctRelations()
    {
        getAreaConnections();
        return indexR.keySet();
    }
    
    public AreaTopology getTopology()
    {
        return topology;
    }

    public AreaConnectionList getAreaConnections()
    {
        if (areaConnections == null)
        {
            areaConnections = new AreaConnectionList();
            indexA1 = new HashMap<>();
            indexA2 = new HashMap<>();
            indexR = new HashMap<>();
            addConnections();
        }
        return areaConnections;
    }
    
    protected void addAreaConnection(AreaConnection con)
    {
        //add to the list
        areaConnections.add(con);
        //add to all the indices
        addToIndex(indexA1, con.getA1(), con);
        addToIndex(indexA2, con.getA2(), con);
        addToIndex(indexR, con.getRelation(), con);
    }
    
    /**
     * Obtains the area connection based on the given criteria.
     * @param dest the source area or {@code null} for any
     * @param r the relation or {@code null} for any
     * @param src the destination area or {@code null} for any
     * @param minWeight the minimal weight of the connection or a negative value for any
     * @return
     */
    public Collection<AreaConnection> getConnections(Area dest, Relation r, Area src, float minWeight)
    {
        Collection<AreaConnection> all = getAreaConnections();
        int iused = 0;
        if (dest != null)
        {
            Collection<AreaConnection> sub = indexA1.get(dest);
            if (sub != null)
            {
                all = sub;
                iused = 1;
            }
        }
        if (r != null)
        {
            Collection<AreaConnection> sub = indexR.get(r);
            if (sub != null && sub.size() < all.size())
            {
                all = sub;
                iused = 2;
            }
        }
        if (src != null)
        {
            Collection<AreaConnection> sub = indexA2.get(src);
            if (sub != null && sub.size() < all.size())
            {
                all = sub;
                iused = 3;
            }
        }
        
        List<AreaConnection> ret = new ArrayList<AreaConnection>();
        if (all != null)
        {
            if (minWeight >= 0)
            {
                for (AreaConnection con : all)
                {
                    if (con.getWeight() > minWeight
                            && (iused == 1 || dest == null || con.getA1().equals(dest))
                            && (iused == 2 || r == null || con.getRelation().equals(r))
                            && (iused == 3 || src == null || con.getA2().equals(src)))
                    {
                        ret.add(con);
                    }
                }
            }
            else
            {
                for (AreaConnection con : all)
                {
                    if ((iused == 1 || dest == null || con.getA1().equals(dest))
                            && (iused == 2 || r == null || con.getRelation().equals(r))
                            && (iused == 3 || src == null || con.getA2().equals(src)))
                    {
                        ret.add(con);
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * Obtains all the area that are in the given relation with the given area.
     * I.e. where the destination is {@code a}. 
     * E.g. all areas below {@code a}. 
     * @param a the area to compare
     * @param r the relation to use.
     * @return the list of corresponding areas
     */
    public List<Area> getAreasInRelation(Area a, Relation r)
    {
        List<Area> ret = new ArrayList<Area>();
        for (AreaConnection con : getAreaConnections())
        {
            if (con.getA2().equals(a) && con.getRelation().equals(r))
            {
                ret.add(con.getA1());
            }
        }
        return ret;
    }

    /**
     * Obtains all the area that are in the given relation with the given area and there exists
     * no better source area for this with the same destination area and a higher weight.
     * E.g. all areas below {@code a}. 
     * @param a the area to compare
     * @param r the relation to use.
     * @return the list of corresponding areas
     */
    public List<Area> getAreasInBestRelation(Area a, Relation r)
    {
        Collection<AreaConnection> dest = getConnections(null, r, a, -1.0f);
        List<Area> ret = new ArrayList<Area>(dest.size());
        for (AreaConnection cand : dest)
        {
            //find the source nodes that are closer
            Collection<AreaConnection> better = getConnections(cand.getA1(), r, null, cand.getWeight());
            if (better.isEmpty())
                ret.add(cand.getA1()); //a1 has no "better" source area, use it
        }
        return ret;
    }
    
    /**
     * Finds all relations between a pair of areas in the page.
     * @param a1 the first area
     * @param a2 the second area
     * @param minWeight minimal relation weight to be considered
     * @return A set of all relations found between {@code a1} and {@code a2}
     */
    public Set<Relation> getRelationsFor(Area a1, Area a2, float minWeight)
    {
        Collection<AreaConnection> conns = getConnections(a1, null, a2, minWeight);
        Set<Relation> ret = new HashSet<>();
        for (AreaConnection con : conns)
            ret.add(con.getRelation());
        return ret;
    }
    
    public TagConnectionList getTagConnections()
    {
        if (tagConnections == null)
        {
            tagConnections = new TagConnectionList();
            for (AreaConnection ac : getAreaConnections())
            {
                final Set<Tag> srcTags = ac.getA1().getTags().keySet();
                final Set<Tag> dstTags = ac.getA2().getTags().keySet();
                if (!srcTags.isEmpty() && !dstTags.isEmpty())
                {
                    for (Tag src : srcTags)
                        for (Tag dest : dstTags)
                            tagConnections.add(new TagConnection(src, dest, ac.getRelation(), ac.getWeight()));
                }
            }
        }
        return tagConnections;
    }
    
    public TagConnectionList getTagConnections(StyleResolver dis)
    {
        TagConnectionList ret = new TagConnectionList();
        for (AreaConnection ac : getAreaConnections())
        {
            final Set<Tag> srcTags = dis.getAreaTags(ac.getA1());
            final Set<Tag> destTags = dis.getAreaTags(ac.getA2());
            for (Tag src : srcTags)
            {
                for (Tag dest : destTags)
                {
                    ret.add(new TagConnection(src, dest, ac.getRelation(), ac.getWeight()));
                }
            }
        }
        return ret;
    }

    //==============================================================================================================
    
    private <T> void addToIndex(Map<T, Collection<AreaConnection>> index, T key, AreaConnection item)
    {
        Collection<AreaConnection> items = index.get(key);
        if (items == null)
        {
            items = new LinkedList<>();
            index.put(key, items);
        }
        items.add(item);
    }
    
}

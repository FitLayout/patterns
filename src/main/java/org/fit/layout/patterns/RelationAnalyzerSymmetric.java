/**
 * RelationAnalyzerSymmetric.java
 *
 * Created on 17. 3. 2016, 14:15:42 by burgetr
 */
package org.fit.layout.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;
import org.fit.layout.patterns.model.AreaConnection;

/**
 * A relation analyzer that includes both the normal and inverse relations where applicable.
 * @author burgetr
 */
public class RelationAnalyzerSymmetric extends RelationAnalyzer
{
    private static final List<Relation> ANALYZED_RELATIONS;
    static {
        ANALYZED_RELATIONS = new ArrayList<>(10);
        ANALYZED_RELATIONS.add(Relation.ONRIGHT);
        ANALYZED_RELATIONS.add(Relation.ONLEFT);
        ANALYZED_RELATIONS.add(Relation.AFTER);
        ANALYZED_RELATIONS.add(Relation.BEFORE);
        ANALYZED_RELATIONS.add(Relation.SAMELINE);
        ANALYZED_RELATIONS.add(Relation.UNDER);
        ANALYZED_RELATIONS.add(Relation.UNDERHEADING);
        ANALYZED_RELATIONS.add(Relation.BELOW);
        ANALYZED_RELATIONS.add(Relation.ABOVE);
        ANALYZED_RELATIONS.add(Relation.LINEBELOW);
    }

    public RelationAnalyzerSymmetric(List<Area> areas)
    {
        super(areas);
    }

    @Override
    public List<Relation> getAnalyzedRelations()
    {
        return ANALYZED_RELATIONS;
    }

    @Override
    protected void addConnections()
    {
        final List<Area> areas = getAreas();
        addSameLineConnections(areas);
        addBelowAboveConnections(areas);
        addLineBelowRelations(areas);
        addUnderHeadingRelations(areas);
    }

    //=====================================================================================================
    
    private void addSameLineConnections(List<Area> areas)
    {
        if (!areas.isEmpty())
        {
            //total page width (use the page of the first area, all areas should share the same page anyway)
            final int tw = areas.get(0).getPage().getWidth();
            //scan the line relationships
            for (Area a1 : areas)
            {
                final Rectangular b1 = a1.getBounds();
                for (Area a2 : areas)
                {
                    final Rectangular b2 = a2.getBounds();
                    if (a1 != a2 && !b1.intersects(b2))
                    {
                        if (AreaUtils.isOnSameLine(b2, b1)) //TODO inline?
                        {
                            final float em = Math.max(a2.getFontSize(), a1.getFontSize());
                            final int distLL = Math.max(b2.getX1() - b1.getX1(), b1.getX1() - b2.getX1());
                            final int distRL = b1.getX1() - b2.getX2();
                            final int distLR = b2.getX1() - b1.getX2();
                            //same line
                            if (distLL > 0)
                            {
                                final float w = 1.0f - ((float) distLL / tw);
                                if (w > RelationAnalyzer.MIN_RELATION_WEIGHT)
                                    addAreaConnection(new AreaConnection(a1, a2, Relation.SAMELINE, w));
                            }
                            //after / before
                            if (distRL > 0)
                            {
                                final float w = 1.0f - (distRL / 3) * 3.0f / tw;
                                if (w > RelationAnalyzer.MIN_RELATION_WEIGHT)
                                    addAreaConnection(new AreaConnection(a1, a2, Relation.AFTER, w));
                            }
                            else if (distLR > 0)
                            {
                                final float w = 1.0f - (distLR / 3) * 3.0f / tw;
                                if (w > RelationAnalyzer.MIN_RELATION_WEIGHT)
                                    addAreaConnection(new AreaConnection(a1, a2, Relation.BEFORE, w));
                            }
                            //onRight / onLeft
                            if (distRL > -0.2*em && distRL < 0.9*em)
                            {
                                addAreaConnection(new AreaConnection(a1, a2, Relation.ONRIGHT, 1.0f));
                            }
                            else if (distLR > -0.2*em && distLR < 0.9*em)
                            {
                                addAreaConnection(new AreaConnection(a1, a2, Relation.ONLEFT, 1.0f));
                            }
                        }
                    }
                }
            }
        }
    }
    
    //=====================================================================================================
    
    private void addBelowAboveConnections(List<Area> areas)
    {
        if (!areas.isEmpty())
        {
            //total page width (use the page of the first area, all areas should share the same page anyway)
            final int th = areas.get(0).getPage().getWidth(); //TODO should we use height?
            //scan the line relationships
            final Set<Entry<Area,Rectangular>> entries = getTopology().getPositionMap().entrySet();
            for (Entry<Area,Rectangular> e1 : entries)
            {
                for (Entry<Area,Rectangular> e2 : entries)
                {
                    final Area a1 = e1.getKey();
                    final Area a2 = e2.getKey();
                    if (a1 != a2 && !a1.getBounds().intersects(a2.getBounds()))
                    {
                        checkBelowUnder(a1, e1.getValue(), a2, e2.getValue(), th);
                    }
                }
            }
        }
    }
    
    private void checkBelowUnder(Area a1, Rectangular gp1, Area a2, Rectangular gp2, int th)
    {
        //here a1 is the bottom area, a2 is the top area
        //we say that a1 is below a2
        final Rectangular inter = gp1.intersection(new Rectangular(gp2.getX1(), gp1.getY1(), gp2.getX2(), gp1.getY2()));
        if (inter.getWidth() > Math.min(gp1.getWidth(), gp2.getWidth()) / 2) //at least 1/2 of the smaller area overlaps
        {
            final float dist = a1.getBounds().getY1() - a2.getBounds().getY2();
            final float em = Math.max(a2.getFontSize(), a1.getFontSize());
            if (dist >= -0.5f*em)
            {
                final float w = 1.0f - dist / th;
                if (w > RelationAnalyzer.MIN_RELATION_WEIGHT)
                {
                    addAreaConnection(new AreaConnection(a1, a2, Relation.BELOW, w));
                    addAreaConnection(new AreaConnection(a2, a1, Relation.ABOVE, w));
                }
                //add 'under' if it is close enough
                if (dist < 0.8f*em)
                    addAreaConnection(new AreaConnection(a1, a2, Relation.UNDER, 1.0f));
            }
        }
    }

    //=====================================================================================================
    
    private void addLineBelowRelations(List<Area> areas)
    {
        if (!areas.isEmpty())
        {
            final int tw = areas.get(0).getPage().getWidth();
            final int th = areas.get(0).getPage().getHeight();
            for (Area a : areas)
                findLineBelow(a, areas, tw, th);
        }
    }
 
    private void findLineBelow(Area a, List<Area> areas, int tw, int th)
    {
        //find the closest area
        float maxW = 0;
        Area closest = null;
        for (Area cand : areas)
        {
            float w = computeWeight(cand, a, tw, th);
            if (w > maxW)
            {
                closest = cand;
                maxW = w;
            }
        }
        //find all on the same line
        if (closest != null)
        {
            Set<Area> used = new HashSet<>();
            for (Area cand : areas)
            {
                if ((cand == closest || AreaUtils.isOnSameLine(cand, closest)) && !used.contains(cand))
                {
                    float w = computeWeight(cand, a, tw, th);
                    addAreaConnection(new AreaConnection(cand, a, Relation.LINEBELOW, w));
                    used.add(cand);
                    //try to use the chunks on the same logical line (if any)
                    if (cand.getLine() != null)
                    {
                        for (Area sibl : cand.getLine())
                        {
                            if (!used.contains(sibl))
                            {
                                addAreaConnection(new AreaConnection(sibl, a, Relation.LINEBELOW, w));
                                used.add(sibl);
                            }
                        }
                    }
                }   
            }
        }
    }
    
    //=====================================================================================================
    
    private void addUnderHeadingRelations(List<Area> areas)
    {
        if (!areas.isEmpty())
        {
            final int tw = areas.get(0).getPage().getWidth();
            final int th = areas.get(0).getPage().getHeight();
            for (Area a : areas)
                findSubordinate(a, getTopology(), areas, tw, th);
        }
    }

    private void findSubordinate(Area a, AreaTopology t, Collection<Area> areas, int tw, int th)
    {
        float m1 = getMarkedness(a);
        Rectangular gp = new Rectangular(t.getPosition(a));
        
        List<Area> candidates = new ArrayList<>();
        while (expandDown(gp, t, m1, candidates))
            ;
        
        for (Area c : candidates)
        {
            final float w = computeWeight(c, a, tw, th);
            addAreaConnection(new AreaConnection(c, a, Relation.UNDERHEADING, w));
        }
        
        /*if (!candidates.isEmpty())
        {
            System.out.println("Heading: " + a);
            for (Area c : candidates)
                System.out.println("    " + c);
        }*/
    }
    
    private boolean expandDown(Rectangular gp, AreaTopology t, float m1, List<Area> destAreas)
    {
        int nextY = gp.getY2() + 1;
        if (nextY < t.getTopologyHeight())
        {
            boolean found = false;
            int x = gp.getX1();
            while (x <= gp.getX2())
            {
                Collection<Area> cands = t.findAllAreasAt(x, nextY);
                if (!cands.isEmpty())
                {
                    Rectangular cgp = null;
                    for (Area cand : cands)
                    {
                        if (getMarkedness(cand) < m1) //acceptable candidate
                        {
                            found = true;
                            destAreas.add(cand);
                            if (cgp == null)
                                cgp = t.getPosition(cand);
                            else
                                cgp.expandToEnclose(t.getPosition(cand));
                        }
                        else
                        {
                            return false; //unacceptable candidate found - cannot expand
                        }
                    }
                    if (cgp != null)
                    {
                        gp.expandToEnclose(cgp);
                        x += cgp.getWidth();
                    }
                }
                else
                    x++;
            }
            if (!found) //empty row, try the next one
                gp.setY2(nextY);
            return true;
        }
        else
            return false;
    }

    private float getMarkedness(Area a)
    {
        //simplified markedness version, use font size and weight only
        return a.getFontSize() * 10 + a.getFontWeight();
    }

    private float computeWeight(Area a1, Area a2, int tw, int th)
    {
        //a2 is the heading, a1 should be under the heading
        final float distX = Math.abs(a1.getBounds().getX1() - a2.getBounds().getX1());
        final float distY = a1.getBounds().getY1() - a2.getBounds().getY2();
        final float em = a1.getFontSize();
        if (distY >= -0.5f*em)
        {
            float ww = 1.0f - distX / tw;
            float wh = 1.0f - distY / th;
            float w = ww * wh;
            return w;
        }
        else
            return 0;
    }
    
}

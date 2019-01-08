/**
 * SameLineRelations.java
 *
 * Created on 8. 1. 2019, 14:57:11 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Rectangular;
import org.fit.layout.patterns.model.AreaConnection;
import org.fit.layout.patterns.model.Metric;

/**
 * 
 * @author burgetr
 */
public class SameLineRelations implements BulkRelation
{
    private static final Relation ONRIGHT = new RelationSide(false);
    private static final Relation ONLEFT = new RelationSide(true);
    private static final Relation AFTER = new RelationAfter(false);
    private static final Relation BEFORE = new RelationAfter(true);
    private static final Relation SAMELINE = new RelationSameLine();

    @Override
    public String getName()
    {
        return null; //this relations is never used as such; the sameLine, before and after relations are generated instead
    }

    @Override
    public Set<Metric> metrics()
    {
        return null;
    }

    @Override
    public Set<AreaConnection> findRelations(AreaTopology topology, Collection<Area> areas)
    {
        Set<AreaConnection> ret = new HashSet<>();
        if (!areas.isEmpty())
        {
            //total page width (use the page of the first area, all areas should share the same page anyway)
            int tw = areas.iterator().next().getPage().getWidth();
            //scan the line relationships
            for (Area a1 : areas)
            {
                final Rectangular b1 = a1.getBounds();
                for (Area a2 : areas)
                {
                    final Rectangular b2 = a2.getBounds();
                    if (a1 != a2 && !b1.intersects(b2))
                    {
                        if (AreaUtils.isOnSameLine(b2, b1)) //TODO?
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
                                    ret.add(new AreaConnection(a1, a2, SAMELINE, w));
                            }
                            //after / before
                            if (distRL > 0)
                            {
                                float w = 1.0f - (distRL / 3) * 3.0f / tw;
                                if (w > RelationAnalyzer.MIN_RELATION_WEIGHT)
                                    ret.add(new AreaConnection(a1, a2, AFTER, w));
                            }
                            else if (distLR > 0)
                            {
                                float w = 1.0f - (distLR / 3) * 3.0f / tw;
                                if (w > RelationAnalyzer.MIN_RELATION_WEIGHT)
                                    ret.add(new AreaConnection(a1, a2, BEFORE, w));
                            }
                            //onRight / onLeft
                            if (distRL > -0.2*em && distRL < 0.9*em)
                            {
                                ret.add(new AreaConnection(a1, a2, ONRIGHT, 1.0f));
                            }
                            else if (distLR > -0.2*em && distLR < 0.9*em)
                            {
                                ret.add(new AreaConnection(a1, a2, ONLEFT, 1.0f));
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

}

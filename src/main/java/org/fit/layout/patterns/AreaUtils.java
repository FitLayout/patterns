/**
 * AreaUtils.java
 *
 * Created on 13. 3. 2015, 17:01:42 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Collection;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;

/**
 * General purpose area analysis functions.
 * 
 * @author burgetr
 */
public class AreaUtils
{
    
    /**
     * Checks if the given areas are in the same visual group (i.e. "are near each other"). 
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isNeighbor(Area a1, Area a2)
    {
        if (isOnSameLine(a1, a2))
            return true; //on the same line
        else
        {
            //the Y difference is less than half the line height
            int dy = a2.getBounds().getY1() - a1.getBounds().getY2();
            if (dy < 0)
                dy = a1.getBounds().getY1() - a2.getBounds().getY2();
            return dy < a1.getBounds().getHeight() / 2;
        }
    }
    
    /**
     * Checks if the given areas are on the same line.
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isOnSameLine(Area a1, Area a2)
    {
        return isOnSameLine(a1.getBounds(), a2.getBounds());
    }
    
    /**
     * Checks if the given rectangles are on the same line.
     * @param r1
     * @param r2
     * @return
     */
    public static boolean isOnSameLine(Rectangular r1, Rectangular r2)
    {
        Rectangular tall, shrt;
        if (r2.getHeight() > r1.getHeight())
        {
            tall = r2; shrt = r1;
        }
        else
        {
            tall = r1; shrt = r2;
        }
        
        return (tall.getY1() < shrt.midY() && tall.getY2() > shrt.midY());
    }
    
    /**
     * Checks if the given areas are in the same column.
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isInSameColumn(Area a1, Area a2, AreaTopology topology)
    {
        final Rectangular gp1 = topology.getPosition(a1);
        final Rectangular gp2 = topology.getPosition(a2);
        return (gp1.getX1() == gp2.getX1()); 
    }
    
    /**
     * Checks if the given areas are aligned in row or column.
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isAligned(Area a1, Area a2, AreaTopology topology)
    {
        final Rectangular gp1 = topology.getPosition(a1);
        final Rectangular gp2 = topology.getPosition(a2);
        return ((gp1.getX1() == gp2.getX1()) //x-aligned
                || (gp1.getY1() == gp2.getY1())); //y-aligned
    }
    
    /**
     * Checks if the given area has a target URL assigned (it acts as a link)
     * @param a
     * @return
     */
    public static boolean isLink(Area a)
    {
        for (Box box : a.getBoxes())
        {
            if (box.getAttribute("href") != null)
                return true;
        }
        return false;
    }

    /**
     * Scans a list of areas and finds the areas that are befor or after a given area on the same line.
     * @param a the given area
     * @param topology the topology to be used for comparing the area positions
     * @param destBefore the destination collection of areas before the given area
     * @param destAfter the destination collection of areas after the given area
     */
    public static void findAreasBeforeAfter(Area a, AreaTopology topology, List<Area> destBefore, List<Area> destAfter)
    {
        Rectangular gpa = topology.getPosition(a);
        //find before
        int x = gpa.getX1() - 1;
        boolean breakFound = false;
        while (x >= 0 && !breakFound)
        {
            int minX = x;
            for (int y = gpa.getY1(); y <= gpa.getY2(); y++)
            {
                Collection<Area> cands = topology.findAllAreasAt(x, y);
                for (Area cand : cands)
                {
                    if (isOnSameLine(cand, a))
                    {
                        destBefore.add(0, a);
                        Rectangular r = topology.getPosition(cand);
                        minX = Math.min(minX, r.getX1()); 
                    }
                    else
                        breakFound = true; // found some area breaking the line
                }
            }
            x = minX - 1; 
        }
        //find after
        x = gpa.getX2() + 1;
        breakFound = false;
        while (x < topology.getTopologyWidth() && !breakFound)
        {
            int maxX = x;
            for (int y = gpa.getY1(); y <= gpa.getY2(); y++)
            {
                Collection<Area> cands = topology.findAllAreasAt(x, y);
                for (Area cand : cands)
                {
                    if (isOnSameLine(cand, a))
                    {
                        destAfter.add(a);
                        Rectangular r = topology.getPosition(cand);
                        maxX = Math.max(maxX, r.getX2());
                    }
                    else
                        breakFound = true; // found some area breaking the line
                }
            }
            x = maxX + 1; 
        }
    }
    
    public static void findAreasBelow(Area a, AreaTopology topology, List<Area> destBelow)
    {
        Rectangular gpa = topology.getPosition(a);
        
        int y = gpa.getY2() + 1;
        boolean found = false;
        while (y < topology.getTopologyHeight() && !found)
        {
            int x = gpa.getX1();
            while (x <= gpa.getX2())
            {
                int maxX = x;
                Collection<Area> cands = topology.findAllAreasAt(x, y);
                if (!cands.isEmpty())
                {
                    for (Area cand : cands)
                    {
                        destBelow.add(cand);
                        Rectangular r = topology.getPosition(cand);
                        maxX = Math.max(maxX, r.getX2());
                    }
                    found = true;
                }
                x = maxX + 1;
            }
            y++;
        }
    }
    
    /**
     * Checks if a given area overlaps with any other area contained in a given collection
     * @param a the area 
     * @param col the collection of other areas for checking overlaps
     * @return true if the area a overlaps with any area from col
     */
    public static boolean intersectsWithAny(Area a, Collection<Area> col)
    {
        for (Area b : col)
        {
            if (a.getBounds().intersects(b.getBounds()))
                return true;
        }
        return false;
    }
    
    public static float statRound(float value)
    {
        final float f = 100000f;
        return Math.round(value * f) / f;
    }

}

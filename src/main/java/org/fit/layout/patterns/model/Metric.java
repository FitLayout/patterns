/**
 * Metric.java
 *
 * Created on 14. 12. 2017, 17:23:10 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * AreaConnection metrics.
 * @author burgetr
 */
public interface Metric
{

    /**
     * Computes the value of the metric for the given set of area connections that are supposed
     * to belong to a particular match.
     * @param cons The set of area connections.
     * @return the metric value
     */
    public float compute(Set<AreaConnection> cons);

    //==================================================================================================
    
    /**
     * A base metric that has a name assigned.
     * 
     * @author burgetr
     */
    abstract public class BaseMetric implements Metric
    {
        private String name;
        
        public BaseMetric(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return name;
        }
        
        @Override
        public String toString()
        {
            return getName();
        }
        
        /**
         * Rounds the pixel distance value to multiples of two for avoiding one-pxel errors
         * that may be caused by rendering.  
         * @param val the value to be rounded
         * @return the rounded result
         */
        protected int pixelRound(int val)
        {
            return val - val % 2;
        }
        
        protected int findMax(Set<AreaConnection> cons, ToIntFunction<AreaConnection> mapper)
        {
            return cons.stream().mapToInt(mapper).max().orElse(0); 
        }
        
        protected int findMin(Set<AreaConnection> cons, ToIntFunction<AreaConnection> mapper)
        {
            return cons.stream().mapToInt(mapper).min().orElse(0); 
        }
        
    }
    
    abstract public class WidthMetric extends BaseMetric
    {
        public enum Dir { LEFT, CENTER, RIGHT };
        
        public WidthMetric(String name)
        {
            super(name);
        }
        
        /**
         * Computes the minimal horizontal distance between the specified point of the first area and specified
         * point of the second area within the whole set of area connections.
         * @param d1 the point in the first area (LEFT, CENTER or RIGHT)
         * @param d2 the point in the second area (LEFT, CENTER or RIGHT)
         * @param cons the set of area connections to be evaluated
         * @return the minimal horizontal distance
         */
        protected float computeMinDist(Dir d1, Dir d2, Set<AreaConnection> cons)
        {
            final AreaConnection first = cons.iterator().next();
            final int ref;
            switch (d1)
            {
                case LEFT:
                    ref = first.getA2().getX1();
                    break;
                case CENTER:
                    ref = (first.getA2().getX1() + first.getA2().getX2()) / 2;
                    break;
                case RIGHT:
                    ref = first.getA2().getX2();
                    break;
                default:
                    ref = 0;
            }
            final ToIntFunction<AreaConnection> mapper;
            switch (d2)
            {
                case LEFT:
                    mapper = c -> pixelRound(c.getA1().getX1() - ref);
                    break;
                case CENTER:
                    mapper = c -> pixelRound(((c.getA1().getX1() + c.getA1().getX2()) / 2) - ref);
                    break;
                case RIGHT:
                    mapper = c -> pixelRound(c.getA1().getX2() - ref);
                    break;
                default:
                    mapper = null;
            }
            int minDist = findMin(cons, mapper); //for positive distances, we need the minimal value
            int maxDist = findMax(cons, mapper); //if the distances are negative, we want the maximal value
            //return the one with the minimal absolute value
            return (Math.abs(minDist) < Math.abs(maxDist)) ? minDist : maxDist;
        }
    }
    
    //==================================================================================================
    
    /**
     * Represents the minimal horizontal distance between the left edges of the connected areas within the set.
     */
    public static final Metric widthLL = new WidthMetric("widthLL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.LEFT, Dir.LEFT, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the left edge of the first area and the center of the second area.
     */
    public static final Metric widthLC = new WidthMetric("widthLC")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.LEFT, Dir.CENTER, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the left edge of the first area and the right edge of the second area.
     */
    public static final Metric widthLR = new WidthMetric("widthLR")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.LEFT, Dir.RIGHT, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the center of the first area and the left edge of the second area.
     */
    public static final Metric widthCL = new WidthMetric("widthCL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.CENTER, Dir.LEFT, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the centers of the connected areas within the set.
     */
    public static final Metric widthCC = new WidthMetric("widthCC")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.CENTER, Dir.CENTER, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the center of the first area and the right edge of the second area.
     */
    public static final Metric widthCR = new WidthMetric("widthCR")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.CENTER, Dir.RIGHT, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the right edge of the first area and the left edge of the second area.
     */
    public static final Metric widthRL = new WidthMetric("widthRL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.RIGHT, Dir.LEFT, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the right edge of the first area and the center of the second area.
     */
    public static final Metric widthRC = new WidthMetric("widthRC")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.RIGHT, Dir.CENTER, cons);
        }
    };
    
    /**
     * Represents the minimal horizontal distance between the right edges of the connected areas.
     */
    public static final Metric widthRR = new WidthMetric("widthRR")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.RIGHT, Dir.RIGHT, cons);
        }
    };
    
    /**
     * Represents the minimal vertical distance between the baselines of the connected areas.
     */
    public static final Metric heightBB = new BaseMetric("heightBB")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            AreaConnection first = cons.iterator().next();
            int ref = first.getA2().getY2();
            int minDif = findMin(cons, c -> pixelRound(c.getA1().getY2() - ref)); //for positive distances, we need the minimal value
            int maxDif = findMax(cons, c -> pixelRound(c.getA1().getY2() - ref)); //if the distances are negative, we want the maximal value
            //return the one with the minimal absolute value
            return (Math.abs(minDif) < Math.abs(maxDif)) ? minDif : maxDif;
        }
    };
    
   

}

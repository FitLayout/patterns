/**
 * Metric.java
 *
 * Created on 14. 12. 2017, 17:23:10 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.Collections;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AreaConnection metrics.
 * @author burgetr
 */
public interface Metric
{

    public float compute(Set<AreaConnection> cons);

    //==================================================================================================
    
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
        
        protected float computeMinDist(Dir d1, Dir d2, Set<AreaConnection> cons)
        {
            final AreaConnection first = cons.iterator().next();
            final int ref;
            switch (d1)
            {
                case LEFT:
                    ref = first.getA1().getX1();
                    break;
                case CENTER:
                    ref = (first.getA1().getX1() + first.getA1().getX2()) / 2;
                    break;
                case RIGHT:
                    ref = first.getA1().getX2();
                    break;
                default:
                    ref = 0;
            }
            final ToIntFunction<AreaConnection> mapper;
            switch (d2)
            {
                case LEFT:
                    mapper = c -> c.getA2().getX1() - ref;
                    break;
                case CENTER:
                    mapper = c -> ((c.getA2().getX1() + c.getA2().getX2()) / 2) - ref;
                    break;
                case RIGHT:
                    mapper = c -> c.getA2().getX2() - ref;
                    break;
                default:
                    mapper = null;
            }
            return findMin(cons, mapper);
        }
    }
    
    //==================================================================================================
    
    public static final Set<Metric> widthMetrics = Collections.unmodifiableSet(Stream.of(
            Metric.widthLL,
            Metric.widthLC,
            Metric.widthLR,
            Metric.widthCL,
            Metric.widthCC,
            Metric.widthCR,
            Metric.widthRL,
            Metric.widthRC,
            Metric.widthRR
            ).collect(Collectors.toSet()));
    
    //==================================================================================================
    
    public static final Metric widthLL = new WidthMetric("widthLL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.LEFT, Dir.LEFT, cons);
        }
    };
    
    public static final Metric widthLC = new WidthMetric("widthLC")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.LEFT, Dir.CENTER, cons);
        }
    };
    
    public static final Metric widthLR = new WidthMetric("widthLR")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.LEFT, Dir.RIGHT, cons);
        }
    };
    
    public static final Metric widthCL = new WidthMetric("widthCL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.CENTER, Dir.LEFT, cons);
        }
    };
    
    public static final Metric widthCC = new WidthMetric("widthCC")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.CENTER, Dir.CENTER, cons);
        }
    };
    
    public static final Metric widthCR = new WidthMetric("widthCR")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.CENTER, Dir.RIGHT, cons);
        }
    };
    
    public static final Metric widthRL = new WidthMetric("widthRL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.RIGHT, Dir.LEFT, cons);
        }
    };
    
    public static final Metric widthRC = new WidthMetric("widthRC")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.RIGHT, Dir.CENTER, cons);
        }
    };
    
    public static final Metric widthRR = new WidthMetric("widthRR")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return computeMinDist(Dir.RIGHT, Dir.RIGHT, cons);
        }
    };
    
    public static final Metric heightBB = new BaseMetric("widthBB")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            AreaConnection first = cons.iterator().next();
            int ref = first.getA1().getY2();
            int minDif = findMin(cons, c -> c.getA2().getY2() - ref);
            return minDif;
        }
    };
    
   

}

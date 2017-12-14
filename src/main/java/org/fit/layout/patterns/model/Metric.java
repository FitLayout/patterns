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
    
    //==================================================================================================
    
    public static final Metric widthLL = new BaseMetric("widthLL")
    {
        @Override
        public float compute(Set<AreaConnection> cons)
        {
            return findMax(cons, c -> c.getA2().getX1());
        }
    };
    
   

}

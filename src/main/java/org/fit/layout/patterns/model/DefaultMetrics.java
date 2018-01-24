/**
 * DefaultMetrics.java
 *
 * Created on 24. 1. 2018, 22:49:23 by burgetr
 */
package org.fit.layout.patterns.model;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default sets of metrics that are frequently used.
 * 
 * @author burgetr
 */
public class DefaultMetrics
{

    public static final Set<Metric> widthMetrics = Collections
            .unmodifiableSet(Stream
                    .of(Metric.widthLL, Metric.widthLC, Metric.widthLR,
                            Metric.widthCL, Metric.widthCC, Metric.widthCR,
                            Metric.widthRL, Metric.widthRC, Metric.widthRR)
                    .collect(Collectors.toSet()));
    
    public static final Set<Metric> heightMetrics = Collections.unmodifiableSet(
            Stream.of(Metric.heightBB).collect(Collectors.toSet()));

}

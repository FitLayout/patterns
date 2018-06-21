/**
 * PresentationStats.java
 *
 * Created on 21. 6. 2018, 10:59:23 by burgetr
 */
package org.fit.layout.patterns.model;

import org.fit.layout.model.Tag;
import org.fit.layout.patterns.Relation;
import org.fit.layout.patterns.RelationAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the statistics about the visual presentation of a given tag and relationship.
 * 
 * @author burgetr
 */
public class PresentationStats
{
    private static Logger log = LoggerFactory.getLogger(PresentationStats.class);

    private Tag tag;
    private Relation rel;
    private RelationAnalyzer ra;
    
    public PresentationStats(Tag tag, Relation rel, RelationAnalyzer ra)
    {
        this.tag = tag;
        this.rel = rel;
        this.ra = ra;
    }
    
    

}

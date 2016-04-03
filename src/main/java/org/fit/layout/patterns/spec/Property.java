/**
 * Property.java
 *
 * Created on 2. 4. 2016, 23:37:19 by burgetr
 */
package org.fit.layout.patterns.spec;

import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class Property
{
    public enum Cardinality { ONE, MANY }
    
    private String name;
    private Cardinality cardinality;
    private Tag sourceTag;
    
    public Property(String name, Cardinality cardinality, Tag sourceTag)
    {
        this.name = name;
        this.cardinality = cardinality;
        this.sourceTag = sourceTag;
    }

    public String getName()
    {
        return name;
    }

    public Cardinality getCardinality()
    {
        return cardinality;
    }

    public Tag getSourceTag()
    {
        return sourceTag;
    }
    
}

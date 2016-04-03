/**
 * Entity.java
 *
 * Created on 2. 4. 2016, 23:38:15 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author burgetr
 */
public class Entity
{
    private String name;
    private List<Property> properties;
    
    public Entity(String name)
    {
        this.name = name;
        this.properties = new ArrayList<>();
    }

    public String getName()
    {
        return name;
    }

    public List<Property> getProperties()
    {
        return properties;
    }

    public void addProperty(Property property)
    {
        properties.add(property);
    }
}

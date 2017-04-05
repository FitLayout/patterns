/**
 * RDFTag.java
 *
 * Created on 5. 4. 2017, 16:39:04 by burgetr
 */
package org.fit.layout.patterns.spec;

import org.fit.layout.impl.DefaultTag;

/**
 * A tag mapped to a RDF resource.
 * 
 * @author burgetr
 */
public class RDFTag extends DefaultTag
{
    private String uri;
    
    
    public RDFTag(String name, String uri)
    {
        super("RDF", name);
        this.uri = uri;
    }

    public String getUri()
    {
        return uri;
    }
    
}

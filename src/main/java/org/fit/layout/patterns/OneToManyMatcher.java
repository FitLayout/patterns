/**
 * OneToManyMatcher.java
 *
 * Created on 28. 2. 2016, 17:07:22 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public class OneToManyMatcher
{
    private Tag srcTag1;
    private Tag srcTag2;
    private boolean fixedOrder;
    
    
    public OneToManyMatcher(Tag srcTag1, Tag srcTag2, boolean fixedOrder)
    {
        this.srcTag1 = srcTag1;
        this.srcTag2 = srcTag2;
        this.fixedOrder = fixedOrder;
    }
    
    
    
    
    

}

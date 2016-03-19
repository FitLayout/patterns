/**
 * StyleAnalyzer.java
 *
 * Created on 19. 3. 2016, 14:27:32 by burgetr
 */
package org.fit.layout.patterns;

import java.util.Set;

import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;

/**
 * A style analyzer that is able to assign a tag or tags to
 * the given area based on its visual style.
 * @author burgetr
 */
public interface StyleAnalyzer
{

    public Set<Tag> inferTags(Area a);
    
}

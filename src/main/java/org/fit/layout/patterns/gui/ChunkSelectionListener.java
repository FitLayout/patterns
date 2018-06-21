/**
 * ChunkSelectionListener.java
 *
 * Created on 21. 6. 2018, 12:47:11 by burgetr
 */
package org.fit.layout.patterns.gui;

import org.fit.layout.model.Area;

/**
 * A listener that can be called when the source chunk selection changes in the SourceAreasPlugin panel.
 * 
 * @author burgetr
 */
public interface ChunkSelectionListener
{
    
    /**
     * This method is called when the source chunk selection changes in the browser.
     * @param area The new selected area.
     */
    public void chunkSelected(Area area);

}

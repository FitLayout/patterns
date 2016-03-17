/**
 * LogicalAreaGroup.java
 *
 * Created on 17. 3. 2016, 14:44:58 by burgetr
 */
package org.fit.layout.patterns;

import org.fit.layout.impl.DefaultArea;
import org.fit.layout.model.Area;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.Rectangular;

/**
 * An area that is actually created by a group of areas represented
 * by a logical area. 
 * @author burgetr
 */
public class LogicalAreaGroup extends DefaultArea
{
    private LogicalArea srcArea;
    
    
    public LogicalAreaGroup(LogicalArea src)
    {
        super(computeTotalBounds(src));
        srcArea = src;
        addTag(srcArea.getMainTag(), 1.0f);
        computeAverages();
    }
    
    @Override
    public String getText()
    {
        return srcArea.getText();
    }

    @Override
    public String getText(String separator)
    {
        return srcArea.getText();
    }

    //=====================================================================================

    private void computeAverages()
    {
        float fsize = 0;
        float fweight = 0;
        float fstyle = 0;
        
        for (Area a : srcArea.getAreas())
        {
            fsize += a.getFontSize();
            fweight += a.getFontWeight();
            fstyle += a.getFontStyle();
        }
        int cnt = srcArea.getAreas().size();
        if (cnt > 0)
        {
            setFontSize(fsize / cnt);
            setFontWeight(fweight / cnt);
            setFontStyle(fstyle / cnt);
        }
    }
    
    private static Rectangular computeTotalBounds(LogicalArea srcArea)
    {
        Rectangular ret = null;
        for (Area a : srcArea.getAreas())
        {
            if (ret == null)
                ret = new Rectangular(a.getBounds());
            else
                ret.expandToEnclose(a.getBounds());
        }
        return (ret == null) ? new Rectangular() : ret;
    }
    
}

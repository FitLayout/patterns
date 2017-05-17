/**
 * PatternsPlugin.java
 *
 * Created on 17. 5. 2017, 23:12:40 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.fit.layout.api.LogicalTreeProvider;
import org.fit.layout.api.ServiceManager;
import org.fit.layout.gui.Browser;
import org.fit.layout.gui.BrowserPlugin;
import org.fit.layout.gui.GUIUpdateListener;
import org.fit.layout.gui.GUIUpdateSource;
import org.fit.layout.gui.TreeListener;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author burgetr
 */
public class PatternsPlugin implements BrowserPlugin, GUIUpdateSource, TreeListener
{
    private static Logger log = LoggerFactory.getLogger(PatternsPlugin.class);
    
    private Browser browser;
    private List<GUIUpdateListener> updateListeners;
    private PatternBasedLogicalProvider provider;
    
    private JPanel pnl_main;
    
    
	//=============================
    
    /**
     * @wbp.parser.entryPoint
     */
    public boolean init(Browser browser)
    {
        this.browser = browser;
        this.browser.addToolPanel("Patterns", getPnl_main());
        this.browser.addTreeListener(this);
        updateListeners = new ArrayList<GUIUpdateListener>();
        
        Map<String, LogicalTreeProvider> providers = ServiceManager.findLogicalTreeProviders();
        for (LogicalTreeProvider p : providers.values())
        {
            if (p instanceof PatternBasedLogicalProvider)
            {
                provider = (PatternBasedLogicalProvider) p;
                log.info("Found logical provider: {}", p);
            }
        }
        
        return true;
    }
    
    @Override
    public void pageRendered(Page page)
    {
    }

    @Override
    public void areaTreeUpdated(AreaTree tree)
    {
    }

    @Override
    public void logicalAreaTreeUpdated(LogicalAreaTree tree)
    {
    }

    @Override
    public void registerGUIUpdateListener(GUIUpdateListener listener)
    {
        updateListeners.add(listener);
    }
    
    private JPanel getPnl_main() 
    {
    	
    	 if (pnl_main == null) {
    		 
             pnl_main = new JPanel();
             GridBagLayout gbl_main = new GridBagLayout();
             gbl_main.columnWeights = new double[] { 0.0, 0.0, 0.0 };
             gbl_main.rowWeights = new double[] { 0.0, 0.0 };
             pnl_main.setLayout(gbl_main);
         }
         return pnl_main;
    }
    
}

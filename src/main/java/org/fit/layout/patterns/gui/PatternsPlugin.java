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
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Page;
import org.fit.layout.patterns.AttributeGroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


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
    private JButton btnAutoConfig;
    private JList<AttributeGroupMatcher.Configuration> configList;
    private JScrollPane configScroll;
    
    
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
    
    //==============================================================================================

    private void autoConfig()
    {
        if (provider != null)
        {
            AreaTree areaTree = browser.getAreaTree();
            List<Area> leaves = new ArrayList<Area>();
            findLeaves(areaTree.getRoot(), leaves);
            
            provider.getMatcher().configure(leaves);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    DefaultListModel<AttributeGroupMatcher.Configuration> model = new DefaultListModel<>();
                    for (AttributeGroupMatcher.Configuration conf : provider.getMatcher().getBestConfigurations())
                        model.addElement(conf);
                    getConfigList().setModel(model);
                    getConfigList().setSelectedIndex(0);
                }
            });
        }
    }
    
    private void findLeaves(Area root, List<Area> dest)
    {
        if (root.isLeaf())
        {
            if (!root.isSeparator())
                dest.add(root);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                findLeaves(root.getChildArea(i), dest);
        }
    }

    
    //==============================================================================================
    
    private JPanel getPnl_main() 
    {
    	
    	 if (pnl_main == null) {
    		 
             pnl_main = new JPanel();
             GridBagLayout gbl_main = new GridBagLayout();
             gbl_main.columnWeights = new double[] { 1.0};
             gbl_main.rowWeights = new double[] { 0.0, 1.0};
             pnl_main.setLayout(gbl_main);
             GridBagConstraints gbc_btnAutoConfig = new GridBagConstraints();
             gbc_btnAutoConfig.insets = new Insets(0, 0, 5, 0);
             gbc_btnAutoConfig.gridx = 0;
             gbc_btnAutoConfig.gridy = 0;
             pnl_main.add(getBtnAutoConfig(), gbc_btnAutoConfig);
             GridBagConstraints gbc_configScroll = new GridBagConstraints();
             gbc_configScroll.fill = GridBagConstraints.BOTH;
             gbc_configScroll.insets = new Insets(0, 0, 0, 5);
             gbc_configScroll.gridx = 0;
             gbc_configScroll.gridy = 1;
             pnl_main.add(getConfigScroll(), gbc_configScroll);
         }
         return pnl_main;
    }
    
    private JButton getBtnAutoConfig() {
        if (btnAutoConfig == null) {
        	btnAutoConfig = new JButton("Auto config");
        	btnAutoConfig.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    Thread t = new Thread()
                    {
                        public void run()
                        {
                            autoConfig();
                        }
                    };
                    t.start();
                }
        	});
        }
        return btnAutoConfig;
    }
    
    private JList<AttributeGroupMatcher.Configuration> getConfigList() 
    {
        if (configList == null) {
        	configList = new JList<AttributeGroupMatcher.Configuration>();
        	configList.addListSelectionListener(new ListSelectionListener() {
        	    public void valueChanged(ListSelectionEvent arg0) {
        	        int index = configList.getSelectedIndex();
        	        provider.getMatcher().setUsedConf(index);
        	    }
        	});
        }
        return configList;
    }
    
    private JScrollPane getConfigScroll() 
    {
        if (configScroll == null) {
        	configScroll = new JScrollPane();
        	configScroll.setViewportView(getConfigList());
        }
        return configScroll;
    }
}

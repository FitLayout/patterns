/**
 * PatternsPlugin.java
 *
 * Created on 17. 5. 2017, 23:12:40 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.fit.layout.api.ServiceManager;
import org.fit.layout.gui.Browser;
import org.fit.layout.gui.BrowserPlugin;
import org.fit.layout.gui.GUIUpdateListener;
import org.fit.layout.gui.GUIUpdateSource;
import org.fit.layout.gui.TreeListener;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Page;
import org.fit.layout.patterns.AttributeGroupMatcher;
import org.fit.layout.patterns.model.MatcherConfiguration;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JLabel;


/**
 * 
 * @author burgetr
 */
public class PatternsPlugin implements BrowserPlugin, GUIUpdateSource, TreeListener
{
    public static final float MIN_TAG_SUPPORT = 0.25f;
    
    private static Logger log = LoggerFactory.getLogger(PatternsPlugin.class);
        
    private Browser browser;
    private List<GUIUpdateListener> updateListeners;
    private PatternBasedLogicalProvider provider;
    private int currentMatcher;
    
    private JPanel pnl_main;
    private JButton btnAutoConfig;
    private JList<MatcherConfiguration> configList;
    private JScrollPane configScroll;
    private JPanel toolPanel;
    private JButton btnPrev;
    private JLabel lblMatcherlabel;
    private JButton btnNext;
    
    
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
        
        provider = ServiceManager.findByClass(ServiceManager.findLogicalTreeProviders().values(), PatternBasedLogicalProvider.class);
        if (provider != null)
        {
            log.info("Found logical provider: {}", provider);
            selectMatcher(0);
            return true;
        }
        else
            return false;
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
    
    private void notifyGUIListeners()
    {
        for (GUIUpdateListener listener : updateListeners)
            listener.updateGUI();
    }
    
    public void selectMatcher(int index)
    {
        if (index < 0)
            index = 0;
        if (index >= provider.getMatchers().size())
            index = provider.getMatchers().size() - 1;
        currentMatcher = index;
        
        if (currentMatcher != -1)
        {
            AttributeGroupMatcher m = provider.getMatchers().get(currentMatcher);
            getLblMatcherlabel().setText(m.toString() + " (" + (currentMatcher + 1) + " / " + provider.getMatchers().size() + ")");
            updateConfigList();
        }
        else
            getLblMatcherlabel().setText("---");
    }
    
    public int getCurrentMatcher()
    {
        return currentMatcher;
    }
    
    //==============================================================================================

    private void autoConfig()
    {
        if (provider != null && browser.getAreaTree() != null)
        {
            provider.configureMatcher(provider.getMatchers().get(currentMatcher), browser.getAreaTree().getRoot());
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateConfigList();
                    notifyGUIListeners();
                }
            });
        }
    }
    
    private void updateConfigList()
    {
        DefaultListModel<MatcherConfiguration> model = new DefaultListModel<>();
        List<MatcherConfiguration> conflist = provider.getMatchers().get(currentMatcher).getBestConfigurations();
        if (conflist != null)
        {
            for (MatcherConfiguration conf : conflist)
                model.addElement(conf);
        }
        getConfigList().setModel(model);
        
        MatcherConfiguration used = provider.getMatchers().get(currentMatcher).getUsedConf();
        if (used != null && conflist.indexOf(used) != -1)
            getConfigList().setSelectedIndex(conflist.indexOf(used));
        else
            getConfigList().setSelectedIndex(0);
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
             GridBagConstraints gbc_toolPanel = new GridBagConstraints();
             gbc_toolPanel.fill = GridBagConstraints.BOTH;
             gbc_toolPanel.insets = new Insets(0, 0, 5, 0);
             gbc_toolPanel.gridx = 0;
             gbc_toolPanel.gridy = 0;
             pnl_main.add(getToolPanel(), gbc_toolPanel);
             GridBagConstraints gbc_configScroll = new GridBagConstraints();
             gbc_configScroll.fill = GridBagConstraints.BOTH;
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
    
    private JList<MatcherConfiguration> getConfigList() 
    {
        if (configList == null) {
        	configList = new JList<MatcherConfiguration>();
        	configList.addListSelectionListener(new ListSelectionListener() {
        	    public void valueChanged(ListSelectionEvent arg0) {
        	        int index = configList.getSelectedIndex();
        	        provider.getMatchers().get(currentMatcher).setUsedConf(index);
        	        notifyGUIListeners();
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
    
    private JPanel getToolPanel() {
        if (toolPanel == null) {
        	toolPanel = new JPanel();
        	GridBagLayout gbl_toolPanel = new GridBagLayout();
        	gbl_toolPanel.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, 1.0};
        	gbl_toolPanel.rowWeights = new double[]{0.0};
        	toolPanel.setLayout(gbl_toolPanel);
        	GridBagConstraints gbc_btnPrev = new GridBagConstraints();
        	gbc_btnPrev.anchor = GridBagConstraints.WEST;
        	gbc_btnPrev.insets = new Insets(0, 0, 5, 0);
        	gbc_btnPrev.gridx = 0;
        	gbc_btnPrev.gridy = 0;
        	toolPanel.add(getBtnPrev(), gbc_btnPrev);
        	GridBagConstraints gbc_lblMatcherlabel = new GridBagConstraints();
        	gbc_lblMatcherlabel.anchor = GridBagConstraints.WEST;
        	gbc_lblMatcherlabel.insets = new Insets(0, 5, 5, 5);
        	gbc_lblMatcherlabel.gridx = 1;
        	gbc_lblMatcherlabel.gridy = 0;
        	toolPanel.add(getLblMatcherlabel(), gbc_lblMatcherlabel);
        	GridBagConstraints gbc_btnNext = new GridBagConstraints();
        	gbc_btnNext.anchor = GridBagConstraints.WEST;
        	gbc_btnNext.gridx = 2;
        	gbc_btnNext.gridy = 0;
        	toolPanel.add(getBtnNext(), gbc_btnNext);
            GridBagConstraints gbc_btnAutoConfig = new GridBagConstraints();
            gbc_btnAutoConfig.anchor = GridBagConstraints.WEST;
            gbc_btnAutoConfig.insets = new Insets(0, 10, 5, 0);
            gbc_btnAutoConfig.gridx = 3;
            gbc_btnAutoConfig.gridy = 0;
            toolPanel.add(getBtnAutoConfig(), gbc_btnAutoConfig);
        }
        return toolPanel;
    }
    
    private JButton getBtnPrev() {
        if (btnPrev == null) {
        	btnPrev = new JButton("<");
        	btnPrev.addActionListener(new ActionListener() {
        	    public void actionPerformed(ActionEvent e) {
        	        selectMatcher(currentMatcher - 1);
        	    }
        	});
        }
        return btnPrev;
    }
    
    private JLabel getLblMatcherlabel() {
        if (lblMatcherlabel == null) {
        	lblMatcherlabel = new JLabel("matcherLabel");
        }
        return lblMatcherlabel;
    }
    
    private JButton getBtnNext() {
        if (btnNext == null) {
        	btnNext = new JButton(">");
        	btnNext.addActionListener(new ActionListener() {
        	    public void actionPerformed(ActionEvent e) {
                    selectMatcher(currentMatcher + 1);
        	    }
        	});
        }
        return btnNext;
    }
}

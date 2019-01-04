/**
 * RelationsPlugin.java
 *
 * Created on 6. 12. 2017, 15:52:31 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import org.fit.layout.api.ServiceManager;
import org.fit.layout.gui.AreaSelectionListener;
import org.fit.layout.gui.Browser;
import org.fit.layout.gui.BrowserPlugin;
import org.fit.layout.model.Area;
import org.fit.layout.patterns.AttributeGroupMatcher;
import org.fit.layout.patterns.Relation;
import org.fit.layout.patterns.RelationAnalyzer;
import org.fit.layout.patterns.chunks.ChunksSource;
import org.fit.layout.patterns.model.AreaConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 
 * @author burgetr
 */
public class RelationsPlugin implements BrowserPlugin, AreaSelectionListener, ChunkSelectionListener
{
    private static Logger log = LoggerFactory.getLogger(RelationsPlugin.class);
    
    private Browser browser;
    private PatternBasedLogicalProvider provider;
    private AttributeGroupMatcher matcher;
    private RelationAnalyzer pa;
    private Area selectedArea;
    
    private JPanel pnl_main;
    private JPanel toolPanel;
    private JScrollPane listScroll;
    private JList<AreaConnection> relationList;
    private JLabel lblRelation;
    private JComboBox<Relation> relationCombo;
    private JCheckBox chckbxOutgoing;


    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public boolean init(Browser browser)
    {
        this.browser = browser;
        this.browser.addToolPanel("Relations", getPnl_main());
        this.browser.addAreaSelectionListener(this);
        
        provider = ServiceManager.findByClass(ServiceManager.findLogicalTreeProviders().values(), PatternBasedLogicalProvider.class);
        if (provider != null)
        {
            log.info("Found logical provider: {}", provider);
            if (provider.getMatchers().size() > 0)
                matcher = provider.getMatchers().get(provider.getMatchers().size() - 1);
        }
        else
            return false;
        
        SourceAreasPlugin sources = ServiceManager.findByClass(ServiceManager.findBrowserPlugins(), SourceAreasPlugin.class);
        if (sources != null)
        {
            log.info("Found source areas plugin: {}", sources);
            sources.addChunkSelectionListener(this);
        }
        else
            return false;
        
        return true;
    }

    @Override
    public void areaSelected(Area area)
    {
        selectedArea = area;
        if (matcher != null && matcher.getUsedConf() != null)
        {
            if (pa == null)
            {
                pa = getCurrentPA();
                fillRelationsCombo(pa.getAnalyzedRelations());
            }
            updateConnectionList(selectedArea, pa);
        }
        else
            log.info("No matcher");
    }
    
    @Override
    public void chunkSelected(Area area)
    {
        selectedArea = area;
        if (matcher != null && matcher.getUsedConf() != null)
        {
            if (pa == null)
            {
                pa = getCurrentPA();
                fillRelationsCombo(pa.getAnalyzedRelations());
            }
            updateConnectionList(selectedArea, pa);
        }
        else
            log.info("No matcher");
    }
    
    private RelationAnalyzer getCurrentPA()
    {
        ChunksSource src = matcher.getUsedSource(browser.getAreaTree().getRoot());
        return src.getPA();
    }
    
    private void fillRelationsCombo(List<Relation> relations)
    {
        Object sel = getRelationCombo().getSelectedItem();
        getRelationCombo().setModel(new DefaultComboBoxModel<>(new Vector<Relation>(relations)));
        if (sel != null)
            getRelationCombo().setSelectedItem(sel);
    }
    
    private void updateConnectionList(Area a, RelationAnalyzer pa)
    {
        Relation rel = (Relation) getRelationCombo().getSelectedItem();
        if (rel != null)
        {
            boolean out = getChckbxOutgoing().isSelected();
            List<AreaConnection> conns;
            if (!out)
                conns = new ArrayList<>(pa.getConnections(null, rel, a, -1.0f));
            else
                conns = new ArrayList<>(pa.getConnections(a, rel, null, -1.0f));
                
            Collections.sort(conns, new Comparator<AreaConnection>() {
                @Override
                public int compare(AreaConnection o1, AreaConnection o2)
                {
                    if (o2.getWeight() > o1.getWeight())
                        return 1;
                    else if (o2.getWeight() < o1.getWeight())
                        return -1;
                    else
                        return 0;
                }
            });
            DefaultListModel<AreaConnection> model = new DefaultListModel<>();
            for (AreaConnection con : conns)
                model.addElement(con);
            getRelationList().setModel(model);
        }
    }
    
    //==============================================================================================
    
    private JPanel getPnl_main() 
    {
        
         if (pnl_main == null) {
             pnl_main = new JPanel();
             GridBagLayout gbl_main = new GridBagLayout();
             gbl_main.columnWeights = new double[] { 1.0};
             gbl_main.rowWeights = new double[] { 1.0, 1.0};
             pnl_main.setLayout(gbl_main);
             GridBagConstraints gbc_toolPanel = new GridBagConstraints();
             gbc_toolPanel.insets = new Insets(0, 0, 5, 0);
             gbc_toolPanel.fill = GridBagConstraints.BOTH;
             gbc_toolPanel.gridx = 0;
             gbc_toolPanel.gridy = 0;
             pnl_main.add(getToolPanel(), gbc_toolPanel);
             GridBagConstraints gbc_listScroll = new GridBagConstraints();
             gbc_listScroll.weighty = 1.0;
             gbc_listScroll.fill = GridBagConstraints.BOTH;
             gbc_listScroll.gridx = 0;
             gbc_listScroll.gridy = 1;
             pnl_main.add(getListScroll(), gbc_listScroll);
         }
         return pnl_main;
    }
    
    private JPanel getToolPanel() {
        if (toolPanel == null) {
        	toolPanel = new JPanel();
        	GridBagLayout gbl_toolPanel = new GridBagLayout();
        	gbl_toolPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
        	gbl_toolPanel.rowWeights = new double[]{0.0};
        	toolPanel.setLayout(gbl_toolPanel);
        	GridBagConstraints gbc_lblRelation = new GridBagConstraints();
        	gbc_lblRelation.insets = new Insets(0, 5, 0, 5);
        	gbc_lblRelation.gridx = 0;
        	gbc_lblRelation.gridy = 0;
        	toolPanel.add(getLblRelation(), gbc_lblRelation);
        	GridBagConstraints gbc_relationCombo = new GridBagConstraints();
        	gbc_relationCombo.insets = new Insets(0, 0, 5, 0);
        	gbc_relationCombo.gridx = 1;
        	gbc_relationCombo.gridy = 0;
        	toolPanel.add(getRelationCombo(), gbc_relationCombo);
        	GridBagConstraints gbc_chckbxOutgoing = new GridBagConstraints();
        	gbc_chckbxOutgoing.weightx = 1.0;
        	gbc_chckbxOutgoing.fill = GridBagConstraints.HORIZONTAL;
        	gbc_chckbxOutgoing.gridx = 2;
        	gbc_chckbxOutgoing.gridy = 0;
        	toolPanel.add(getChckbxOutgoing(), gbc_chckbxOutgoing);
        }
        return toolPanel;
    }
    
    private JScrollPane getListScroll() {
        if (listScroll == null) {
        	listScroll = new JScrollPane();
        	listScroll.setViewportView(getRelationList());
        }
        return listScroll;
    }
    
    private JList<AreaConnection> getRelationList() {
        if (relationList == null) {
        	relationList = new JList<AreaConnection>();
        }
        return relationList;
    }
    
    private JLabel getLblRelation() {
        if (lblRelation == null) {
        	lblRelation = new JLabel("Relation");
        }
        return lblRelation;
    }
    
    private JComboBox<Relation> getRelationCombo() {
        if (relationCombo == null) {
        	relationCombo = new JComboBox<Relation>();
        	relationCombo.addActionListener(new ActionListener() {
        	    public void actionPerformed(ActionEvent e) {
        	        if (selectedArea != null && pa != null)
        	            updateConnectionList(selectedArea, pa);
        	    }
        	});
        }
        return relationCombo;
    }
    
    private JCheckBox getChckbxOutgoing() {
        if (chckbxOutgoing == null) {
        	chckbxOutgoing = new JCheckBox("Outgoing");
        	chckbxOutgoing.addActionListener(new ActionListener() {
        	    public void actionPerformed(ActionEvent e) {
                    if (selectedArea != null && pa != null)
                        updateConnectionList(selectedArea, pa);
        	    }
        	});
        }
        return chckbxOutgoing;
    }

}

/**
 * SourceAreasPlugin.java
 *
 * Created on 9. 3. 2018, 20:27:03 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.fit.layout.api.ServiceManager;
import org.fit.layout.gui.Browser;
import org.fit.layout.gui.BrowserPlugin;
import org.fit.layout.gui.CanvasClickListener;
import org.fit.layout.gui.GUIUpdateListener;
import org.fit.layout.impl.DefaultTag;
import org.fit.layout.model.Area;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.AttributeGroupMatcher;
import org.fit.layout.patterns.ChunksSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Shows the list of source areas in a browser panel 
 * @author burgetr
 */
public class SourceAreasPlugin implements BrowserPlugin, GUIUpdateListener, CanvasClickListener
{
    private static Logger log = LoggerFactory.getLogger(SourceAreasPlugin.class);
    
    private Browser browser;
    private PatternBasedLogicalProvider provider;
    private PatternsPlugin pp;
    private List<ChunkSelectionListener> chunkSelectionListeners;
    private List<Area> currentAreas;
    private List<Area> filteredAreas;
    private Tag tagAll = new DefaultTag("FitLayout", "ALL"); //the tag used for selecting all

    private JPanel sourceAreasPanel;
    private JScrollPane mainScroll;
    private JList<Area> areaList;
    private JToolBar showToolBar;
    private JButton showSepButton;
    private JPanel toolPanel;
    private JComboBox<Tag> tagCombo;
    

    public SourceAreasPlugin()
    {
        super();
        chunkSelectionListeners = new ArrayList<>();
    }
    
    @Override
    public boolean init(Browser browser)
    {
        this.browser = browser;
        initGui();
        
        provider = ServiceManager.findByClass(ServiceManager.findLogicalTreeProviders().values(), PatternBasedLogicalProvider.class);
        if (provider != null)
        {
            log.info("Found logical provider: {}", provider);
            pp = ServiceManager.findByClass(ServiceManager.findBrowserPlugins(), PatternsPlugin.class);
            if (pp != null)
                pp.registerGUIUpdateListener(this);
        }
        
        return (provider != null && pp != null);
    }
    
    private void initGui()
    {
        browser.addStructurePanel("Chunks", getSourceAreasPanel());
        browser.addToolBar(getShowToolBar());
        browser.addCanvasClickListener("Chunks", this, false);
    }
    
    public void addChunkSelectionListener(ChunkSelectionListener listener)
    {
        chunkSelectionListeners.add(listener);
    }
    
    //========================================================================================
    
    @Override
    public void updateGUI()
    {
        AttributeGroupMatcher m = provider.getMatchers().get(pp.getCurrentMatcher());
        if (m != null)
        {
            ChunksSource source = m.getUsedConf().getSource();
            setAreas(source.getAreas());
        }
        else
            clearAreas();
    }

    private void setAreas(List<Area> areas)
    {
        currentAreas = new ArrayList<>(areas);
        fillTagCombo(areas);
        showFilteredAreas();
    }
    
    private void fillTagCombo(List<Area> areas)
    {
        Set<Tag> tags = new HashSet<>();
        for (Area a : areas)
        {
            for (Tag t : a.getTags().keySet())
                tags.add(t);
        }
        Vector<Tag> tagsv = new Vector<Tag>(tags);
        tagsv.insertElementAt(tagAll, 0);
        Tag current = (Tag) getTagCombo().getSelectedItem();
        getTagCombo().setModel(new DefaultComboBoxModel<>(tagsv));
        if (current != null)
            getTagCombo().setSelectedItem(current);
    }
    
    private void showFilteredAreas()
    {
        Tag filter = (Tag) getTagCombo().getSelectedItem();
        if (filter == null || filter.equals(tagAll))
            filteredAreas = currentAreas;
        else
            filteredAreas = filterAreas(currentAreas, filter);
        showAreas(filteredAreas);
    }
    
    public List<Area> filterAreas(List<Area> areas, Tag tag)
    {
        List<Area> ret = new ArrayList<>();
        for (Area a : areas)
        {
            if (a.hasTag(tag))
                ret.add(a);
        }
        return ret;
    }
    
    private void showAreas(List<Area> areas)
    {
        DefaultListModel<Area> ml = new DefaultListModel<>();
        for (Area a : areas)
            ml.addElement(a);
        areaList.setModel(ml);
    }
    
    private void clearAreas()
    {
        areaList.setModel(new DefaultListModel<>());
    }
    
    private void showArea(Area a)
    {
        browser.getOutputDisplay().drawExtent(a);
        browser.getOutputDisplay().colorizeByTags(a, a.getSupportedTags(PatternsPlugin.MIN_TAG_SUPPORT));
        browser.displayAreaDetails(a);
    }
    
    //========================================================================================
    
    /**
     * @wbp.parser.entryPoint
     */
    private JPanel getSourceAreasPanel()
    {
        if (sourceAreasPanel == null)
        {
            sourceAreasPanel = new JPanel();
            GridBagLayout gbl_sourceAreasPanel = new GridBagLayout();
            gbl_sourceAreasPanel.columnWeights = new double[]{1.0};
            gbl_sourceAreasPanel.rowWeights = new double[]{0.0, 1.0};
            sourceAreasPanel.setLayout(gbl_sourceAreasPanel);
            GridBagConstraints gbc_toolPanel = new GridBagConstraints();
            gbc_toolPanel.insets = new Insets(0, 0, 5, 0);
            gbc_toolPanel.fill = GridBagConstraints.BOTH;
            gbc_toolPanel.gridx = 0;
            gbc_toolPanel.gridy = 0;
            sourceAreasPanel.add(getToolPanel(), gbc_toolPanel);
            GridBagConstraints gbc_mainScroll = new GridBagConstraints();
            gbc_mainScroll.weighty = 1.0;
            gbc_mainScroll.fill = GridBagConstraints.BOTH;
            gbc_mainScroll.gridx = 0;
            gbc_mainScroll.gridy = 1;
            sourceAreasPanel.add(getMainScroll(), gbc_mainScroll);
        }
        return sourceAreasPanel;
    }

    private JScrollPane getMainScroll()
    {
        if (mainScroll == null)
        {
            mainScroll = new JScrollPane();
            mainScroll.setViewportView(getAreaList());
        }
        return mainScroll;
    }
    
    private JPanel getToolPanel()
    {
        if (toolPanel == null)
        {
            toolPanel = new JPanel();
            toolPanel.add(getTagCombo());
        }
        return toolPanel;
    }

    private JComboBox<Tag> getTagCombo()
    {
        if (tagCombo == null)
        {
            tagCombo = new JComboBox<>();
            tagCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showFilteredAreas();
                }
            });
        }
        return tagCombo;
    }

    private JList<Area> getAreaList()
    {
        if (areaList == null)
        {
            areaList = new JList<Area>();
            areaList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
            {
                public void valueChanged(javax.swing.event.ListSelectionEvent e)
                {
                    Area a = areaList.getSelectedValue();
                    if (a != null)
                    {
                        showArea(a);
                        browser.updateDisplay();
                        for (ChunkSelectionListener listener : chunkSelectionListeners)
                            listener.chunkSelected(a);
                    }
                }
            });
        }
        return areaList;
    }

    private JToolBar getShowToolBar()
    {
        if (showToolBar == null)
        {
            showToolBar = new JToolBar("Patterns");
            showToolBar.add(getShowChunksButton());
        }
        return showToolBar;
    }
    
    private JButton getShowChunksButton()
    {
        if (showSepButton == null)
        {
            showSepButton = new JButton();
            showSepButton.setText("Show chunks");
            showSepButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    if (currentAreas != null && filteredAreas != null)
                    {
                        for (Area a : filteredAreas)
                            showArea(a);
                        browser.updateDisplay();
                    }
                }
            });
        }
        return showSepButton;
    }

    //========================================================================================

    @Override
    public void canvasClicked(int x, int y)
    {
        for (Area a : currentAreas)
        {
            if (a.getBounds().contains(x, y))
            {
                getAreaList().setSelectedValue(a, true);
                break;
            }
        }
    }
    
}

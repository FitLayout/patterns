/**
 * SourceAreasPlugin.java
 *
 * Created on 9. 3. 2018, 20:27:03 by burgetr
 */
package org.fit.layout.patterns.gui;

import java.awt.GridLayout;
import java.util.List;

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
import org.fit.layout.model.Area;
import org.fit.layout.patterns.AttributeGroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows the list of source areas in a browser panel 
 * @author burgetr
 */
public class SourceAreasPlugin implements BrowserPlugin, GUIUpdateListener, CanvasClickListener
{
    private static Logger log = LoggerFactory.getLogger(SourceAreasPlugin.class);
    
    private Browser browser;
    private PatternBasedLogicalProvider provider;
    private List<Area> currentAreas;

    private JPanel sourceAreasPanel;
    private JScrollPane mainScroll;
    private JList<Area> areaList;
    private JToolBar showToolBar;
    private JButton showSepButton;
    

    @Override
    public boolean init(Browser browser)
    {
        this.browser = browser;
        initGui();
        
        PatternsPlugin pp = null;
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
    
    //========================================================================================
    
    @Override
    public void updateGUI()
    {
        for (AttributeGroupMatcher m : provider.getMatchers())
        {
            if (m.getSourceAreas() != null)
            {
                currentAreas = m.getSourceAreas();
                showAreas(currentAreas);
                return;
            }
        }
        clearAreas();
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
    
    private JPanel getSourceAreasPanel()
    {
        if (sourceAreasPanel == null)
        {
            GridLayout gl = new GridLayout();
            gl.setRows(1);
            gl.setColumns(1);
            sourceAreasPanel = new JPanel();
            sourceAreasPanel.setLayout(gl);
            sourceAreasPanel.add(getMainScroll(), null);
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
                    if (currentAreas != null)
                    {
                        for (Area a : currentAreas)
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

/**
 * DefaultPatternBasedLogicalProvider.java
 *
 * Created on 11. 10. 2017, 13:26:31 by burgetr
 */
package org.fit.layout.patterns;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fit.layout.impl.BaseLogicalTreeProvider;
import org.fit.layout.impl.DefaultLogicalArea;
import org.fit.layout.impl.DefaultLogicalAreaTree;
import org.fit.layout.impl.DefaultTag;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Tag;
import org.fit.layout.patterns.graph.Graph;
import org.fit.layout.patterns.graph.GraphLoader;
import org.fit.layout.patterns.graph.Node;
import org.fit.layout.patterns.graph.Path;
import org.fit.layout.patterns.gui.PatternBasedLogicalProvider;
import org.fit.layout.patterns.model.Match;
import org.fit.layout.patterns.spec.GraphTaskGenerator;
import org.fit.layout.patterns.spec.RDFTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A common base for pattern-based logical tree providers where the patterns are obtained from an extraction graph. 
 * It implements the graph loading and the basic logical tree creation operations. Final logical tree providers
 * must only implement the specific service interface. 
 * 
 * @author burgetr
 */
public abstract class GraphBasedLogicalProvider extends BaseLogicalTreeProvider implements PatternBasedLogicalProvider
{
    private static Logger log = LoggerFactory.getLogger(GraphBasedLogicalProvider.class);
    
    /** Start auto-configuration of matchers when the logical tree is requested? */
    private boolean autoConfigure = true;
    /** Main tag used for tagging the results */
    private Tag mainTag = new DefaultTag("logical", "match");
    /** Graph specification */
    private Graph graph;
    /** The used group matchers */
    private List<AttributeGroupMatcher> groupMatchers;
    
    
    public GraphBasedLogicalProvider()
    {
        super();
    }
    
    public boolean isAutoConfigure()
    {
        return autoConfigure;
    }

    /**
     * Switches auto-configuration in or off. When set to {@code true} (default), auto-configuration of the matchers
     * will be performed when {@link GraphBasedLogicalProvider#createLogicalTree(AreaTree)} is called and the
     * matchers are not yet configured. When set to {@code false}, only an error message will be displayed
     * and no logical tree will be created when the matchers are not configured. 
     * @param autoConfigure the autoConfigure switch value
     */
    public void setAutoConfigure(boolean autoConfigure)
    {
        this.autoConfigure = autoConfigure;
    }

    /**
     * Gets the current graph used for extraction.
     * @return the currently used graph
     */
    public Graph getGraph()
    {
        return graph;
    }

    /**
     * Sets a new extraction graph. The matchers must be reconfigured after setting a new graph.
     * @param graph the new extraction graph
     */
    public void setGraph(Graph graph)
    {
        this.graph = graph;
        groupMatchers = null; //a new graph requires creating new group matchers
    }
    
    /**
     * Loads a new graph from a JSON description.
     * @param is an input stream from which the JSON specification will be read
     */
    public void loadGraphFromJson(InputStream is)
    {
        GraphLoader gl = new GraphLoader();
        Reader r = new InputStreamReader(is);
        Graph g = gl.loadFromJson(r).collapse();
        log.info("Graph {} : {} ", g.getId(), g.getTitle());
        log.info("Loaded {} nodes {} edges", g.getNodes().size(), g.getEdges().size());
        log.info("Paths:");
        
        Node n = g.getPrimaryNode();
        if (n != null)
        {
            for (Path path : g.getDatatypePathsFrom(n))
                log.info("> {}", path.toString());
        }
        else
            log.error("No primary node specified!");
        
        log.info("Groups:");
        g.getGroups();
        
        setGraph(g);
    }

    @Override
    public void configureMatcher(AttributeGroupMatcher matcher, Area root)
    {
        //autoconfigure the matcher
        matcher.configure(root);
    }

    @Override
    public List<AttributeGroupMatcher> getMatchers()
    {
        if (groupMatchers == null)
        {
            if (getGraph() != null)
            {
                GraphTaskGenerator gen = new GraphTaskGenerator(getGraph());
                log.info("Tags: {}", gen.getAssignedTags());
                
                List<AttributeGroupMatcher> tasks = gen.createTasks();
                System.out.println("Tasks:");
                for (AttributeGroupMatcher m : tasks)
                {
                    System.out.println("    " + m.getAttrs());
                    for (AttributeGroupMatcher dep : m.getDependencies())
                    {
                        String depinfo = "";
                        if (dep.getGroup() != null)
                        {
                            if (dep.getGroup().isSrcMany())
                                depinfo += ">";
                            if (dep.getGroup().isMany())
                                depinfo += "*";
                            if (!dep.getGroup().isRequired())
                                depinfo += "?";
                        }
                        System.out.println("      dep " + dep.getGroup().getRoot() + " " + dep.getAttrs() + depinfo);
                    }
                }
                groupMatchers = tasks;
            }
            else
                log.error("getMatchers() called while no graph is loaded");
        }
        return groupMatchers;
    }

    @Override
    public LogicalAreaTree createLogicalTree(AreaTree areaTree)
    {
        //get the top matcher
        AttributeGroupMatcher matcher = getMatchers().get(getMatchers().size() - 1);
        
        //configure the matcher when not configured yet
        if (matcher.getBestConfigurations() == null)
        {
            if (autoConfigure)
                autoConfigureMatchers(areaTree);
            else
                log.error("Matcher is not configured and auto-configuration is disabled");
        }
        
        //find matches in the area tree
        Collection<Match> matches = matcher.match(areaTree.getRoot());
        if (matches == null)
            matches = Collections.emptyList();
        
        //create the logical tree from the matches
        LogicalArea lroot = new DefaultLogicalArea(areaTree.getRoot());
        addLogicalAreas(matches, matcher, lroot, createTagForMatcher(matcher));
        
        DefaultLogicalAreaTree ret = new DefaultLogicalAreaTree(areaTree);
        ret.setRoot(lroot);
        return ret;
    }

    protected void autoConfigureMatchers(AreaTree areaTree)
    {
        //configure and use the their best configurations for all matchers
        for (AttributeGroupMatcher m : getMatchers())
        {
            configureMatcher(m, areaTree.getRoot());
            if (m.getBestConfigurations() != null && m.getBestConfigurations().size() > 0)
                m.setUsedConf(0); //the best configuration should be the first one
            else
                log.error("Matcher {} has no configurations available", m);
        }
    }
    
    protected void addLogicalAreas(Collection<Match> matches, AttributeGroupMatcher matcher, LogicalArea destNode, Tag superAreaTag)
    {
        for (Match match : matches)
        {
            LogicalArea dest = destNode;
            //create an enclosing area when required
            if (superAreaTag != null)
            {
                LogicalArea la = new DefaultLogicalArea();
                la.setMainTag(superAreaTag);
                String ws = String.valueOf(match.getAverageConnectionWeight());
                //match.dumpConnectionWeights();
                la.setText(ws);
                destNode.appendChild(la);
                dest = la;
            }
            //add logical areas for each tag
            for (Tag tag : matcher.getUsedTags())
            {
                List<Area> areas = match.get(tag);
                for (Area a : areas)
                {
                    dest.addArea(a);
                    if (destNode != dest)
                        destNode.addArea(a);
                    LogicalArea childArea = new DefaultLogicalArea(a);
                    childArea.setMainTag(tag);
                    dest.appendChild(childArea);
                }
            }
            //add dependencies
            for (AttributeGroupMatcher dep : matcher.getDependencies())
            {
                addLogicalAreas(match.getSubMatches(), dep, dest, createTagForMatcher(dep));
            }
        }
    }
    
    protected Tag createTagForMatcher(AttributeGroupMatcher matcher)
    {
        if (matcher.getGroup() != null && matcher.getGroup().getRoot() != null)
        {
            final Node root = matcher.getGroup().getRoot();
            String name = root.getTitle();
            if (name.lastIndexOf(':') != -1) //remove prefix from the name if used 
                name = name.substring(name.lastIndexOf(':') + 1);
            String uri = "";
            if (root.getUris() != null && root.getUris().length > 0)
                uri = root.getUris()[0];
            return new RDFTag(name, uri);
        }
        else
            return mainTag;
    }

}

/**
 * DefaultPatternBasedLogicalProvider.java
 *
 * Created on 11. 10. 2017, 13:26:31 by burgetr
 */
package org.fit.layout.patterns;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.fit.layout.impl.BaseLogicalTreeProvider;
import org.fit.layout.model.Area;
import org.fit.layout.patterns.graph.Graph;
import org.fit.layout.patterns.graph.GraphLoader;
import org.fit.layout.patterns.graph.Node;
import org.fit.layout.patterns.graph.Path;
import org.fit.layout.patterns.gui.PatternBasedLogicalProvider;
import org.fit.layout.patterns.spec.GraphTaskGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public abstract class GraphBasedLogicalProvider extends BaseLogicalTreeProvider implements PatternBasedLogicalProvider
{
    private static Logger log = LoggerFactory.getLogger(GraphBasedLogicalProvider.class);
    
    /** Graph specification */
    private Graph graph;
    /** The used group matcher */
    private List<AttributeGroupMatcher> groupMatchers;
    
    
    public GraphBasedLogicalProvider()
    {
        super();
    }
    
    public Graph getGraph()
    {
        return graph;
    }

    public void setGraph(Graph graph)
    {
        this.graph = graph;
        groupMatchers = null; //a new graph requires creating new group matchers
    }
    
    public void loadGraphFromJson(InputStream is)
    {
        GraphLoader gl = new GraphLoader();
        Reader r = new InputStreamReader(is);
        Graph g = gl.loadFromJson(r).collapse();
        log.info("Graph {} : {} ", g.getId(), g.getTitle());
        log.info("Loaded {} nodes {} edges", g.getNodes().size(), g.getEdges().size());
        log.info("Paths:");
        
        Node n = g.getPrimaryNode();
        for (Path path : g.getDatatypePathsFrom(n))
            log.info("> {}", path.toString());
        
        log.info("Groups:");
        g.getGroups();
        
        setGraph(g);
    }

    @Override
    public void configureMatcher(AttributeGroupMatcher matcher, List<Area> areas)
    {
        //autoconfigure the matcher
        matcher.configure(areas);
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
                        System.out.println("      dep " + dep.getAttrs() + depinfo);
                    }
                }
                groupMatchers = tasks;
            }
            else
                log.error("getMatchers() called while no graph is loaded");
        }
        return groupMatchers;
    }

}

/**
 * DefaultPatternBasedLogicalProvider.java
 *
 * Created on 11. 10. 2017, 13:26:31 by burgetr
 */
package org.fit.layout.patterns;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * 
 * @author burgetr
 */
public abstract class GraphBasedLogicalProvider extends BaseLogicalTreeProvider implements PatternBasedLogicalProvider
{
    private static Logger log = LoggerFactory.getLogger(GraphBasedLogicalProvider.class);
    
    /** Main tag used for tagging the results */
    private Tag mainTag = new DefaultTag("logical", "match");
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
        List<Area> leaves = new ArrayList<Area>();
        findLeaves(areaTree.getRoot(), leaves);
        
        AttributeGroupMatcher matcher = getMatchers().get(getMatchers().size() - 1);
        Collection<Match> matches = matcher.match(leaves);
        if (matches == null)
            matches = Collections.emptyList();
        
        LogicalArea lroot = new DefaultLogicalArea(areaTree.getRoot());
        addLogicalAreas(matches, matcher, lroot, createTagForMatcher(matcher));
        
        DefaultLogicalAreaTree ret = new DefaultLogicalAreaTree(areaTree);
        ret.setRoot(lroot);
        return ret;
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
    
    //========================================================================================
    
    protected void findLeaves(Area root, List<Area> dest)
    {
        if (root.isLeaf())
        {
            if (!root.isSeparator())
                dest.add(root);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                findLeaves(root.getChildAt(i), dest);
        }
    }
    
    protected void findLeaves(Area root, Tag[] tags, List<Area> dest)
    {
        if (tags != null)
        {
            Set<Tag> tagSet = new HashSet<>(tags.length);
            for (Tag t : tags)
                tagSet.add(t);
            findLeaves(root, dest, tagSet);
        }
        else
            findLeaves(root, dest, null);
    }
    
    protected void findLeaves(Area root, List<Area> dest, Set<Tag> tags)
    {
        boolean tagfound = false;
        if (tags != null)
        {
            Set<Tag> atags = root.getSupportedTags(0.2f);
            atags.retainAll(tags);
            tagfound = !atags.isEmpty();
        }
        if (tagfound || root.isLeaf())
        {
            if (!root.isSeparator())
                dest.add(root);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                findLeaves(root.getChildAt(i), dest, tags);
        }
    }

}

/**
 * GraphTask.java
 *
 * Created on 5. 4. 2017, 16:24:36 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fit.layout.classify.Tagger;
import org.fit.layout.patterns.AttributeGroupMatcher;
import org.fit.layout.patterns.AttributeGroupMatcher.Attribute;
import org.fit.layout.patterns.graph.Graph;
import org.fit.layout.patterns.graph.Group;
import org.fit.layout.patterns.graph.Node;
import org.fit.layout.patterns.graph.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements generating an extraction task based on a graph description.
 * 
 * @author burgetr
 */
public class GraphTaskGenerator
{
    private static Logger log = LoggerFactory.getLogger(GraphTaskGenerator.class);

    private Graph graph;
    private List<Path> targetPaths;
    private Map<Node, RDFTag> targetNodes;
    private Map<Node, Tagger> taggers;
    private float minSupport = 0.3f;
    
    
    public GraphTaskGenerator(Graph graph)
    {
        this.graph = graph;
        
        targetPaths = graph.getDatatypePathsFrom(graph.getPrimaryNode());
        targetNodes = findTargetNodes(targetPaths);
        taggers = new HashMap<>();
        mapDefaultTaggers();
    }

    public Graph getGraph()
    {
        return graph;
    }
    
    public Collection<RDFTag> getAssignedTags()
    {
        return targetNodes.values();
    }
    
    public void mapTagger(String uri, Tagger tagger)
    {
        Node node = graph.findNodeByUri(uri);
        if (node != null)
            mapTagger(node, tagger);
        else
            log.error("Couldn't map tagger to unknown node URI: {}", uri);
    }
    
    public void mapTagger(Node node, Tagger tagger)
    {
        if (targetNodes.containsKey(node))
        {
            taggers.put(node, tagger);
            log.info("Mapped tagger {} for {}", tagger.toString(), node.toString());
        }
        else
            log.error("Couldn't map tagger to unknown node: {}", node);
    }
    
    public List<AttributeGroupMatcher> createTasks()
    {
        Collection<Group> groups = graph.getGroups();
        List<AttributeGroupMatcher> ret = new ArrayList<>();
        List<AttributeGroupMatcher> depends = new ArrayList<>();
        List<Attribute> attrs = recursiveAddTasks(groups, ret, depends);
        if (!attrs.isEmpty()) //some attributes remaining, create a top matcher
        {
            AttributeGroupMatcher matcher = new AttributeGroupMatcher(attrs);
            //matcher.setGroup(group); //TODO
            matcher.setDependencies(depends);
            ret.add(matcher);
        }
        return ret;
    }
    
    private List<Attribute> recursiveAddTasks(Collection<Group> groups, List<AttributeGroupMatcher> dest, List<AttributeGroupMatcher> deps)
    {
        List<Attribute> attrs = new ArrayList<>();
        for (Group group : groups)
        {
            List<AttributeGroupMatcher> depends = new ArrayList<>();
            List<Attribute> subattrs = recursiveAddTasks(group.getSubGroups(), dest, depends);
            //create a new task for groups
            final int subCnt = subattrs.size() + depends.size(); //total number of sub groups (sub attributes + dependencies)
            if (subCnt > 1)
            {
                AttributeGroupMatcher matcher = new AttributeGroupMatcher(subattrs);
                matcher.setGroup(group);
                matcher.setDependencies(depends);
                dest.add(matcher);
                deps.add(matcher);
            }
            else if (subCnt == 1) //only one member group - join it as an attribute to current group
            {
                final Attribute sattr = subattrs.get(0);
                Attribute nsattr = new Attribute(sattr.getTag(),
                        sattr.getMinSupport(), 
                        sattr.isRequired() && group.isRequired(), 
                        sattr.isMany() || group.isMany(),
                        sattr.isSrcMany() || group.isSrcMany());
                attrs.add(nsattr);
            }
            //find new attributes if any
            Tagger tagger = taggers.get(group.getRoot());
            if (tagger != null)
                attrs.add(new Attribute(tagger.getTag(), minSupport, group.isRequired(), group.isMany(), group.isSrcMany()));
            else if (!group.getRoot().isObject())
                log.error("No tagger registered for datatype node {}", group.getRoot());
        }
        return attrs;
    }
    
    //==========================================================
    
    private Map<Node, RDFTag> findTargetNodes(List<Path> paths)
    {
        Map<Node, RDFTag> ret = new HashMap<>();
        for (Path p : paths)
        {
            Node target = p.getLast();
            if (!ret.containsKey(target))
            {
                String[] uris = target.getUris();
                RDFTag tag = new RDFTag(target.getTitle(), uris.length > 0 ? uris[0] : null);
                ret.put(target, tag);
            }
        }
        return ret;
    }
    
    private void mapDefaultTaggers()
    {
        for (Node n : targetNodes.keySet())
        {
            String taggerName = n.getTagger();
            if (taggerName != null)
            {
                try
                {
                    Class<?> clazz = Class.forName(taggerName);
                    Constructor<?> cons = clazz.getConstructor();
                    Object tagger = cons.newInstance();
                    if (tagger instanceof Tagger)
                        mapTagger(n, (Tagger) tagger);
                    else
                        log.error("Couldn't instantiate tagger {}: tagger is not instance of Tagger", taggerName);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    log.error("Couldn't instantiate tagger {}: {}", taggerName, e.getMessage());
                }
            }
            else
                log.warn("No default tagger registered for {}", n);
        }
    }
    
}

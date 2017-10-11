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
import org.fit.layout.patterns.graph.Graph;
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
    
    public AttributeGroupMatcher createTask()
    {
        List<Node> oo = new ArrayList<>();
        List<Node> om = new ArrayList<>();
        List<Node> mo = new ArrayList<>();
        List<Node> mm = new ArrayList<>();
        List<AttributeGroupMatcher.Attribute> attrs = new ArrayList<>();
        //categorize nodes by target cardinality
        for (Node n : targetNodes.keySet())
        {
            Path p = findPathFor(n);
            if (p.isSrcMany())
            {
                if (p.isDstMany())
                    mm.add(n);
                else
                    mo.add(n);
            }
            else
            {
                if (p.isDstMany())
                    om.add(n);
                else
                    oo.add(n);
            }
            Tagger tagger = taggers.get(n);
            if (tagger != null)
                attrs.add(new AttributeGroupMatcher.Attribute(tagger.getTag(), minSupport, !p.isOptional(), p.isDstMany(), p.isSrcMany()));
            else
                log.error("No tagger registered for {}", n);
        }
        return new AttributeGroupMatcher(attrs);
    }
    
    //==========================================================
    
    private Path findPathFor(Node target)
    {
        Path ret = null;
        for (Path p : targetPaths)
        {
            if (p.getLast().equals(target))
            {
                if (ret == null || p.isDstMany()) //prefer greater cardinality paths
                    ret = p;
            }
        }
        return ret;
    }
    
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

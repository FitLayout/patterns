/**
 * GraphTask.java
 *
 * Created on 5. 4. 2017, 16:24:36 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fit.layout.patterns.graph.Graph;
import org.fit.layout.patterns.graph.Node;
import org.fit.layout.patterns.graph.Path;

/**
 * Implements generating an extraction task based on a graph description.
 * 
 * @author burgetr
 */
public class GraphTaskGenerator
{
    private Graph graph;
    private Node mainNode;
    private Set<RDFTag> assignedTags;
    
    public GraphTaskGenerator(Graph graph, Node mainNode)
    {
        this.graph = graph;
        this.mainNode = mainNode;
        this.assignedTags = createTags(graph, mainNode);
    }

    public Graph getGraph()
    {
        return graph;
    }
    
    public Node getMainNode()
    {
        return mainNode;
    }
    
    public Set<RDFTag> getAssignedTags()
    {
        return assignedTags;
    }
    
    //==========================================================
    
    private Set<RDFTag> createTags(Graph g, Node main)
    {
        List<Path> paths = g.getDatatypePathsFrom(main);
        Set<RDFTag> ret = new HashSet<>(paths.size());
        for (Path p : paths)
        {
            String[] uris = p.getLast().getUris();
            ret.add(new RDFTag(p.getLast().getTitle(), uris.length > 0 ? uris[0] : null));
        }
        return ret;
    }

}

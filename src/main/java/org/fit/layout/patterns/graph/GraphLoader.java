/**
 * GraphLoader.java
 *
 * Created on 2. 4. 2017, 22:43:29 by burgetr
 */
package org.fit.layout.patterns.graph;

import java.io.Reader;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This class implements the extraction graph construction based on an external description (currently JSON file).
 * 
 * @author burgetr
 */
public class GraphLoader
{

    public Graph loadFromJson(Reader reader)
    {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Graph.class, new GraphDeserializer())
                .registerTypeAdapter(Node.class, new NodeDeserializer())
                .registerTypeAdapter(Edge.class, new EdgeDeserializer())
                .create();
        return gson.fromJson(reader, Graph.class);
    }
    
    private static String getNullAsEmptyString(JsonElement jsonElement) 
    {
        return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }
    
    private static class GraphDeserializer implements JsonDeserializer<Graph>
    {
        @Override
        public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            //System.out.println("graph " + json.toString());
            Graph ret = new Graph();
            
            JsonObject jGraph = json.getAsJsonObject().get("graph").getAsJsonObject();
            ret.setId(jGraph.get("id").getAsLong());
            ret.setTitle(GraphLoader.getNullAsEmptyString(jGraph.get("values").getAsJsonObject().get("title")));
            
            Node[] nodes = context.deserialize(jGraph.get("nodes"), Node[].class);
            for (Node node : nodes)
                ret.addNode(node);
            
            Edge[] edges = context.deserialize(jGraph.get("edges"), Edge[].class);
            for (Edge edge : edges)
                ret.addEdge(edge);
            
            return ret;
        }
    }
    
    private static class NodeDeserializer implements JsonDeserializer<Node>
    {
        @Override
        public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            //System.out.println("node");
            Node ret = new Node();
            
            JsonObject jNode = json.getAsJsonObject();
            ret.setId(jNode.get("id").getAsLong());
            JsonObject jValues = jNode.get("values").getAsJsonObject();
            ret.setTitle(GraphLoader.getNullAsEmptyString(jValues.get("title")));
            ret.setObject(jValues.get("object").getAsBoolean());
            String[] uris = context.deserialize(jValues.get("uris"), String[].class);
            ret.setUris(uris);
            
            return ret;
        }
    }
    
    private static class EdgeDeserializer implements JsonDeserializer<Edge>
    {
        @Override
        public Edge deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Edge ret = new Edge();
            
            JsonObject jNode = json.getAsJsonObject();
            ret.setSrcId(jNode.get("srcId").getAsLong());
            ret.setDstId(jNode.get("dstId").getAsLong());
            JsonObject jValues = jNode.get("values").getAsJsonObject();
            ret.setTitle(GraphLoader.getNullAsEmptyString(jValues.get("title")));
            ret.setSrcMany(jValues.get("cardinality").getAsJsonObject().get("src").getAsBoolean());
            ret.setDstMany(jValues.get("cardinality").getAsJsonObject().get("dst").getAsBoolean());
            if (jValues.get("optional") != null)
            {
                ret.setSrcOptional(jValues.get("optional").getAsJsonObject().get("src").getAsBoolean());
                ret.setDstOptional(jValues.get("optional").getAsJsonObject().get("dst").getAsBoolean());
            }
            String[] uris = context.deserialize(jValues.get("uris"), String[].class);
            ret.setUris(uris);
            
            return ret;
        }
    }
    
}

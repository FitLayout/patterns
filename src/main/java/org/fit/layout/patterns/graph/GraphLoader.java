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
import com.google.gson.JsonParseException;

/**
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
    
    private static class GraphDeserializer implements JsonDeserializer<Graph>
    {
        @Override
        public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    private static class NodeDeserializer implements JsonDeserializer<Node>
    {
        @Override
        public Node deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    private static class EdgeDeserializer implements JsonDeserializer<Edge>
    {
        @Override
        public Edge deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
}

/**
 * Task.java
 *
 * Created on 24. 3. 2016, 11:36:18 by burgetr
 */
package org.fit.layout.patterns.spec;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.model.Area;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.Tag;

/**
 * 
 * @author burgetr
 */
public abstract class Task
{
    private List<Task> subtasks;
    private Tag outputTag;
    
    
    public Task(Tag outputTag)
    {
        this.outputTag = outputTag;
        subtasks = new ArrayList<Task>();
    }

    public List<Task> getSubtasks()
    {
        return subtasks;
    }
    
    public void addSubTask(Task subtask)
    {
        subtasks.add(subtask);
    }

    public Tag getOutputTag()
    {
        return outputTag;
    }

    public boolean isLeaf()
    {
        return subtasks.isEmpty();
    }
    
    public List<Tag> getSubtaskTags()
    {
        List<Tag> ret = new ArrayList<Tag>(subtasks.size());
        for (Task task : subtasks)
            ret.add(task.getOutputTag());
        return ret;
    }
    
    abstract public List<LogicalArea> match(Area root);

}

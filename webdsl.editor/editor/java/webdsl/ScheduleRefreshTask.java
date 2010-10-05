package webdsl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import webdsl.WebDSLProjectBuilder;

public class ScheduleRefreshTask extends Task {
    private String projectname;

    public void execute() throws BuildException {
        System.out.println("refresh project: "+projectname);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project = workspace.getRoot().getProject(projectname);
        WebDSLProjectBuilder.addRefreshJob(project, 1000);
    }
    
    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }
}

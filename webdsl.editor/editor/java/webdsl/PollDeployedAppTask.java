package webdsl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import webdsl.WebDSLProjectBuilder;

public class PollDeployedAppTask extends Task {
    private String projectname;

    public void execute() throws BuildException {
        System.out.println("start server and publish project: "+projectname);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(projectname);
        String buildid = WebDSLProjectBuilder.getBuildIdCompleted(project);
        new WebDSLProjectBuilder().pollDeployedAppAndOpenBrowser(project,buildid, 0); //wtp tomcat specific
    }
    
    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }
}

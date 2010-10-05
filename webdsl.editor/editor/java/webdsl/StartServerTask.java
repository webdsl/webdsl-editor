package webdsl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import webdsl.WebDSLProjectBuilder;

public class StartServerTask extends Task {
    private String projectname;

    public void execute() throws BuildException {
        System.out.println("start server and publish project: "+projectname);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project = workspace.getRoot().getProject(projectname);
        try {
            NullProgressMonitor monitor = new NullProgressMonitor();
            WebDSLProjectBuilder.tryStartServer(project, monitor,null,0);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }
}

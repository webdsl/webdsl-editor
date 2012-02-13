package webdsl;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServer;

//hoped this would help with redeploying after context path change, but it doesn't
public class RemoveServerModuleTask extends Task {
    private String projectname;

    public void execute() throws BuildException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(projectname);
        try {
            IServer server = WebDSLProjectBuilder.getTomcatServer(project, null);
            if(server == null){
                System.out.println("module is currently not in server, cannot remove it");
            }
            else{
              System.out.println("Removing server module for project: "+projectname);
              WebDSLEditorWizard.removeProjectModuleFromServer(project, server, null);
              System.out.println("Stop server.");
              server.stop(true);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }
}

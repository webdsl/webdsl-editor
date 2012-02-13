package webdsl;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
//import org.eclipse.wst.server.core.IServer;

//@SuppressWarnings("restriction")
public class UpdateDotSettingsDirTask extends Task {
    private String projectname;
    private String rootapp;

    public void execute() throws BuildException {
        System.out.println("Update .settings dir for project: "+projectname);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(projectname);
        try {
            WebDSLEditorWizard.writeJdtPrefsFile(project);
            WebDSLEditorWizard.writeWstFacetFile(project);
            WebDSLEditorWizard.writeWstComponentFile(project, rootapp.equals("true")); // the main reason for doing this, this file contains settings for (non-)rootapp deploy in WTP Tomcat, however, only a manual 'publish' action seems to pick up this setting for the actual Tomcat config
            /*
            IServer server = WebDSLProjectBuilder.getTomcatServer(project, null);
            if(server != null){
              System.out.println(ComponentUtilities.getServerContextRoot(project));
              server.publish(IServer.PUBLISH_STATE_FULL,null);
              System.out.println(ComponentUtilities.getServerContextRoot(project));
            }*/
            /*if(rootapp.equals("true")){
                ComponentUtilities.setServerContextRoot(project, "");
            }
            else{
                ComponentUtilities.setServerContextRoot(project, project.getName());
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }
    public void setRootapp(String rootapp) {
        this.rootapp = rootapp;
    }
}

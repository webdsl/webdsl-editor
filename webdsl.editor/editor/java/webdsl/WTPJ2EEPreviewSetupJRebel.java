package webdsl;

import static webdsl.FileUtils.fileExists;
import static webdsl.FileUtils.refreshProject;
import static webdsl.FileUtils.writeStringToFile;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class WTPJ2EEPreviewSetupJRebel extends WTPJ2EEPreviewSetup {
  
    //singleton, want to be able to subclass and override methods
    private static final WTPJ2EEPreviewSetupJRebel instance = new WTPJ2EEPreviewSetupJRebel();
    protected WTPJ2EEPreviewSetupJRebel() {}
    public static WTPJ2EEPreviewSetupJRebel getInstance() {
        return instance;
    }	

    @Override
    public void writeJ2EEPreviewConfigFile(String plugindir, IProgressMonitor monitor) throws CoreException{
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project = workspace.getRoot().getProject("Servers");
        if(!project.exists()){
            project.create(monitor);
        }
        if(!project.isOpen()){
            project.open(monitor);
        }
        String fileName = project.getLocation()+"/"+j2eepreviewservername+" at localhost.launch";
        if(fileExists(fileName)){
            System.out.println("J2EE Preview configuration file already exists: "+fileName);
            return;
        }
        StringBuffer configFile = new StringBuffer();
        configFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        configFile.append("\t<launchConfiguration type=\"org.eclipse.jst.server.preview.launchConfigurationType\">\n");
        //configFile.append("\t<listAttribute key=\"org.eclipse.jdt.launching.CLASSPATH\">\n");
        //configFile.append("\t\t<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry containerPath=&quot;org.eclipse.jdt.launching.JRE_CONTAINER&quot; path=&quot;1&quot; type=&quot;4&quot;/&gt;&#10;\"/>\n");
        //configFile.append("\t</listAttribute>\n");
        configFile.append("\t<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
        configFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Drebel.log=true ${jrebel_args} -Xss8m -Xms48m -Xmx1024m -XX:MaxPermSize=128m\"/>\n");
        configFile.append("\t<stringAttribute key=\"server-id\" value=\""+j2eepreviewserverid+"\"/>\n");
        configFile.append("</launchConfiguration>\n");
        try {
           writeStringToFile(configFile.toString(), fileName);
           System.out.println("created J2EE Preview configuration file: "+fileName);
        } catch (IOException e) {
           e.printStackTrace();
        }
        refreshProject(project);
    }

}
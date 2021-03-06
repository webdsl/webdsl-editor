package webdsl;

import static webdsl.FileUtils.fileExists;
import static webdsl.FileUtils.getWebDSLVersion;
import static webdsl.FileUtils.refreshProject;
import static webdsl.FileUtils.writeStringToFile;
import static webdsl.WTPUtils.addProjectModuleToServer;
import static webdsl.WTPUtils.getCompatibleServerType;
import static webdsl.WTPUtils.getRuntime;
import static webdsl.WTPUtils.getRuntimeType;
import static webdsl.WTPUtils.getServer;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;

public class WTPJ2EEPreviewSetup  {
   
    //singleton, want to be able to subclass and override methods
    private static final WTPJ2EEPreviewSetup instance = new WTPJ2EEPreviewSetup();
    protected WTPJ2EEPreviewSetup() {}
    public static WTPJ2EEPreviewSetup getInstance() {
        return instance;
    }
    
     /*
      * add version to server instance String, otherwise builds break after
      * updating the plugin, due to stale references in 
      * -workspace-/.metadata/.plugins/org.eclipse.wst.server.core/
      * to the tomcat installation of the previous version
      */
     public String webdslversion = getWebDSLVersion();
         
     public String j2eepreviewruntimeid = 
         "webdsl_j2eepreviewruntime" + webdslversion; 
     public String j2eepreviewruntimename = 
         "Runtime J2EE Preview WebDSL v" + webdslversion; 
     public String j2eepreviewserverid = 
         "webdsl_j2eepreviewserver" + webdslversion; 
     public String j2eepreviewservername = 
         "J2EE Preview Server WebDSL v" + webdslversion; 
     
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
         configFile.append("\t<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
         configFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Xss8m -Xms48m -Xmx1024m -XX:MaxPermSize=128m\"/>\n");
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
     
     public IRuntimeType getJ2EEPreviewRuntimeType(){
         return getRuntimeType("J2EE Preview");
     }
     
     public IRuntime getWebDSLJ2EEPreviewRuntime(){
         return getRuntime(j2eepreviewruntimeid);
     }

     public IRuntime createWebDSLJ2EEPreviewRuntime(String plugindir, IProgressMonitor monitor) throws CoreException{
             IRuntimeType tomcatRuntimeType = getJ2EEPreviewRuntimeType();
             IRuntimeWorkingCopy rwc = tomcatRuntimeType.createRuntime(j2eepreviewruntimeid, monitor);
             rwc.setName(j2eepreviewruntimename);
             IRuntime rt = rwc.save(true, monitor);
             System.out.println("created runtime: "+rt);
             return rt;
     }
     
     /**
      * add J2EE Preview runtime for webdsl plugin if not created yet
      */
     public IRuntime getOrCreateWebDSLJ2EEPreviewRuntime(String plugindir, IProgressMonitor monitor)throws CoreException{
         IRuntime runtime = getWebDSLJ2EEPreviewRuntime();
         if(runtime == null){
             runtime = createWebDSLJ2EEPreviewRuntime(plugindir,monitor);
         }
         System.out.println("get or create runtime: "+runtime);
         return runtime;
     }
     
     
     public IServer getWebDSLJ2EEPreviewServer(IProject project,IProgressMonitor monitor){
         return getServer(project, j2eepreviewserverid, monitor);
     }
     
     public IServer createWebDSLJ2EEPreviewServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IRuntime runtime = getOrCreateWebDSLJ2EEPreviewRuntime(plugindir,monitor);
         IRuntimeType runtimetype = getJ2EEPreviewRuntimeType();
         IServerType st = getCompatibleServerType(runtimetype);
         //System.out.println(st);
         IServer server = null;
         IServerWorkingCopy serverwc = st.createServer(j2eepreviewserverid, null, runtime, monitor);
         serverwc.setName(j2eepreviewservername);
         server = serverwc.saveAll(true, monitor); //saveAll will also save ServerConfiguration and Runtime if they were still WorkingCopy
         System.out.println("created server: "+server);
         
         copyKeystoreFile(project, plugindir);         

         writeJ2EEPreviewConfigFile(plugindir, monitor);
         server.publish(IServer.PUBLISH_CLEAN, monitor);
         
         return server;
     }
     
     public void copyKeystoreFile(IProject project, String plugindir){
        System.out.println("TODO: check https support in J2EE Preview server");
     }
     
     /**
      * add J2EE Preview server for webdsl plugin of not created yet
      */
     public IServer getOrCreateWebDSLJ2EEPreviewServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IServer server = getWebDSLJ2EEPreviewServer(project,monitor);
         if(server==null){
             server = createWebDSLJ2EEPreviewServer(project, plugindir, monitor);
         }
         System.out.println("get or create server: "+server);
         return server;
     }

     public void initWtpServerConfig(String plugindir, final IProject project, final String projectName, IProgressMonitor monitor) throws CoreException{
         IServer server = getOrCreateWebDSLJ2EEPreviewServer(project,plugindir,monitor);
         addProjectModuleToServer(project,server,monitor);
     }

}
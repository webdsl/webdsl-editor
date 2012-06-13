package webdsl;

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
import static webdsl.FileUtils.*;
import static webdsl.WTPUtils.*;

public class WTPJ2EEPreviewSetup  {
   
     /*
      * add version to server instance String, otherwise builds break after
      * updating the plugin, due to stale references in 
      * -workspace-/.metadata/.plugins/org.eclipse.wst.server.core/
      * to the tomcat installation of the previous version
      */
     public static String webdslversion = getWebDSLVersion();
         
     public static String j2eepreviewruntimeid = 
         "webdsl_j2eepreviewruntime" + webdslversion; 
     public static String j2eepreviewruntimename = 
         "Runtime J2EE Preview WebDSL v" + webdslversion; 
     public static String j2eepreviewserverid = 
         "webdsl_j2eepreviewserver" + webdslversion; 
     public static String j2eepreviewservername = 
         "J2EE Preview Server WebDSL v" + webdslversion; 
    /* 
     public static String writeJ2EEPreviewConfigFile(String plugindir){
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IProject project = workspace.getRoot().getProject("Servers");
         String fileName = project.getLocation()+"/"+j2eepreviewservername+" at localhost.launch";
         if(fileExists(fileName)){
             System.out.println("J2EE Preview configuration file already exists: "+fileName);
             return fileName;
         }
         StringBuffer configFile = new StringBuffer();
         configFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
         configFile.append("\t<launchConfiguration type=\"org.eclipse.jst.server.preview.launchConfigurationType\">\n");
         configFile.append("\t<stringAttribute key=\"server-id\" value=\""+j2eepreviewserverid+"\"/>\n");
         configFile.append("</launchConfiguration>\n");
         try {
            writeStringToFile(configFile.toString(), fileName);
            System.out.println("created J2EE Preview configuration file: "+fileName);
         } catch (IOException e) {
            e.printStackTrace();
         }
         refreshProject(project);
         return fileName;
     }*/
     
     public static IRuntimeType getJ2EEPreviewRuntimeType(){
         return getRuntimeType("J2EE Preview");
     }
     
     public static IRuntime getWebDSLJ2EEPreviewRuntime(){
         return getRuntime(j2eepreviewruntimeid);
     }

     public static IRuntime createWebDSLJ2EEPreviewRuntime(String plugindir, IProgressMonitor monitor) throws CoreException{
             IRuntimeType tomcat6runtimetype = getJ2EEPreviewRuntimeType();
             IRuntimeWorkingCopy rwc = tomcat6runtimetype.createRuntime(j2eepreviewruntimeid, monitor);
             rwc.setName(j2eepreviewruntimename);
             IRuntime rt = rwc.save(true, monitor);
             System.out.println("created runtime: "+rt);
             return rt;
     }
     
     /**
      * add J2EE Preview runtime for webdsl plugin if not created yet
      */
     public static IRuntime getOrCreateWebDSLJ2EEPreviewRuntime(String plugindir, IProgressMonitor monitor)throws CoreException{
         IRuntime runtime = getWebDSLJ2EEPreviewRuntime();
         if(runtime == null){
             runtime = createWebDSLJ2EEPreviewRuntime(plugindir,monitor);
         }
         System.out.println("get or create runtime: "+runtime);
         return runtime;
     }
     
     
     public static IServer getWebDSLJ2EEPreviewServer(IProject project,IProgressMonitor monitor){
         return getServer(project, j2eepreviewserverid, monitor);
     }
     
     public static IServer createWebDSLJ2EEPreviewServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
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

         //writeJ2EEPreviewConfigFile(plugindir);
         server.publish(IServer.PUBLISH_CLEAN, monitor);
         
         return server;
     }
     
     public static void copyKeystoreFile(IProject project, String plugindir){
        System.out.println("TODO: check https support in J2EE Preview server");
     }
     
     /**
      * add J2EE Preview server for webdsl plugin of not created yet
      */
     public static IServer getOrCreateWebDSLJ2EEPreviewServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IServer server = getWebDSLJ2EEPreviewServer(project,monitor);
         if(server==null){
             server = createWebDSLJ2EEPreviewServer(project, plugindir, monitor);
         }
         System.out.println("get or create server: "+server);
         return server;
     }

     public static void initWtpServerConfig(String plugindir, final IProject project, final String projectName, IProgressMonitor monitor) throws CoreException{
         IServer server = getOrCreateWebDSLJ2EEPreviewServer(project,plugindir,monitor);
         addProjectModuleToServer(project,server,monitor);
     }

}
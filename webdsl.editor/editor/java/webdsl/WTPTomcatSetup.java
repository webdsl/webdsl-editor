package webdsl;

import java.io.IOException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import static webdsl.FileUtils.*;
import static webdsl.WTPUtils.*;

public class WTPTomcatSetup  {
   
    //singleton, want to be able to subclass and override methods
    private static final WTPTomcatSetup instance = new WTPTomcatSetup();
    protected WTPTomcatSetup() {}
    public static WTPTomcatSetup getInstance() {
        return instance;
    }
    
     /*
      * add version to server instance String, otherwise builds break after
      * updating the plugin, due to stale references in 
      * -workspace-/.metadata/.plugins/org.eclipse.wst.server.core/
      * to the tomcat installation of the previous version
      */
     public String webdslversion = 
         getWebDSLVersion();
     public String tomcatruntimeid = 
         "webdsl_tomcat7runtime" + webdslversion; 
     public String tomcatruntimename = 
         "Runtime Tomcat v7.0 WebDSL v" + webdslversion; 
     public String tomcatserverid = 
         "webdsl_tomcat7server" + webdslversion; 
     public String tomcatservername = 
         "Tomcat v7.0 Server WebDSL v" + webdslversion; 
     
     public String writeTomcatConfigFile(String plugindir){
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         String tomcatdir = plugindir+"webdsl-template/tomcat/tomcat";
         IProject project = workspace.getRoot().getProject("Servers");
         String fileName = project.getLocation()+"/"+tomcatservername+" at localhost.launch";
         if(fileExists(fileName)){
             System.out.println("Tomcat configuration file already exists: "+fileName);
             return fileName;
         }
         String jre = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6";
         String workspacedir = workspace.getRoot().getRawLocation().toString();
         
         String workingDir = workspacedir+"/Servers/workingdir/tomcat/tmp_v"+webdslversion;
         System.out.println("Server working dir: "+workingDir); //seems to be ignored, VM_ARGUMENTS settings below are overridden
         StringBuffer tomcatconfigFile = new StringBuffer();
         tomcatconfigFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
         tomcatconfigFile.append("\t<launchConfiguration type=\"org.eclipse.jst.server.tomcat.core.launchConfigurationType\">\n");
         tomcatconfigFile.append("\t<listAttribute key=\"org.eclipse.jdt.launching.CLASSPATH\">\n");
         tomcatconfigFile.append("\t\t<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry containerPath=&quot;"+jre+"&quot; path=&quot;1&quot; type=&quot;4&quot;/&gt;&#10;\"/>\n");
         tomcatconfigFile.append("\t\t<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry externalArchive=&quot;"+tomcatdir+"/bin/bootstrap.jar&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#10;\"/>\n");
         tomcatconfigFile.append("\t</listAttribute>\n");
         tomcatconfigFile.append("\t<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"false\"/>\n");
         tomcatconfigFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\""+jre+"\"/>\n");
         tomcatconfigFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.PROGRAM_ARGUMENTS\" value=\"start\"/>\n");
         tomcatconfigFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Dcatalina.base=&quot;"+workingDir+"&quot; -Dcatalina.home=&quot;"+tomcatdir+"&quot; -Dwtp.deploy=&quot;"+workingDir+"/wtpwebapps&quot; -Djava.endorsed.dirs=&quot;"+tomcatdir+"/endorsed&quot; -Xss8m -Xms48m -Xmx1024m -XX:MaxPermSize=384m\"/>\n");
         tomcatconfigFile.append("\t<stringAttribute key=\"server-id\" value=\""+tomcatserverid+"\"/>\n");
         tomcatconfigFile.append("</launchConfiguration>\n");
         try {
            writeStringToFile(tomcatconfigFile.toString(), fileName);
            System.out.println("created Tomcat configuration file: "+fileName);
         } catch (IOException e) {
            e.printStackTrace();
         }
         refreshProject(project);
         return fileName;
     }
     
     public IRuntimeType getTomcatRuntimeType(){
         return getRuntimeType("Apache Tomcat v7.0");
     }
     
     public IRuntime getWebDSLTomcatRuntime(){
         return getRuntime(tomcatruntimeid);
     }

     public IRuntime createWebDSLTomcatRuntime(String plugindir, IProgressMonitor monitor) throws CoreException{
             IRuntimeType tomcatRuntimeType = getTomcatRuntimeType();
             IRuntimeWorkingCopy rwc = tomcatRuntimeType.createRuntime(tomcatruntimeid, monitor);
             rwc.setLocation(Path.fromOSString(plugindir+"/webdsl-template/tomcat/tomcat"));
             //System.out.println("Location of Tomcat runtime: "+rwc.getLocation());
             rwc.setName(tomcatruntimename);
             IRuntime rt = rwc.save(true, monitor);
             System.out.println("created runtime: "+rt);
             return rt;
     }
     
     /**
      * add tomcat runtime for webdsl plugin if not created yet
      */
     public IRuntime getOrCreateWebDSLTomcatRuntime(String plugindir, IProgressMonitor monitor)throws CoreException{
         IRuntime pluginTomcatRuntime = getWebDSLTomcatRuntime();
         if(pluginTomcatRuntime == null){
             pluginTomcatRuntime = createWebDSLTomcatRuntime(plugindir,monitor);
         }
         System.out.println("get or create runtime: "+pluginTomcatRuntime);
         return pluginTomcatRuntime;
     }
     
     
     public IServer getWebDSLTomcatServer(IProject project,IProgressMonitor monitor){
         return getServer(project, tomcatserverid, monitor);
     }
     
     public IServer createWebDSLTomcatServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IRuntime pluginTomcatRuntime = getOrCreateWebDSLTomcatRuntime(plugindir,monitor);
         IRuntimeType tomcatRuntimeType = getTomcatRuntimeType();
         IServerType st = getCompatibleServerType(tomcatRuntimeType);
         //System.out.println(st);
         IServer pluginTomcatServer = null;
         IServerWorkingCopy server = st.createServer(tomcatserverid, null, pluginTomcatRuntime, monitor);
         server.setName(tomcatservername);
         pluginTomcatServer = server.saveAll(true, monitor); //saveAll will also save ServerConfiguration and Runtime if they were still WorkingCopy
         System.out.println("created server: "+pluginTomcatServer);
         
         copyKeystoreFile(project, plugindir);         

         writeTomcatConfigFile(plugindir);
         pluginTomcatServer.publish(IServer.PUBLISH_CLEAN, monitor);
         
         return pluginTomcatServer;
     }
     
     public void copyKeystoreFile(IProject project, String plugindir){
        try {
            copyFile(plugindir+"/webdsl-template/template-java-servlet/tomcat/.keystore",project.getWorkspace().getRoot().getLocation()+"/Servers/.keystore");
        } catch (IOException e) {
            e.printStackTrace();
        }   
     }
     
     /**
      * add tomcat server for webdsl plugin of not created yet
      */
     public IServer getOrCreateWebDSLTomcatServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IServer pluginTomcatServer = getWebDSLTomcatServer(project,monitor);
         if(pluginTomcatServer==null){
             pluginTomcatServer = createWebDSLTomcatServer(project, plugindir, monitor);
         }
         System.out.println("get or create server: "+pluginTomcatServer);
         return pluginTomcatServer;
     }
     
     public void initWtpServerConfig(String plugindir, final IProject project, final String projectName, IProgressMonitor monitor) throws CoreException{
         IServer pluginTomcatServer = getOrCreateWebDSLTomcatServer(project,plugindir,monitor);
         addProjectModuleToServer(project,pluginTomcatServer,monitor);
     }
     
}
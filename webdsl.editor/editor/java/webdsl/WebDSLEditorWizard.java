package webdsl;

import static org.eclipse.core.resources.IResource.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

/**
 * A wizard for creating new WebDSL projects.
 */
@SuppressWarnings("restriction")
public class WebDSLEditorWizard extends Wizard implements INewWizard {

    protected WebDSLEditorWizardPage input;
    
    private IProject lastProject;

    // TODO: Support external directory and working set selection in wizard
            
    public WebDSLEditorWizard() {
        setNeedsProgressMonitor(true);
        input = new WebDSLEditorWizardPage();
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // No further initialization required
    }
    
    @Override
    public void addPages() {
        addPage(input);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final String appName = input.getInputAppName();
        final String projectName = input.getInputProjectName();
        final boolean isMysqlSelected = input.isMysqlSelected();
        final String host = input.getInputDBHost();
        final String user = input.getInputDBUser();
        final String pass = input.getInputDBPass();
        final String name = input.getInputDBName();
        final String mode = input.getInputDBMode();
        final String file = input.getInputDBFile();
        final String tomcatpath = input.getInputTomcatPath();
        final String smtphost = input.getInputSmtpHost();
        final String smtpport = input.getInputSmtpPort();
        final String smtpuser = input.getInputSmtpUser();
        final String smtppass = input.getInputSmtpPass();
        final boolean isRootApp = input.isRootApp();
        System.out.println(appName+projectName);
        
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    doFinish(appName, projectName, isMysqlSelected, host, user, pass, name, mode, file, tomcatpath, smtphost, smtpport, smtpuser, smtppass, isRootApp, monitor);
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            rollback();
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            Environment.logException("Exception while creating new project", realException);
            MessageDialog.openError(getShell(), "Error: " + realException.getClass().getName(), realException.getMessage());
            rollback();
            return false;
        }
        return true;
    }
    
    private void rollback() {
        // monitor.setTaskName("Undoing workspace operations");
        try {
            if (lastProject != null) lastProject.delete(true, null);
        } catch (CoreException e) {
            Environment.logException("Could not remove new project", e);
        }
    }
    
     private void doFinish(String appName, String projectName, boolean isMysqlSelected, String host, String user, String pass, String name, String mode, String file, String tomcatpath, String smtphost, String smtpport, String smtpuser, String smtppass, boolean isRootApp, IProgressMonitor monitor) throws IOException, CoreException {
        //disableAutoBuild();
         
        final int TASK_COUNT = 3;
        lastProject = null;
        monitor.beginTask("Creating " + appName + " application", TASK_COUNT);
        
        IProject project = createNewProject(projectName, monitor);
        
        monitor.setTaskName("Copying example application files");

        String plugindir = webdsl.WebDSLEditorWizard.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            plugindir = plugindir.substring(1);
        }
        System.out.println("path: "+plugindir);
        
        try { 
            String appinifilename = project.getLocation()+"/application.ini";
            System.out.println(appinifilename);
            BufferedWriter out = new BufferedWriter(new FileWriter(appinifilename)); 
            out.write("appname="+appName+"\n");
            out.write("backend=servlet\n");
            out.write("sessiontimeout=120\n");
            out.write("smtphost="+smtphost+"\n");
            out.write("smtpport="+smtpport+"\n");
            out.write("smtpuser="+smtpuser+"\n");
            out.write("smtppass="+smtppass+"\n");
            out.write("tomcatpath="+tomcatpath+"\n");
            out.write("httpport=8080\n");
            out.write("httpsport=8443\n");
            if(isMysqlSelected){
                out.write("dbserver="+host+"\n"); 
                out.write("dbuser="+user+"\n"); 
                out.write("dbpassword="+pass+"\n");
                out.write("dbname="+name+"\n");
                out.write("dbmode="+mode+"\n");
            }
            else{
                out.write("db=sqlite\n");
                out.write("dbfile="+file+"\n");
                out.write("dbmode="+mode+"\n");
            }
            if(isRootApp){
                out.write("rootapp=true\n");
            }
            out.close(); 
        } 
        catch (IOException e) { 
            Environment.logException(e);
            throw e;
        } 
        
        writeExampleApplicationFiles(project, appName, plugindir);
        
        monitor.worked(1);
        
        writeBuildXmlFile(project);
        writeBuildXmlLaunchFile(project, appName, plugindir);
        writeCleanProjectXmlFile(project);
        writeCleanProjectXmlLaunchFile(project, appName, plugindir);
        
        writeClassPathFile(project);
        
        //write .settings/* files
        createDirs(project.getLocation()+"/.settings");
        writeJdtPrefsFile(project);
        writeWstComponentFile(project, isRootApp);
        writeWstFacetFile(project);
        
        writeProjectFileWithoutWebDSLBuilder(project);
        
        refreshProject(project);
        initWtpServerConfig(plugindir,project,projectName,monitor);
        writeProjectFile(project);
        refreshProject(project);
        
        writeTomcatConfigFile(plugindir);

        openEditorsForExampleApp(appName, project,monitor);
    }
     
     public static void disableAutoBuild() {
         IWorkspace ws = ResourcesPlugin.getWorkspace();
         IWorkspaceDescription desc = ws.getDescription();
         desc.setAutoBuilding(false);
         try {
            ws.setDescription(desc);
        } catch (CoreException e) {
            e.printStackTrace();
        }
     }
     
     protected void openEditorsForExampleApp(String appName, IProject project, IProgressMonitor monitor){
         monitor.setTaskName("Opening editor tabs");
         Display display = getShell().getDisplay();
         EditorState.asyncOpenEditor(display, project.getFile("templates.app"), true);
         EditorState.asyncOpenEditor(display, project.getFile(appName+".app"), true);
         monitor.worked(1);    	 
     }
     
     protected IProject createNewProject(String projectName, IProgressMonitor monitor) throws CoreException{
         monitor.setTaskName("Creating Eclipse project");
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IProject project = lastProject = workspace.getRoot().getProject(projectName);
         project.create(null);
         project.open(null);
         monitor.worked(1);
         return project;
     }
     
     public void writeExampleApplicationFiles(IProject project, String appName, String plugindir) throws IOException{
         copyFile(plugindir+"webdsl-template/new_project/templates.app", project.getLocation()+"/templates.app");
         copyFile(plugindir+"webdsl-template/new_project/APPLICATION_NAME.app", project.getLocation()+"/"+appName+".app");
         createDirs(project.getLocation()+"/images");
         copyFile(plugindir+"webdsl-template/new_project/images/logosmall.png", project.getLocation()+"/images/logosmall.png");
         createDirs(project.getLocation()+"/stylesheets");
         copyFile(plugindir+"webdsl-template/new_project/stylesheets/common_.css", project.getLocation()+"/stylesheets/common_.css");
      
         copyWebDSLSrcLibrary(project, appName, plugindir);
         
         //create other special dirs, to avoid seeing red warnings in build, also helps to see what you can modify/extend
         createDirs(project.getLocation()+"/html");
         createDirs(project.getLocation()+"/javascript");
         createDirs(project.getLocation()+"/lib");
         createDirs(project.getLocation()+"/nativejava");
         
         writeStringToFile("needed to start initial build", project.getLocation()+"/.saved-but-not-built");
     }
     
     public static void copyWebDSLSrcLibrary(IProject project, String appName, String plugindir) throws IOException{
         //copy the webdsl src library for the first time, this will also be performed for clean builds
         createDirs(project.getLocation()+"/.servletapp/src-webdsl-template");
         copyFile(plugindir+"webdsl-template/template-webdsl/built-in.app", project.getLocation()+"/.servletapp/src-webdsl-template/built-in.app");
     }
     
     public static void writeBuildXmlFile(IProject project) throws IOException{
         StringBuffer ant = new StringBuffer();
         ant.append("<project name=\"webdsl-eclipse-plugin\" default=\"plugin-eclipse-build\">\n");
         //ant.append("\t<property name=\"plugindir\" value=\""+plugindir+"\" />\n");
         ant.append("\t<fail unless=\"plugindir\" message=\"WebDSL plugin is not correctly installed. The 'plugindir' property is not available.\" />\n");
         ant.append("\t<property name=\"templatedir\" value=\"${plugindir}/webdsl-template\"/>\n");
         ant.append("\t<property name=\"currentdir\" value=\"${basedir}\"/>\n");
         ant.append("\t<property name=\"webdsl-java-cp\" value=\"${plugindir}/include/webdsl.jar\"/>\n");
         ant.append("\t<property name=\"webdslexec\" value=\"java\"/>\n");
         ant.append("\t<import file=\"${plugindir}/webdsl-template/webdsl-build.xml\"/>\n");
        
         ant.append("\t<target name=\"plugin-eclipse-build\">\n");
         ant.append("\t\t<antcall target=\"eclipse-build\"/>\n");
         ant.append("\t</target>\n");
         
         ant.append("\t<target name=\"plugin-build\">\n");
         ant.append("\t\t<property name=\"buildoptions\" value=\"build\" />\n");
         ant.append("\t\t<antcall target=\"command\"/>\n");
         ant.append("\t</target>\n");
          
          ant.append("\t<target name=\"plugin-run\">\n");
          ant.append("\t\t<property name=\"buildoptions\" value=\"run\" />\n");
          ant.append("\t\t<antcall target=\"command\"/>\n");
          ant.append("\t</target>\n");
          
          ant.append("\t<target name=\"plugin-deploy\">\n");
          ant.append("\t\t<property name=\"buildoptions\" value=\"deploy\" />\n");
          ant.append("\t\t<antcall target=\"command\"/>\n");
          ant.append("\t</target>\n");
          
          ant.append("\t<target name=\"plugin-tomcatdeploy\">\n");
          ant.append("\t\t<property name=\"buildoptions\" value=\"tomcatdeploy\" />\n");
          ant.append("\t\t<antcall target=\"command\"/>\n");
          ant.append("\t</target>\n");
          
          ant.append("\t<target name=\"plugin-cleanall\">\n");
          ant.append("\t\t<property name=\"buildoptions\" value=\"cleanall\" />\n");
          ant.append("\t\t<antcall target=\"command\"/>\n");
          ant.append("\t</target>\n");
          
         ant.append("</project>");
         
         writeStringToFile(ant.toString(), project.getLocation()+"/build.xml");
         
     }
     
     public static void writeCleanProjectXmlFile(IProject project) throws IOException{
         StringBuffer ant = new StringBuffer();
         ant.append("<project name=\"clean-project\" default=\"clean-project\">\n");
         ant.append("\t<target name=\"clean-project\">\n");
         ant.append("\t\t<delete dir=\"${basedir}/.webdsl-parsecache\" />\n");
         ant.append("\t\t<delete dir=\"${basedir}/.cache\" />\n");
         ant.append("\t\t<delete dir=\"${basedir}/.servletapp\" />\n");
         ant.append("\t\t<delete dir=\"${basedir}/.webdsl-fragment-cache\" />\n");
         ant.append("\t\t<delete file=\"${basedir}/.dependencies.webdsl\" />\n");
         ant.append("\t\t<delete includeemptydirs=\"true\">\n");
         ant.append("\t\t\t<fileset dir=\"${basedir}/WebContent\" includes=\"**/*\"/>\n");
         ant.append("\t\t</delete>\n");
         ant.append("\t\t<echo file=\".saved-but-not-built\"/>\n");
         ant.append("\t\t<eclipse.convertPath fileSystemPath=\"${basedir}\" property=\"resourcePath\" />\n");
         ant.append("\t\t<eclipse.refreshLocal resource=\"${resourcePath}\" depth=\"infinite\" />\n");
         ant.append("\t</target>\n");
         ant.append("</project>"); 
         writeStringToFile(ant.toString(), project.getLocation()+"/clean-project.xml");
     }
     
     public static void writeCleanProjectXmlLaunchFile(IProject project, String appName, String plugindir) throws IOException{
         //writeAntXmlLaunchFile(project,appName,plugindir,"clean-project.xml","clean-project", "\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"clean\"/>\n");
         String antfile = "clean-project.xml";
         
         StringBuffer buildLaunchFile = new StringBuffer();
         buildLaunchFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
         buildLaunchFile.append("<launchConfiguration type=\"org.eclipse.ant.AntBuilderLaunchConfigurationType\">\n");
         buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.ant.ui.ATTR_TARGETS_UPDATED\" value=\"true\"/>\n");
         buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
         buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"false\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
         buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\""+appName+"\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/"+appName+"/"+antfile+"}\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"clean\"/>\n");
         buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>\n");
         buildLaunchFile.append("</launchConfiguration>\n");
         writeStringToFile(buildLaunchFile.toString(), project.getLocation()+"/"+appName+" "+antfile+".launch");
     }
     public static void writeBuildXmlLaunchFile(IProject project, String appName, String plugindir) throws IOException{
         writeAntXmlLaunchFile(project,appName,plugindir,"build.xml","plugin-eclipse-build","");
     }
     public static void writeAntXmlLaunchFile(IProject project, String appName, String plugindir, String antfile, String anttarget, String extra) throws IOException{
        //create build launch file to make sure ant uses same jre instance as eclipse, otherwise the plugindir property provider won't work
         StringBuffer buildLaunchFile = new StringBuffer();
         buildLaunchFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
         buildLaunchFile.append("<launchConfiguration type=\"org.eclipse.ant.AntLaunchConfigurationType\">\n");
         buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
         buildLaunchFile.append("\t<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n");
         buildLaunchFile.append("\t\t<listEntry value=\"/"+appName+"/"+antfile+"\"/>\n");
         buildLaunchFile.append("\t</listAttribute>\n");
         buildLaunchFile.append("\t<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n");
         buildLaunchFile.append("\t\t<listEntry value=\"1\"/>\n");
         buildLaunchFile.append("\t</listAttribute>\n");
         buildLaunchFile.append("<listAttribute key=\"org.eclipse.jdt.launching.CLASSPATH\">\n");
         //<!--buildLaunchFile.append("<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry containerPath=&quot;org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6&quot; path=&quot;1&quot; type=&quot;4&quot;/&gt;&#10;\"/>\n");-->
         buildLaunchFile.append("<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry id=&quot;org.eclipse.ant.ui.classpathentry.antHome&quot;&gt;&#10;&lt;memento default=&quot;true&quot;/&gt;&#10;&lt;/runtimeClasspathEntry&gt;&#10;\"/>\n");
         buildLaunchFile.append("<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry id=&quot;org.eclipse.ant.ui.classpathentry.extraClasspathEntries&quot;&gt;&#10;&lt;memento/&gt;&#10;&lt;/runtimeClasspathEntry&gt;&#10;\"/>\n");
         //buildLaunchFile.append("<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry externalArchive=&quot;"+plugindir+"/webdsl-template/template-java-servlet/lib-test/org.eclipse.wst.server.core_1.1.102.v20100123.jar&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#10;\"/>\n");
         //buildLaunchFile.append("<listEntry value=\"&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; standalone=&quot;no&quot;?&gt;&#10;&lt;runtimeClasspathEntry externalArchive=&quot;"+plugindir+"/webdsl-template/template-java-servlet/lib-test/org.eclipse.wst.server.ui_1.1.103.v20100123.jar&quot; path=&quot;3&quot; type=&quot;2&quot;/&gt;&#10;\"/>\n");
         buildLaunchFile.append("</listAttribute>\n");
         buildLaunchFile.append("<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"false\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\""+appName+"\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LAUNCH_CONFIGURATION_BUILD_SCOPE\" value=\"${none}\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_ANT_TARGETS\" value=\""+anttarget+",\"/>\n");
         buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/"+appName+"/"+antfile+"}\"/>\n");
         if(!extra.equals("")){
             buildLaunchFile.append(extra);
         }
         buildLaunchFile.append("\t<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
         buildLaunchFile.append("</launchConfiguration>\n");
         writeStringToFile(buildLaunchFile.toString(), project.getLocation()+"/"+appName+" "+antfile+".launch");
     }
     
     public static void writeClassPathFile(IProject project) throws IOException{
        //write a .classpath for java nature of project
         StringBuffer classpathFile = new StringBuffer();
         classpathFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         classpathFile.append("<classpath>\n");
         classpathFile.append("\t<classpathentry kind=\"src\" path=\".servletapp/src\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jst.j2ee.internal.web.container\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jst.j2ee.internal.module.container\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"output\" path=\"WebContent/WEB-INF/classes\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/antlr.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/asm-attrs.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/asm.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/c3p0-0.9.1.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/cglib.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/commons-codec-1.3.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/commons-collections.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/commons-fileupload-1.2.1.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/commons-io-1.4.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/commons-lang-2.3.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/commons-logging.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/dom4j.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/ehcache-1.2.3.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/ejb3-persistence.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/hibernate-annotations.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/hibernate-commons-annotations.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/hibernate-search-3.1.1.GA.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/hibernate3.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/icu4j-3_8.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/jasypt-1.3.1.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/javaee.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/javassist-3.4.GA.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/jcaptcha-all-1.0-RC6.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/jta.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/junit.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/log4j.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/lucene-core-2.4.1.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/mail.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/markdownj.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/mysql-connector-java-5.1.6-bin.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/slf4j-api.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/slf4j-log4j12.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/sqlite-jdbc-3.6.17.1.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/strategoxt.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/urlrewrite.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/webdsl-support.jar\"/>\n");
         classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/xercesImpl.jar\"/>\n");
         classpathFile.append("</classpath>\n");
         writeStringToFile(classpathFile.toString(), project.getLocation()+"/.classpath"); 
     }
     
     public static void writeJdtPrefsFile(IProject project) throws IOException{
         StringBuffer jdtprefsFile = new StringBuffer();
         jdtprefsFile.append("eclipse.preferences.version=1\n");
         jdtprefsFile.append("org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n");
         jdtprefsFile.append("org.eclipse.jdt.core.compiler.compliance=1.6\n");
         jdtprefsFile.append("org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\n");
         jdtprefsFile.append("org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n");
         jdtprefsFile.append("org.eclipse.jdt.core.compiler.source=1.6\n");
         writeStringToFile(jdtprefsFile.toString(), project.getLocation()+"/.settings/org.eclipse.jdt.core.prefs");
     }
     public static void writeWstComponentFile(IProject project, boolean isRootApp) throws IOException{
         String projectName = project.getName();
         StringBuffer wstcomponentFile = new StringBuffer();
         wstcomponentFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         wstcomponentFile.append("<project-modules id=\"moduleCoreId\" project-version=\"1.5.0\">\n");
         wstcomponentFile.append("\t<wb-module deploy-name=\""+projectName+"\">\n");
         wstcomponentFile.append("\t\t<wb-resource deploy-path=\"/\" source-path=\"/WebContent\"/>\n");
         wstcomponentFile.append("\t\t<wb-resource deploy-path=\"/WEB-INF/classes\" source-path=\"/.servletapp/src\"/>\n");
         if(isRootApp){
           wstcomponentFile.append("\t\t<property name=\"context-root\" value=\"\"/>\n");
         }else{
           wstcomponentFile.append("\t\t<property name=\"context-root\" value=\""+projectName+"\"/>\n");
         }
         wstcomponentFile.append("\t\t<property name=\"java-output-path\"/>\n");
         wstcomponentFile.append("\t</wb-module>\n");
         wstcomponentFile.append("</project-modules>\n");
         writeStringToFile(wstcomponentFile.toString(), project.getLocation()+"/.settings/org.eclipse.wst.common.component");
     }
     
     public static void writeWstFacetFile(IProject project) throws IOException{
         StringBuffer wstfacetFile = new StringBuffer();
         wstfacetFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         wstfacetFile.append("<faceted-project>\n");
         wstfacetFile.append("\t<installed facet=\"jst.java\" version=\"6.0\"/>\n");
         wstfacetFile.append("\t<installed facet=\"jst.web\" version=\"2.4\"/>\n");
         wstfacetFile.append("</faceted-project>\n");
         writeStringToFile(wstfacetFile.toString(), project.getLocation()+"/.settings/org.eclipse.wst.common.project.facet.core.xml");
     }
     
     public static void writeTomcatConfigFile(String plugindir) throws IOException{
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         String tomcatdir = plugindir+"webdsl-template/tomcat/tomcat";
         String jre = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6";
         String workspacedir = workspace.getRoot().getRawLocation().toString();
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
         tomcatconfigFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Dcatalina.base=&quot;"+workspacedir+"/.metadata/.plugins/org.eclipse.wst.server.core/tmp0&quot; -Dcatalina.home=&quot;"+tomcatdir+"&quot; -Dwtp.deploy=&quot;"+workspacedir+"/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps&quot; -Djava.endorsed.dirs=&quot;"+tomcatdir+"/endorsed&quot; -Xss8m -Xms48m -Xmx1024m -XX:MaxPermSize=384m\"/>\n");
         tomcatconfigFile.append("\t<stringAttribute key=\"server-id\" value=\"webdsl_tomcat6server\"/>\n");
         tomcatconfigFile.append("</launchConfiguration>\n");
         IProject project = workspace.getRoot().getProject("Servers");
         writeStringToFile(tomcatconfigFile.toString(), project.getLocation()+"/Tomcat v6.0 Server at localhost.launch");
         refreshProject(project);
     }
     
     /**
      * Using this .project file will prevent the webdsl builder from running if eclipse is set to 'build automatically'.
      * The configuration as server module needs to be created before running an actual build.
      * @param project
      */
     public static void writeProjectFileWithoutWebDSLBuilder(IProject project){
         //overwrite .project file with correct settings
         StringBuffer projectFile = new StringBuffer();
         projectFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         projectFile.append("<projectDescription>\n");
         projectFile.append("\t<name>"+project.getName()+"</name>\n");
         projectFile.append("\t<buildSpec>\n");
         projectFile.append("\t\t<buildCommand>\n");
         projectFile.append("\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n");
         projectFile.append("\t\t</buildCommand>\n");
         projectFile.append("\t\t<buildCommand>\n");
         projectFile.append("\t\t\t<name>org.eclipse.wst.common.project.facet.core.builder</name>\n");
         projectFile.append("\t\t</buildCommand>\n");
         projectFile.append("\t</buildSpec>\n");
         projectFile.append("\t<natures>\n");
         projectFile.append("\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n");
         projectFile.append("\t\t<nature>org.eclipse.wst.common.project.facet.core.nature</nature>\n");
         projectFile.append("\t\t<nature>org.eclipse.wst.common.modulecore.ModuleCoreNature</nature>\n");
         projectFile.append("\t\t<nature>org.eclipse.jem.workbench.JavaEMFNature</nature>\n");
         projectFile.append("\t</natures>\n");
         projectFile.append("</projectDescription>\n");
         try {
             writeStringToFile(projectFile.toString(), project.getLocation()+"/.project");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     public static void writeProjectFile(IProject project){
        //overwrite .project file with correct settings
         StringBuffer projectFile = new StringBuffer();
         projectFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         projectFile.append("<projectDescription>\n");
         projectFile.append("\t<name>"+project.getName()+"</name>\n");
         projectFile.append("\t<buildSpec>\n");

         projectFile.append("\t\t<buildCommand>\n");
         projectFile.append("\t\t\t<name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n");
         projectFile.append("\t\t\t<triggers>full,incremental,</triggers>\n");
         projectFile.append("\t\t\t<arguments><dictionary>\n");
         projectFile.append("\t\t\t\t<key>LaunchConfigHandle</key>\n");
         projectFile.append("\t\t\t\t<value>&lt;project&gt;/"+project.getName()+" build.xml.launch</value>\n");
         projectFile.append("\t\t\t</dictionary></arguments>\n");
         projectFile.append("\t\t</buildCommand>\n");

         projectFile.append("\t\t<buildCommand>\n");
         projectFile.append("\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n");
         projectFile.append("\t\t</buildCommand>\n");
         //projectFile.append("\t\t<buildCommand>\n");
         //projectFile.append("\t\t\t<name>org.eclipse.wst.common.project.facet.core.builder</name>\n");
         //projectFile.append("\t\t</buildCommand>\n");
         //projectFile.append("\t\t<buildCommand>\n");
         //projectFile.append("\t\t\t<name>org.eclipse.wst.validation.validationbuilder</name>\n");
         //projectFile.append("\t\t</buildCommand>\n");

         projectFile.append("\t\t<buildCommand><name>webdsl.editor.builder</name></buildCommand>\n");

         //clean trigger
         projectFile.append("\t\t<buildCommand>\n");
         projectFile.append("\t\t\t<name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n");
         projectFile.append("\t\t\t<triggers>clean,</triggers>\n");
         projectFile.append("\t\t\t<arguments><dictionary>\n");
         projectFile.append("\t\t\t\t<key>LaunchConfigHandle</key>\n");
         projectFile.append("\t\t\t\t<value>&lt;project&gt;/"+project.getName()+" clean-project.xml.launch</value>\n");
         projectFile.append("\t\t\t</dictionary></arguments>\n");
         projectFile.append("\t\t</buildCommand>\n");
         projectFile.append("\t</buildSpec>\n");
         
         projectFile.append("\t<natures>\n");
         projectFile.append("\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n");
         projectFile.append("\t\t<nature>org.eclipse.wst.common.project.facet.core.nature</nature>\n");
         projectFile.append("\t\t<nature>org.eclipse.wst.common.modulecore.ModuleCoreNature</nature>\n");
         projectFile.append("\t\t<nature>org.eclipse.jem.workbench.JavaEMFNature</nature>\n");
         projectFile.append("\t</natures>\n");
         projectFile.append("</projectDescription>\n");
         try {
            writeStringToFile(projectFile.toString(), project.getLocation()+"/.project");
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
     
     /*
      * add version to server instance String, otherwise builds break after
      * updating the plugin, due to stale references in 
      * -workspace-/.metadata/.plugins/org.eclipse.wst.server.core/
      * to the tomcat installation of the previous version
      */
     public static String tomcatruntimeid = 
         "webdsl_tomcat6runtime" + Activator.getInstance().getBundle().getVersion().toString(); 
     public static String tomcatserverid = 
         "webdsl_tomcat6server" + Activator.getInstance().getBundle().getVersion().toString(); 
     
     public static IRuntimeType getTomcatRuntimeType(){
         IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null, null);
         IRuntimeType tomcat6runtimetype = null;
         if (runtimeTypes != null) {
             int size = runtimeTypes.length;
             for (int i = 0; i < size; i++) {
                 IRuntimeType runtimeType = runtimeTypes[i];
                 //System.out.println("  "+ runtimeType +"    "+ runtimeType.getName());
                 if(runtimeType.getName().equals("Apache Tomcat v6.0")){
                     tomcat6runtimetype = runtimeType;
                 }
             }
         }
         System.out.println("runtime type: "+tomcat6runtimetype);
         return tomcat6runtimetype;
     }
     public static IRuntime getWebDSLTomcatRuntime(){
         IRuntime[] runtimes = ServerUtil.getRuntimes(null, null);
         IRuntime plugintomcat6runtime = null;
         for(IRuntime ir : runtimes){
             System.out.println(ir.getId());
             if(ir.getId().equals(tomcatruntimeid)){
                 plugintomcat6runtime = ir;
             }
         }
         System.out.println("runtime: "+plugintomcat6runtime);
         return plugintomcat6runtime;
     }
     public static IRuntime createWebDSLTomcatRuntime(String plugindir, IProgressMonitor monitor) throws CoreException{
             IRuntimeType tomcat6runtimetype = getTomcatRuntimeType();
             IRuntimeWorkingCopy rwc = tomcat6runtimetype.createRuntime(tomcatruntimeid, monitor);
             rwc.setLocation(Path.fromOSString(plugindir+"/webdsl-template/tomcat/tomcat"));
             //System.out.println("Location of Tomcat 6 runtime: "+rwc.getLocation());
             IRuntime rt = rwc.save(true, monitor);
             System.out.println("created runtime: "+rt);
             return rt;
     }
     /**
      * add tomcat 6 runtime for webdsl plugin if not created yet
      * @param plugindir
      * @param monitor
      * @return
      * @throws CoreException
      */
     public static IRuntime getOrCreateWebDSLTomcatRuntime(String plugindir, IProgressMonitor monitor)throws CoreException{
         IRuntime plugintomcat6runtime = getWebDSLTomcatRuntime();
         if(plugintomcat6runtime == null){
             plugintomcat6runtime = createWebDSLTomcatRuntime(plugindir,monitor);
         }
         System.out.println("get or create runtime: "+plugintomcat6runtime);
         return plugintomcat6runtime;
     }
     
     
     public static IServer getWebDSLTomcatServer(IProject project,IProgressMonitor monitor){
         IServer plugintomcat6server = null;
         for(IServer serv : ServerUtil.getAvailableServersForModule(ServerUtil.getModule(project), true, monitor)){
             if(serv.getId().equals(tomcatserverid)){
                 plugintomcat6server = serv;
             }
         } 
         System.out.println("server: "+plugintomcat6server);
         return plugintomcat6server;
     }
     public static IServer createWebDSLTomcatServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IRuntime plugintomcat6runtime = getOrCreateWebDSLTomcatRuntime(plugindir,monitor);
         IRuntimeType tomcat6runtimetype = getTomcatRuntimeType();
         IServerType st = getCompatibleServerType(tomcat6runtimetype);
         //System.out.println(st);
         IServer plugintomcat6server = null;
         IServerWorkingCopy server = st.createServer(tomcatserverid, null, plugintomcat6runtime, monitor);
         plugintomcat6server = server.saveAll(true, monitor); //saveAll will also save ServerConfiguration and Runtime if they were still WorkingCopy
         System.out.println("created server: "+plugintomcat6server);
         copyKeystoreFile(project, plugindir);         
         return plugintomcat6server;
     }
     public static void copyKeystoreFile(IProject project, String plugindir){
        try {
            copyFile(plugindir+"/webdsl-template/template-java-servlet/tomcat/.keystore",project.getWorkspace().getRoot().getLocation()+"/Servers/.keystore");
        } catch (IOException e) {
            e.printStackTrace();
        }   
     }
     /**
      * add tomcat 6 server for webdsl plugin of not created yet
      * @param project
      * @param monitor
      * @return
      * @throws CoreException
      */
     public static IServer getOrCreateWebDSLTomcatServer(IProject project, String plugindir, IProgressMonitor monitor) throws CoreException{
         IServer plugintomcat6server = getWebDSLTomcatServer(project,monitor);
         if(plugintomcat6server==null){
             plugintomcat6server = createWebDSLTomcatServer(project, plugindir, monitor);
         }
         System.out.println("get or create server: "+plugintomcat6server);
         return plugintomcat6server;
     }
     
    public static void addProjectModuleToServer(IProject project, IServer plugintomcat6server, IProgressMonitor monitor) throws CoreException{
         IServerWorkingCopy serveraddmodule = new ServerWorkingCopy((Server) plugintomcat6server);
         // attach the project module to the server config
         IModule[] modules = {ServerUtil.getModule(project)};
         serveraddmodule.modifyModules(modules, null, null);
         serveraddmodule.saveAll(false,monitor);
     }
     
     public static void initWtpServerConfig(String plugindir, final IProject project, final String projectName, IProgressMonitor monitor) throws CoreException{
         IServer plugintomcat6server = getOrCreateWebDSLTomcatServer(project,plugindir,monitor);
         addProjectModuleToServer(project,plugintomcat6server,monitor);
     }
     
     //copy from org.eclipse.wst.server.ui.internal.wizard.page.NewRuntimeComposite (protected access)
     protected static IServerType getCompatibleServerType(IRuntimeType runtimeType) {
         List<IServerType> list = new ArrayList<IServerType>();
         IServerType[] serverTypes = ServerCore.getServerTypes();
         int size = serverTypes.length;
         for (int i = 0; i < size; i++) {
             IRuntimeType rt = serverTypes[i].getRuntimeType();
             if (rt != null && rt.equals(runtimeType))
                 list.add(serverTypes[i]);
         }
         if (list.size() == 1)
             return list.get(0);
         return null;
     }
     
     public static void writeStringToFile(String s, String file) throws IOException{
         FileOutputStream in = null;
         try{
             File buildxml = new File(file);
             in = new FileOutputStream(buildxml);
            FileChannel fchan = in.getChannel();
            BufferedWriter bf = new BufferedWriter(Channels.newWriter(fchan,"UTF-8"));
            bf.write(s);
            bf.close();
         }
         finally{
             if(in != null){
                 in.close();
             }
         }
     }
     
     public static void createDirs(String dirs){
         new File(dirs).mkdirs();
     }
     
     public static void copyFile(String ssource, String sdest) throws IOException {
         System.out.println("Copying "+ssource+" to "+sdest);
         File dest = new File(sdest);
         File source = new File(ssource);
         if(!dest.exists()) {
             dest.createNewFile();
         }
         FileChannel in = null;
         FileChannel out = null;
         try {
             in = new FileInputStream(source).getChannel();
             out = new FileOutputStream(dest).getChannel();
             out.transferFrom(in, 0, in.size());
         }
         finally {
             if(in != null) {
                 in.close();
             }
             if(out != null) {
                 out.close();
             }
         }
     }

    private static void refreshProject(final IProject project) {
        try {
            NullProgressMonitor monitor = new NullProgressMonitor();
            project.refreshLocal(DEPTH_INFINITE, monitor);
            project.close(monitor);
            project.open(monitor);
            
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

}
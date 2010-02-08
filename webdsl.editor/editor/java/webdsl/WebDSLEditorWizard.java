package webdsl;

import static org.eclipse.core.resources.IResource.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.spoofax.interpreter.core.Interpreter;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoErrorExit;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.StrategoExit;

/**
 * A wizard for creating new WebDSL projects.
 */
public class WebDSLEditorWizard extends Wizard implements INewWizard {

	private final WebDSLEditorWizardPage input = new WebDSLEditorWizardPage();
	
	private IProject lastProject;

	// TODO: Support external directory and working set selection in wizard
			
	public WebDSLEditorWizard() {
		setNeedsProgressMonitor(true);
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
		System.out.println(appName+projectName);
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(appName, projectName, isMysqlSelected, host, user, pass, name, mode, file, tomcatpath, smtphost, smtpport, smtpuser, smtppass, monitor);
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
	
 	private void doFinish(String appName, String projectName, boolean isMysqlSelected, String host, String user, String pass, String name, String mode, String file, String tomcatpath, String smtphost, String smtpport, String smtpuser, String smtppass, IProgressMonitor monitor) throws IOException, CoreException {
 		final int TASK_COUNT = 3;
		lastProject = null;
		monitor.beginTask("Creating " + appName + " application", TASK_COUNT);
		
		//EditorIOAgent agent = new EditorIOAgent();
		//agent.setAlwaysActivateConsole(true);
		//Context context = new Context(Environment.getTermFactory(), agent);
		//context.registerClassLoader(make_permissive.class.getClassLoader());
		//sdf2imp.init(context);
		
		

		monitor.setTaskName("Creating Eclipse project");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = lastProject = workspace.getRoot().getProject(projectName);
		project.create(null);
		project.open(null);
		monitor.worked(1);
		
		
		monitor.setTaskName("Copying example application files");

		//agent.setWorkingDir(project.getLocation().toOSString());
     	String plugindir = webdsl.WebDSLEditorWizard.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (Platform.getOS().equals(Platform.OS_WIN32)) { // FIXME: proper paths on Windows
			plugindir = plugindir.substring(1);
			//jar2 = jar2.substring(1);
			//jar3 = jar3.substring(1);
		}
		/*
		if (!jar1.endsWith(".jar")) { // ensure correct jar at development time
			String jar1a = jar1 + "/../strategoxt.jar";
			if (new File(jar1a).exists()) jar1 = jar1a;
			jar1a = jar1 + "/java/strategoxt.jar";
			if (new File(jar1a).exists()) jar1 = jar1a;
		}*/
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
			out.close(); 
		} 
		catch (IOException e) { 
			Environment.logException(e);
			throw e;
		} 
		
		copyFile(plugindir+"webdsl-template/new_project/templates.app", project.getLocation()+"/templates.app");
		copyFile(plugindir+"webdsl-template/new_project/APPLICATION_NAME.app", project.getLocation()+"/"+appName+".app");
		createDirs(project.getLocation()+"/images");
		copyFile(plugindir+"webdsl-template/new_project/images/logosmall.png", project.getLocation()+"/images/logosmall.png");
		createDirs(project.getLocation()+"/stylesheets");
		copyFile(plugindir+"webdsl-template/new_project/stylesheets/common_.css", project.getLocation()+"/stylesheets/common_.css");
		
		monitor.worked(1);
		
		StringBuffer ant = new StringBuffer();
		ant.append("<project name=\"webdsl-eclipse-plugin\" default=\"plugin-eclipse-build\">\n");
		//ant.append("\t<property name=\"plugindir\" value=\""+plugindir+"\" />\n");
		ant.append("\t<fail unless=\"plugindir\" message=\"WebDSL plugin is not correctly installed. The 'plugindir' property is not available.\" />\n");
		ant.append("\t<property name=\"projectdir\" value=\""+project.getLocation()+"\" />\n");
		ant.append("\t<property name=\"templatedir\" value=\"${plugindir}/webdsl-template\"/>\n");
		ant.append("\t<property name=\"currentdir\" value=\"${projectdir}\"/>\n");
		ant.append("\t<property name=\"webdslexec\" value=\"java -ss4m -cp '${plugindir}/include/webdsl.jar' org.webdsl.webdslc.Main\"/>\n");
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
		
		//create build launch file to make sure ant uses same jre instance as eclipse, otherwise the plugindir property provider won't work
		StringBuffer buildLaunchFile = new StringBuffer();
		buildLaunchFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
	    buildLaunchFile.append("<launchConfiguration type=\"org.eclipse.ant.AntLaunchConfigurationType\">\n");
		buildLaunchFile.append("\t<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
		buildLaunchFile.append("\t<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n");
		buildLaunchFile.append("\t\t<listEntry value=\"/"+appName+"/build.xml\"/>\n");
		buildLaunchFile.append("\t</listAttribute>\n");
		buildLaunchFile.append("\t<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n");
		buildLaunchFile.append("\t\t<listEntry value=\"1\"/>\n");
		buildLaunchFile.append("\t</listAttribute>\n");
		buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\""+appName+"\"/>\n");
		buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_ANT_TARGETS\" value=\"plugin-run,\"/>\n");
		buildLaunchFile.append("\t<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/"+appName+"/build.xml}\"/>\n");
		buildLaunchFile.append("\t<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
		buildLaunchFile.append("</launchConfiguration>\n");
		writeStringToFile(buildLaunchFile.toString(), project.getLocation()+"/"+appName+" build.xml.launch");
		
		//overwrite .project file with correct settings
		StringBuffer projectFile = new StringBuffer();
		projectFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		projectFile.append("<projectDescription>\n");
		projectFile.append("\t<name>"+projectName+"</name>\n");
		projectFile.append("\t<buildSpec>\n");
		projectFile.append("\t\t<buildCommand>\n");
		projectFile.append("\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n");
		projectFile.append("\t\t</buildCommand>\n");
		projectFile.append("\t\t<buildCommand>\n");
		projectFile.append("\t\t\t<name>org.eclipse.wst.common.project.facet.core.builder</name>\n");
		projectFile.append("\t\t</buildCommand>\n");
		//projectFile.append("\t\t<buildCommand>\n");
		//projectFile.append("\t\t\t<name>org.eclipse.wst.validation.validationbuilder</name>\n");
		//projectFile.append("\t\t</buildCommand>\n");
		projectFile.append("\t</buildSpec>\n");
		projectFile.append("\t<natures>\n");
		projectFile.append("\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n");
		projectFile.append("\t\t<nature>org.eclipse.wst.common.project.facet.core.nature</nature>\n");
		projectFile.append("\t\t<nature>org.eclipse.wst.common.modulecore.ModuleCoreNature</nature>\n");
		projectFile.append("\t\t<nature>org.eclipse.jem.workbench.JavaEMFNature</nature>\n");
		projectFile.append("\t</natures>\n");
		projectFile.append("</projectDescription>\n");
		writeStringToFile(projectFile.toString(), project.getLocation()+"/.project");
		
		//write a .classpath for java nature of project
		StringBuffer classpathFile = new StringBuffer();
		classpathFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		classpathFile.append("<classpath>\n");
		classpathFile.append("\t<classpathentry kind=\"src\" path=\".servletapp/src\"/>\n");
		classpathFile.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>\n");
		classpathFile.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jst.j2ee.internal.web.container\"/>\n");
		classpathFile.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jst.j2ee.internal.module.container\"/>\n");
		classpathFile.append("\t<classpathentry kind=\"output\" path=\"WebContent/WEB-INF/classes\"/>\n");
		classpathFile.append("\t<classpathentry kind=\"lib\" path=\".servletapp/bin/WEB-INF/lib/*.jar\"/>\n");
		classpathFile.append("</classpath>\n");
		writeStringToFile(classpathFile.toString(), project.getLocation()+"/.classpath");
		
		//write .settings/* files
		createDirs(project.getLocation()+"/.settings");
		
		StringBuffer jdtprefsFile = new StringBuffer();
		jdtprefsFile.append("eclipse.preferences.version=1\n");
		jdtprefsFile.append("org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n");
		jdtprefsFile.append("org.eclipse.jdt.core.compiler.compliance=1.6\n");
		jdtprefsFile.append("org.eclipse.jdt.core.compiler.problem.assertIdentifier=error\n");
		jdtprefsFile.append("org.eclipse.jdt.core.compiler.problem.enumIdentifier=error\n");
		jdtprefsFile.append("org.eclipse.jdt.core.compiler.source=1.6\n");
		writeStringToFile(jdtprefsFile.toString(), project.getLocation()+"/.settings/org.eclipse.jdt.core.prefs");
		
		StringBuffer wstcomponentFile = new StringBuffer();
		wstcomponentFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		wstcomponentFile.append("<project-modules id=\"moduleCoreId\" project-version=\"1.5.0\">\n");
		wstcomponentFile.append("\t<wb-module deploy-name=\""+projectName+"\">\n");
		wstcomponentFile.append("\t\t<wb-resource deploy-path=\"/\" source-path=\"/bin\"/>\n");
		wstcomponentFile.append("\t\t<wb-resource deploy-path=\"/WEB-INF/classes\" source-path=\"/.servletapp/src\"/>\n");
		wstcomponentFile.append("\t\t<property name=\"context-root\" value=\""+projectName+"\"/>\n");
		wstcomponentFile.append("\t\t<property name=\"java-output-path\"/>\n");
		wstcomponentFile.append("\t</wb-module>\n");
		wstcomponentFile.append("</project-modules>\n");
		writeStringToFile(wstcomponentFile.toString(), project.getLocation()+"/.settings/org.eclipse.wst.common.component");
		
		StringBuffer wstfacetFile = new StringBuffer();
		wstfacetFile.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		wstfacetFile.append("<faceted-project>\n");
		wstfacetFile.append("\t<installed facet=\"jst.java\" version=\"6.0\"/>\n");
		wstfacetFile.append("\t<installed facet=\"jst.web\" version=\"2.4\"/>\n");
		wstfacetFile.append("</faceted-project>\n");
		writeStringToFile(wstfacetFile.toString(), project.getLocation()+"/.settings/org.eclipse.wst.common.project.facet.core.xml");
		
/*
 * 
		monitor.worked(3);*/
/*
		monitor.setTaskName("Acquiring workspace lock"); // need root lock for builder
		IWorkspaceRoot root = project.getWorkspace().getRoot();
		Job.getJobManager().beginRule(root, monitor); // avoid ant builder launching
		try {
			monitor.setTaskName("Acquiring environment lock");
			monitor.worked(1);
			synchronized (Environment.getSyncRoot()) { // avoid background editor loading
				monitor.setTaskName("Loading new resources");
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				monitor.worked(1);
				
				monitor.setTaskName("Building and loading example editor");
				project.build(IncrementalProjectBuilder.FULL_BUILD, null);
				monitor.worked(6);

				// TODO: Optimize - don't reload editor (already done from Ant file)
				// DynamicDescriptorLoader.getInstance().forceNoUpdate(descriptor);
				monitor.setTaskName("Loading editor");
				IResource descriptor = project.findMember("include/" + languageName + ".packed.esv");
				DynamicDescriptorLoader.getInstance().forceUpdate(descriptor);
				monitor.worked(2);

				//project.refreshLocal(DEPTH_INFINITE, new NullProgressMonitor());
				monitor.worked(1);
			}
		} finally {
			Job.getJobManager().endRule(root);
		}

		monitor.setTaskName("Opening editor tabs");
		Display display = getShell().getDisplay();
		EditorState.asyncOpenEditor(display, project.getFile("/trans/" + toStrategoName(languageName) +  ".str"), true);
		monitor.worked(2);
		EditorState.asyncOpenEditor(display, project.getFile("/editor/" + languageName +  ".main.esv"), true);
		monitor.worked(1);
		EditorState.asyncOpenEditor(display, project.getFile("/syntax/" + languageName +  ".sdf"), true);
		monitor.worked(1);
		EditorState.asyncOpenEditor(display, project.getFile("/test/example." + extensions.split(",")[0]), false);*/
		refreshProject(project);
		
		monitor.setTaskName("Opening editor tabs");
		//wait a second for refresh of project
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Display display = getShell().getDisplay();
		EditorState.asyncOpenEditor(display, project.getFile("templates.app"), true);
		EditorState.asyncOpenEditor(display, project.getFile(appName+".app"), true);
		monitor.worked(1);
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

	private void refreshProject(final IProject project) {
		// We schedule a project refresh to make all ".generated" files readable
		Job job = new Job("Refreshing project") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (Environment.getSyncRoot()) {}; // wait for update thread
				try {
					project.refreshLocal(DEPTH_INFINITE, new NullProgressMonitor());
				} catch (CoreException e) {
					// Ignore
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(0);
	}

}
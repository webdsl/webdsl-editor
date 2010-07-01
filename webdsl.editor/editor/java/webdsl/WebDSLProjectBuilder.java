package webdsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchDelegate;
import org.eclipse.ant.internal.ui.launchConfigurations.AntLaunchShortcut;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.AntProjectNodeProxy;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
import org.strategoxt.lang.terms.StrategoAppl;

public final class WebDSLProjectBuilder extends IncrementalProjectBuilder{
    
    protected IProject[] build( final int kind, 
                                final Map args, 
                                final IProgressMonitor monitor ) throws CoreException {
        if( monitor != null ){
            monitor.beginTask( "Building WebDSL project", 1 );
        }
        
        try{
            final IProject project = getProject();
            
            String buildid=null;
            try{
                FileReader input = new FileReader(project.getLocation().toString()+"/.servletapp/.last-build-id");
                BufferedReader bufRead = new BufferedReader(input);
                String line = bufRead.readLine();
                buildid=line;
                bufRead.close();
            }
            catch(Exception e){
              System.out.println("Could not find a build-id in .servletapp/.last-build-id");
            }
            
            setPublishListener(project, monitor, buildid);
            project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            tryStartServer(project,monitor);
            
            worked( monitor, 1 );
            //System.out.println("build done");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if( monitor != null ){
                monitor.done();
            }
        }
        return new IProject[0];
    }
    
    private static void worked( final IProgressMonitor monitor,
                                final int ticks ){
        if( monitor != null ){
            monitor.worked( ticks );
            
            if( monitor.isCanceled() ){
                throw new OperationCanceledException();
            }
        }
    }
    
    /*
     * tried several ways of calling ant, but none of them is working as needed, ant is now called from .project builder directly instead
     */
    public static void runAntBuild(IProject project, IProgressMonitor monitor, String buildid) throws CoreException{
        
        File buildFile = new File(project.getLocation().toString()+"/build.xml");
        
        AntLaunchShortcut launcher = new AntLaunchShortcut();
        IPath p = Path.fromPortableString(buildFile.toString());
        System.out.println(p);
        launcher.setShowDialog(false);
        launcher.launch(new AntProjectNodeProxy(buildFile.toString()),org.eclipse.debug.core.ILaunchManager.RUN_MODE);
        
        /*
        AntLaunchShortcut launcher = new AntLaunchShortcut();
        IPath p = Path.fromPortableString(buildFile.toString());
        System.out.println(p);
        launcher.launch(p, project, org.eclipse.debug.core.ILaunchManager.RUN_MODE, "plugin-eclipse-build");
        */
        
        /*
        AntRunner builder = new AntRunner();
        builder.setBuildFileLocation(buildFile.toString());
        //builder.setMessageOutputLevel(org.apache.tools.ant.Project.MSG_INFO);
        System.out.println(new webdsl.AntConsoleLogger().getClass().getName());
        builder.addBuildLogger(new webdsl.AntConsoleLogger().getClass().getName());
        builder.run(monitor);
        */
        
        //non-eclipse ant runner, log will not show up in user console, eclipse tasks will not be available
        /*   
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        
        String plugindir = new WebDSLJarAntPropertyProvider().getAntPropertyValue("plugindir");
        String strjdir = StrategoAppl.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        System.out.println(strjdir);
        p.setUserProperty("plugindir", plugindir);
        p.setUserProperty("stratego-jar-cp",strjdir);
        p.setUserProperty("build-id",buildid);
        
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);

        DefaultLogger consoleLogger = new webdsl.AntConsoleLogger(); //new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        p.addBuildListener(consoleLogger);
        
        p.executeTarget("plugin-eclipse-build");*/
    }
    
    
    public static IServer getTomcatServer(IProject project, IProgressMonitor monitor){
        IServer[] servers = ServerUtil.getServersByModule(ServerUtil.getModule(project), monitor);
        for(IServer s :  servers){
          return s;
        }
        return null;
    }
    

    //quick hack, missing a removeAllPublishListeners method in Server
    public static IPublishListener previouslyAddedPublishListener = null;
    public static void tryRemovePreviousListener(IServer server){
        if(previouslyAddedPublishListener != null){
            server.removePublishListener(previouslyAddedPublishListener);
            previouslyAddedPublishListener = null;
          }
    }
    public static void setPublishListener(final IProject project,IProgressMonitor monitor, final String buildid) throws CoreException{
         final IServer tomcatserver = getTomcatServer(project,monitor);
         tryRemovePreviousListener(tomcatserver);
        
        IPublishListener publishListener = null;
        if(buildid != null){
          publishListener = new PublishAdapter() {
            public void publishStarted(IServer server) {
                //showServersView(false);
            }

            public void publishFinished(IServer server, IStatus status) {
                //showServersView(false);
                System.out.println("status: " + status);
                Job job = new Job("start server") { 
                    public IStatus run(IProgressMonitor monitor){
                        boolean deployed = pollDeployedAppForNewBuildId(project,buildid);
                        if(deployed){
                            //opens default external browser     
                            try {
                                IWorkbenchBrowserSupport browserSupport = ServerUIPlugin.getInstance().getWorkbench().getBrowserSupport();
                                IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
                                browser.openURL(new URL("http://localhost:8080/"+project.getName()));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                            // after succesful open, remove listener to avoid having the browser popup each time a change is made in servers publish state
                            tryRemovePreviousListener(tomcatserver);
                        }
                        
                        return Status.OK_STATUS;
                    }  
                };
                job.schedule(3000);
            }
          };
          System.out.println("Adding publish listener.");
          tomcatserver.addPublishListener(publishListener);
        }
       
        previouslyAddedPublishListener = publishListener;
    }
    

    public static boolean pollDeployedAppForNewBuildId(IProject project, String buildid){
        URL url = null;
        try {
            url = new URL("http://localhost:8080/"+project.getName()+"/?show_build_id=true");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String searchfor = "build-id:"+buildid;
        System.out.println("searching for: "+searchfor);
        boolean found = false;
        int tries = 3;
        while(tries > 0 && !found){
          tries = tries - 1;
          try {
            InputStreamReader instream = new InputStreamReader(url.openStream());
            BufferedReader inreader = new BufferedReader(instream);
            String inputLine = inreader.readLine();
            while (inputLine != null) {
              if(inputLine.contains(searchfor)){ 
                  found = true; 
              }
              inputLine = inreader.readLine();
            }
            inreader.close();
          } catch (Exception e) {
            //e.printStackTrace();
          }
          try {
            Thread.sleep (2000);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if(found){
          System.out.println("Application deployed.");
        }
        else{
          System.out.println("Application not deployed yet.");
        }
        return found;
    }
    
    public static void tryStartServer(IProject project,IProgressMonitor monitor) throws CoreException{
        System.out.println("Polling server status.");
        IServer tomcatserver = getTomcatServer(project,monitor);
        if(tomcatserver.canStart(org.eclipse.debug.core.ILaunchManager.RUN_MODE).equals(Status.OK_STATUS)){
            System.out.println("Starting server.");
            getTomcatServer(project,monitor).start(org.eclipse.debug.core.ILaunchManager.RUN_MODE,monitor);
        }
        else{
            System.out.println("Server already started.");
        }
    }
    
}

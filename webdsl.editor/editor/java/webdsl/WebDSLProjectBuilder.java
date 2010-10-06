package webdsl;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;
@SuppressWarnings("restriction")
public final class WebDSLProjectBuilder extends IncrementalProjectBuilder{

    @SuppressWarnings("unchecked")
    protected IProject[] build( final int kind,
                                final Map args,
                                final IProgressMonitor monitor ) throws CoreException {
        //if(kind == IncrementalProjectBuilder.AUTO_BUILD){
        //    System.out.println("auto build disabled in WebDSLProjectBuilder");
        //    return new IProject[0];
        //}
        
        if( monitor != null ){
            monitor.beginTask( "Building WebDSL project", 1 );
        }

        try{
            final IProject project = getProject();

            final String buildid = getBuildIdCompleted(project);

            //check that last build completed, sometimes this builder is started when compiler is still running
            if(buildid != null && !isWebDSLProjectBuilderStarted(project)){
              markWebDSLProjectBuilderStarted(project);
              //addRefreshJob(project,defaultDelay);
              //setPublishListener(project, monitor, buildid);
              tryStartServer(project, monitor, new ChainedJob(){ public void run(){ pollDeployedAppAndOpenBrowser(project, buildid, 0); }}, 0);
              //pollDeployedAppAndOpenBrowser(project, buildid, 1000);
            }
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

    public static String getBuildId(IProject project){
        return getIdFromFile(project,".last-build-id");
    }
    public static String getBuildIdCompleted(IProject project){
        return getIdFromFile(project,".last-build-id-completed");
    }
    public static boolean isWebDSLProjectBuilderStarted(IProject project){
        return getIdFromFile(project,".webdsl-project-builder-started") != null;
    }
    public static void markWebDSLProjectBuilderStarted(IProject project){
        try {
            WebDSLEditorWizard.writeStringToFile("started", project.getLocation()+"/.servletapp/.webdsl-project-builder-started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getIdFromFile(IProject project, String fileName){
        String buildid = null;
        try{
            FileReader input = new FileReader(project.getLocation().toString()+"/.servletapp/"+fileName);
            BufferedReader bufRead = new BufferedReader(input);
            String line = bufRead.readLine();
            buildid = line;
            bufRead.close();
        }
        catch(Exception e){
            System.out.println("Could not find a build id in .servletapp/"+fileName);
        }
        return buildid;
    }

    public static boolean isRootApp(IProject project){
      if("true".equals(getProperty(project, "rootapp"))){
        return true;
      }
      return false;
    }

    public static String getProperty(IProject project, String p){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(project.getLocation().toString()+"/application.ini"));
            return (String) properties.get(p);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
    /*
    public static void runAntBuild(IProject project, IProgressMonitor monitor, String buildid) throws CoreException{

        File buildFile = new File(project.getLocation().toString()+"/build.xml");

        AntLaunchShortcut launcher = new AntLaunchShortcut();
        IPath p = Path.fromPortableString(buildFile.toString());
        System.out.println(p);
        launcher.setShowDialog(false);
        launcher.launch(new AntProjectNodeProxy(buildFile.toString()),org.eclipse.debug.core.ILaunchManager.RUN_MODE);
      */
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
   // }


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
    
    
    public static void pollDeployedAppAndOpenBrowser(final IProject project, final String buildid, int delay){
      if(buildid != null){
        Job job = new Job("poll deployed app and open browser") {
           public IStatus run(IProgressMonitor monitor){
               boolean deployed = WebDSLProjectBuilder.pollDeployedAppForNewBuildId(project,buildid);
               if(deployed){
                   //opens default external browser
                   try {
                       IWorkbenchBrowserSupport browserSupport = ServerUIPlugin.getInstance().getWorkbench().getBrowserSupport();
                       IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
                       browser.openURL(new URL(WebDSLProjectBuilder.getAppUrl(project)));
                   } catch (MalformedURLException e) {
                       e.printStackTrace();
                   } catch (PartInitException e) {
                       e.printStackTrace();
                   }
               }
               return Status.OK_STATUS;
           }
         };
         job.schedule(delay);
      }
   }
    

    public static int defaultDelay = 1000;

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
                Job job = new Job("poll deployed app and open browser") {
                    public IStatus run(IProgressMonitor monitor){
                        boolean deployed = pollDeployedAppForNewBuildId(project,buildid);
                        if(deployed){
                            //opens default external browser
                            try {
                                IWorkbenchBrowserSupport browserSupport = ServerUIPlugin.getInstance().getWorkbench().getBrowserSupport();
                                IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
                                browser.openURL(new URL(getAppUrl(project)));
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
                job.schedule(defaultDelay);
            }
          };
          System.out.println("Adding publish listener.");
          tomcatserver.addPublishListener(publishListener);
        }

        previouslyAddedPublishListener = publishListener;
    }

    public static String getAppUrl(IProject project){
        if(isRootApp(project)){
            return "http://localhost:8080/";
        }
        else{
            return "http://localhost:8080/"+project.getName()+"/";
        }
    }

    public static boolean pollDeployedAppForNewBuildId(IProject project, String buildid){
        URL url = null;
        try {
            url = new URL(getAppUrl(project)+"?show_build_id=true");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String searchfor = "build-id:"+buildid;
        System.out.println("searching for: "+searchfor);
        boolean found = false;
        int tries = 20;
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
    
    public static boolean forcePublish = true;

    //restart once for a new project
    public static List<String> alreadyStarted = new ArrayList<String>();

    public static void tryStartServer(IProject project, IProgressMonitor monitor, ChainedJob cj, int delay) throws CoreException{
        //open 'Servers' project if closed, otherwise tomcat will not start
        IProject servers = ResourcesPlugin.getWorkspace().getRoot().getProject("Servers");
        if(!servers.isOpen()){
            servers.open(monitor);
        }

        final IServer server = getTomcatServer(project,monitor);

        if(forcePublish){
            // invoke publish first time, the initial start tends to hang when
            //   executed before publishing task completes.
            if(!alreadyStarted.contains(project.getName())){
                //addPublishJob(server, project, defaultDelay);
                publish(server, monitor);
            }
        }
        addCheckServerStartedJob(server,project,cj,delay);
    }
    
    public static boolean restartWhenNewAppIsBuildFirstTime = true;

    /*
     * if server start/restart is not executed asynchronously from a job it tends to hang
     */
    public static void addCheckServerStartedJob(final IServer server, final IProject project, final ChainedJob cj, int delay){
        Job job = new Job("check server status") {
            public IStatus run(IProgressMonitor monitor){
                checkServerStarted(server,project,cj);
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }
    
    public static void checkServerStarted(IServer server, IProject project, ChainedJob cj){
           System.out.println("Polling server status.");
            if(server.canStart(org.eclipse.debug.core.ILaunchManager.RUN_MODE).equals(Status.OK_STATUS)){
                addStartServerJob(server,cj,defaultDelay);
            }
            else{
                System.out.println("Server already started.");
                if(restartWhenNewAppIsBuildFirstTime && !alreadyStarted.contains(project.getName())){
                    addRestartServerJob(server,cj,defaultDelay);
                }
                else{
                    if(cj!=null){cj.run();}
                }
            }
            if(!alreadyStarted.contains(project.getName())){
                alreadyStarted.add(project.getName());
            }
    }
    
    public static void addStartServerJob(final IServer server, final ChainedJob cj, int delay){
        Job job = new Job("start server") {
            public IStatus run(IProgressMonitor monitor){
                try {
                  //server might have been started in the mean time
                  if(server.canStart(org.eclipse.debug.core.ILaunchManager.RUN_MODE).equals(Status.OK_STATUS)){
                    System.out.println("Starting server.");
                    server.start(org.eclipse.debug.core.ILaunchManager.RUN_MODE,monitor);
                  }
                  //after starting execute ChainedJob
                  if(cj!=null){cj.run();}
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }
   
    public static void addRestartServerJob(final IServer server, final ChainedJob cj, final int delay){
        Job job = new Job("Restarting server.") {
            public IStatus run(IProgressMonitor monitor){
                System.out.println("Stopping server.");
                /* stop and start or restart */
                /*server.stop(true);
                addStartServerJob(server, cj, delay);*/
                server.restart(org.eclipse.debug.core.ILaunchManager.RUN_MODE,monitor);
                if(cj!=null){cj.run();}
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }
    
    public static void addPublishJob(final IServer server,final IProject project, int delay){
        Job job = new Job("publish server") {
            public IStatus run(IProgressMonitor monitor){
                publish(server, monitor);
                addCheckServerStartedJob(server,project,null,defaultDelay);
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }
    
    public static void publish(IServer server, IProgressMonitor monitor){
       System.out.println("Publishing server.");
       server.publish(IServer.PUBLISH_STATE_INCREMENTAL,monitor);
    }
    
    public static void addRefreshJob(final IProject project, int delay){
        Job job = new Job("refresh project") {
            public IStatus run(IProgressMonitor monitor){
                refresh(project, monitor);
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }
    
    public static void refresh(IProject project, IProgressMonitor monitor){
        try {
            project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}

package webdsl;

import static webdsl.EclipseConsoleUtils.log;
import static webdsl.FileUtils.writeStringToFile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
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
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;

@SuppressWarnings("restriction")
public class WebDSLProjectBuilder extends IncrementalProjectBuilder{

    //@SuppressWarnings("unchecked")
    @SuppressWarnings("rawtypes")
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

              initWtpServerConfig(WebDSLEditorWizard.getPluginDir(),project,project.getName(),monitor);

              ReadApplicationIni.init(project);

              tryStartServer(project, monitor,
                  new ChainedJob(){
                      public void run(){
                          // trying refresh here instead of in Ant build file, http://yellowgrass.org/issue/WebDSL/762
                          refresh(project, monitor);
                          if(ReadApplicationIni.isPluginBuildPollServerEnabled()){
                        	  log("automatic poll of server is enabled");
                              pollDeployedAppAndOpenBrowser(project, buildid, 0);
                          }
                          else{
                        	  log("automatic poll of server is disabled");
                          }
                      }
                  }, 0);
              //pollDeployedAppAndOpenBrowser(project, buildid, 1000);
            }
            worked( monitor, 1 );
            //log("build done");
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
            writeStringToFile("started", project.getLocation()+"/.servletapp/.webdsl-project-builder-started");
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
            log("Could not find a build id in .servletapp/"+fileName);
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

    protected void initWtpServerConfig(String plugindir, IProject project, String projectname, IProgressMonitor monitor) throws CoreException{

    }

    /*
     * tried several ways of calling ant, but none of them is working as needed, ant is now called from .project builder directly instead
     */
    /*
    public void runAntBuild(IProject project, IProgressMonitor monitor, String buildid) throws CoreException{

        File buildFile = new File(project.getLocation().toString()+"/build.xml");

        AntLaunchShortcut launcher = new AntLaunchShortcut();
        IPath p = Path.fromPortableString(buildFile.toString());
        log(p);
        launcher.setShowDialog(false);
        launcher.launch(new AntProjectNodeProxy(buildFile.toString()),org.eclipse.debug.core.ILaunchManager.RUN_MODE);
      */
        /*
        AntLaunchShortcut launcher = new AntLaunchShortcut();
        IPath p = Path.fromPortableString(buildFile.toString());
        log(p);
        launcher.launch(p, project, org.eclipse.debug.core.ILaunchManager.RUN_MODE, "plugin-eclipse-build");
        */

        /*
        AntRunner builder = new AntRunner();
        builder.setBuildFileLocation(buildFile.toString());
        //builder.setMessageOutputLevel(org.apache.tools.ant.Project.MSG_INFO);
        log(new webdsl.AntConsoleLogger().getClass().getName());
        builder.addBuildLogger(new webdsl.AntConsoleLogger().getClass().getName());
        builder.run(monitor);
        */

        //non-eclipse ant runner, log will not show up in user console, eclipse tasks will not be available
        /*
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());

        String plugindir = new WebDSLJarAntPropertyProvider().getAntPropertyValue("plugindir");
        String strjdir = StrategoAppl.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        log(strjdir);
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


    public static IServer getProjectServer(IProject project, IProgressMonitor monitor){
        IServer[] servers = ServerUtil.getServersByModule(ServerUtil.getModule(project), monitor);
        for(IServer s :  servers){
          return s;
        }
        return null;
    }


    //quick hack, missing a removeAllPublishListeners method in Server
    /*public static IPublishListener previouslyAddedPublishListener = null;
    public static void tryRemovePreviousListener(IServer server){
        if(previouslyAddedPublishListener != null){
            server.removePublishListener(previouslyAddedPublishListener);
            previouslyAddedPublishListener = null;
          }
    }*/


    public void pollDeployedAppAndOpenBrowser(final IProject project, final String buildid, int delay){
      if(buildid != null){
        Job job = new Job("poll deployed app and open browser") {
           public IStatus run(IProgressMonitor monitor){
        	   log("waiting for server to start");
        	   log("number of milliseconds to wait before polling: "+ReadApplicationIni.getPluginBuildPollWaitTime());
        	   try {
        	   //monitor.wait(ReadApplicationIni.getPluginBuildPollWaitTime());// delay in job.schedule(delay) seems to be ignored, trying this way
				Thread.sleep(ReadApplicationIni.getPluginBuildPollWaitTime());

			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			log("polling");
               boolean deployed = pollDeployedAppForNewBuildId(project,buildid);
               if(deployed){
            	   log("opening tab");
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
               }
               else{
            	   log("poll failed");
               }
               return Status.OK_STATUS;
           }
         };
         job.schedule(delay);
      }
   }


    public static int defaultDelay = 1000;
/*
    public void setPublishListener(final IProject project,IProgressMonitor monitor, final String buildid) throws CoreException{
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
                log("status: " + status);
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
          log("Adding publish listener.");
          tomcatserver.addPublishListener(publishListener);
        }

        previouslyAddedPublishListener = publishListener;
    }
*/
    public String getAppUrl(IProject project){
        return getUrlRoot(project);
    }

    public String getUrlRoot(IProject project){
    	String port = getProperty(project, "httpport");
        if(isRootApp(project)){
            return "http://localhost:" + port + "/";
        }
        else{
            return "http://localhost:" + port +"/"+project.getName()+"/";
        }
    }

    public boolean pollDeployedAppForNewBuildId(IProject project, String buildid){
        URL url = null;
        try {
            url = new URL(getAppUrl(project)+"?show_build_id=true");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String searchfor = "build-id:"+buildid;
        log("searching for: "+searchfor);
        boolean found = false;
        int tries = ReadApplicationIni.getPluginBuildPollNumberOfTries();
        log("total tries: "+tries);
        while(tries > 0 && !found){
        	log("tries left: "+tries);
          tries = tries - 1;
          try {
            URLConnection con = url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            InputStreamReader instream = new InputStreamReader(con.getInputStream());
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
            log("error while requesting page "+url+" : "+e.getMessage());
            //e.printStackTrace();
          }
          try {
            Thread.sleep (2500);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if(found){
          log("application deployed, opening browser tab");
        }
        else{
          log("application not deployed yet, cancelled opening browser tab");
        }
        return found;
    }

    public static boolean forcePublish = false;

    //restart once for a new project
    public static List<String> alreadyStarted = new ArrayList<String>();

    public static void openServersProject(IProgressMonitor monitor) throws CoreException{
        //open 'Servers' project if closed, otherwise tomcat will not start
        IProject servers = ResourcesPlugin.getWorkspace().getRoot().getProject("Servers");
        if(!servers.isOpen()){
            servers.open(monitor);
        }
    }

    public void tryStartServer(IProject project, IProgressMonitor monitor, ChainedJob cj, int delay) throws CoreException{
        openServersProject(monitor);

        final IServer server = getProjectServer(project,monitor);

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

    //disabled for now http://yellowgrass.org/issue/WebDSL/228
    public static boolean restartWhenNewAppIsBuildFirstTime = false;

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
           log("polling Tomcat server status");
            if(server.canStart(org.eclipse.debug.core.ILaunchManager.RUN_MODE).equals(Status.OK_STATUS)){
                addStartServerJob(server,cj,defaultDelay);
            }
            else{
                log("server already started");
                if(ReadApplicationIni.isPluginBuildRestartServer()){
                	log("configured to restart Tomcat on each deploy");
                	addRestartServerJob(server,cj,defaultDelay);
                }
                if(restartWhenNewAppIsBuildFirstTime && !alreadyStarted.contains(project.getName())){
                	log("restarting Tomcat for first deploy");
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
            @SuppressWarnings("deprecation")
            public IStatus run(IProgressMonitor monitor){
                try {
                  //server might have been started in the mean time
                  if(server.canStart(org.eclipse.debug.core.ILaunchManager.RUN_MODE).equals(Status.OK_STATUS)){
                    log("starting server");
                    server.synchronousStart(org.eclipse.debug.core.ILaunchManager.RUN_MODE,monitor);
                  }
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                //after starting execute ChainedJob
                if(cj!=null){cj.run();}
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }

    public static void addStopServerJob(final IServer server, final ChainedJob cj, int delay){
        Job job = new Job("stop server") {
            @SuppressWarnings("deprecation")
            public IStatus run(IProgressMonitor monitor){
                log("Stopping server.");
                server.synchronousStop(true);
                if(cj!=null){cj.run();}
                return Status.OK_STATUS;
            }
        };
        job.schedule(delay);
    }

    public static void addRestartServerJob(final IServer server, final ChainedJob cj, final int delay){
        Job job = new Job("Restarting server.") {
            @SuppressWarnings("deprecation")
            public IStatus run(IProgressMonitor monitor){
                log("stopping server");
                /* stop and start or restart */
                /*server.stop(true);
                addStartServerJob(server, cj, delay);*/
                try {
                    server.synchronousRestart(org.eclipse.debug.core.ILaunchManager.RUN_MODE,monitor);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
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
       log("publishing server");
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

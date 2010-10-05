package webdsl;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wst.server.core.IPublishListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.eclipse.wst.server.ui.internal.ServerUIPlugin;

import webdsl.WebDSLProjectBuilder;

public class PollDeployedAppTask extends Task {
    private String projectname;

    public void execute() throws BuildException {
        System.out.println("start server and publish project: "+projectname);
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject project = workspace.getRoot().getProject(projectname);
        String buildid = WebDSLProjectBuilder.getBuildIdCompleted(project);
        WebDSLProjectBuilder.pollDeployedAppAndOpenBrowser(project,buildid, 0);
    }
    
    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }
}

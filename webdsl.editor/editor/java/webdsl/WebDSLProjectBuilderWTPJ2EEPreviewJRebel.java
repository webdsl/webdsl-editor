package webdsl;

import static webdsl.FileUtils.fileExists;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IServer;

public class WebDSLProjectBuilderWTPJ2EEPreviewJRebel extends WebDSLProjectBuilderWTPJ2EEPreview{

    @Override
    protected void initWtpServerConfig(String plugindir, IProject project, String projectname, IProgressMonitor monitor) throws CoreException{
        WTPJ2EEPreviewSetupJRebel.getInstance().initWtpServerConfig(plugindir,project,projectname,monitor);
    }

    @Override
    public void tryStartServer(IProject project, IProgressMonitor monitor, final ChainedJob cj, int delay) throws CoreException{
        //publish, clean, restart are all not supported on this server type, so just stop and start
        //with JRebel only stop and start if there were entity changes 
        if(fileExists(project.getLocation().toString()+"/.servletapp/.entities-have-changed")){
            super.tryStartServer(project, monitor, cj, delay);       
        }
        else{ 
            //start the server if it was not started already, or let JRebel reload classes if it was started 
            final IServer server = getProjectServer(project,monitor);
            addStartServerJob(server, cj, delay);      
        }
    }
    
}

package webdsl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public final class WebDSLProjectBuilderWTPTomcat extends WebDSLProjectBuilder{

    @Override
    protected void initWtpServerConfig(String plugindir, IProject project, String projectname, IProgressMonitor monitor) throws CoreException{
        WTPTomcatSetup.initWtpServerConfig(plugindir,project,projectname,monitor);
    }

}

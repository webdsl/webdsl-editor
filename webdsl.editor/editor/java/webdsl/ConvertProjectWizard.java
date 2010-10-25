package webdsl;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ConvertProjectWizard extends WebDSLEditorWizard {

    public ConvertProjectWizard() {
        setNeedsProgressMonitor(true);
        input = new ConvertProjectWizardPage();
    }

    /**
     * Instead of creating a project, use the selected existing one.
     */
    @Override
    protected IProject createNewProject(String projectName, IProgressMonitor monitor) throws CoreException{
        return ((ConvertProjectWizardPage) input).getSelectedProject();
    }
    /**
     *  Don't generate example app files
     */
    @Override
    public void writeExampleApplicationFiles(IProject project, String appName, String plugindir) throws IOException{
        StringBuffer file = new StringBuffer();
        file.append("file to let the ant builder know that the project needs to be cleaned");
        WebDSLEditorWizard.createDirs(project.getLocation()+"/.servletapp/");
        writeStringToFile(file.toString(), project.getLocation()+"/.servletapp/.clean-project-required");
        writeStringToFile("needed to start initial build", project.getLocation()+"/.saved-but-not-built");
        createDirs(project.getLocation()+"/nativejava");
    }
    @Override
    protected void openEditorsForExampleApp(String appName, IProject project, IProgressMonitor monitor){}
}

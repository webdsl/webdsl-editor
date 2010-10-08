package webdsl;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.strategoxt.imp.runtime.EditorState;

public class NewEmptyProjectWizard extends WebDSLEditorWizard {

    public NewEmptyProjectWizard() {
        setNeedsProgressMonitor(true);
        input = new WebDSLEditorWizardPage();
    }

    @Override
    public void writeExampleApplicationFiles(IProject project, String appName, String plugindir) throws IOException{
        writeStringToFile("application "+appName+"\n\n\tdefine page root(){ \"Hello world\" }", project.getLocation()+"/"+appName+".app");
        
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
    
    @Override
    protected void openEditorsForExampleApp(String appName, IProject project, IProgressMonitor monitor){
        monitor.setTaskName("Opening editor tabs");
        Display display = getShell().getDisplay();
        EditorState.asyncOpenEditor(display, project.getFile(appName+".app"), true);
        monitor.worked(1);    	 
    }
}

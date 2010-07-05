package webdsl;

import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.views.navigator.IResourceNavigator;

//@TODO: import settings from existing application.ini file

/*
 * Similar to the new WebDSL project wizard, difference is that an existing project
 * is selected here, which determines projectname and appname.
 */
public class ConvertProjectWizardPage extends WebDSLEditorWizardPage {
   
    protected CheckboxTableViewer tableViewer;
    
    protected Object[] listItems = null;
    
    public IProject getSelectedProject(){
      return selectedProject;
    }
    public IProject selectedProject = null;
    
    public ConvertProjectWizardPage() {
        setTitle("Convert to a WebDSL project");
        setDescription("This wizard generates the WebDSL project structure in an existing project.");
    }

    @Override
    public void projectAndApplicationNameEntry(Composite container){
      createAvailableProjectsGroup(container);
    }
    
    protected boolean projectNameAvailable() {
        return true;
    }

    protected void onChange() {
        if (!ignoreEvents) {
            setErrorMessage(null);
            if (tableViewer.getCheckedElements().length != 1) {
                setErrorStatus("A project must be selected");
                return;
            }
            String filename = selectedProject.getName()+".app";
            if(selectedProject.findMember(filename) == null){
                setErrorStatus("Project "+selectedProject.getName()+" must contain a "+filename+" file.");
                return;
            }
        }
        super.onChange();
    }
    private final Composite createAvailableProjectsGroup(Composite parent) {

        Label label = new Label(parent, SWT.TOP);
        label.setText("Select a project");
        
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        layout.numColumns = 2;
        container.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(data);
        
        // create the table
        Table    table = new Table(container, 
                                   SWT.CHECK | SWT.BORDER | SWT.MULTI | 
                                   SWT.SINGLE | SWT.H_SCROLL | 
                                   SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(data);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);

        TableLayout tableLayout = new TableLayout();
        table.setHeaderVisible(false);
        table.setLayout(tableLayout);

        tableViewer = new CheckboxTableViewer(table);
        tableViewer.setLabelProvider(new ProjectLabelProvider());
        tableViewer.setContentProvider(new ProjectContentProvider());

        tableViewer.setInput(getElements());

        tableViewer.setSorter(new ViewerSorter() {
            @Override
            public int compare(Viewer viewer, Object object1, Object object2) {

                if ((object1 instanceof IProject) && (object2 instanceof IProject)) {
                    IProject left = (IProject)object1;
                    IProject right = (IProject)object2;
                    int result = left.getName().compareToIgnoreCase(right.getName());

                    if (result != 0) {
                        return result;
                    }
                    return left.getName().compareToIgnoreCase(right.getName());
                }
                return super.compare(viewer, object1, object2);
            }

            @Override
            public boolean isSorterProperty(Object element, String property) {
                return true;
            }
        });
        tableViewer.refresh();
        
        //try to select a project based on what is selected in navigator
        IProject p = getSelectedProjectInNavigator(); 
        if(p != null){
            //tableViewer seems to be missing a getAllElements(), using getCheckedElements instead, 
            // and uncheck when not the same project
            tableViewer.setAllChecked(true);
            for(Object o : tableViewer.getCheckedElements()){
                if(!o.equals(p)){
                  tableViewer.setChecked(o, false);
                }
                else{
                  updateProjectSelection(p);
                }
            } 
        }
        
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                IProject selected = (IProject) ((StructuredSelection)e.getSelection()).getFirstElement();
              
                //only allow single selection
                for(Object o : tableViewer.getCheckedElements()){
                    if(!o.equals(selected)){
                       tableViewer.setChecked(o, false);
                    }
                }
                
                updateProjectSelection(selected);
            }
        });
        return parent;
    }
    
    public void updateProjectSelection(IProject project){
        //set values for project selection
        inputProjectNameValue = project.getName();
        inputAppNameValue = project.getName();
        
        selectedProject = project;
        
        onChange();
    }
    
    public IProject getSelectedProjectInNavigator(){
        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (!(selection instanceof IStructuredSelection))
        {
            return null;
        }
        IStructuredSelection sel = (IStructuredSelection) selection;
        Object res = sel.getFirstElement();
        if (res instanceof IProject)
        { 
           return (IProject) res;
        }
        if (res instanceof IJavaProject)
        {  
          return ((IJavaProject) res).getProject();
        }
        return null;
    }
       
    public class ProjectContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object parent) {
             return listItems;                
        }
        public void dispose() {}
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    }

    public class ProjectLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object obj, int index) {
            if (index == 0) {
                return ((IProject)obj).getName();
            }
            return "";
        }
        public Image getColumnImage(Object obj, int index) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
        }
    }

    protected Object[] getElements() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        Vector<IProject> candidates = new Vector<IProject>(projects.length);
        IProject next = null;

        for (IProject project : projects) {
            next = project;
            if ((next != null) && next.isOpen() && isCandidate(next)) {
                candidates.addElement(next);
            }
            next = null;
        }
        Object[] candidateArray = null;
        if (candidates.size() > 0) {
            candidateArray = new Object[candidates.size()];
            candidates.copyInto(candidateArray);
        }
        listItems = candidateArray;
        return candidateArray;
    }

    public boolean isCandidate(IProject project){return true;}
    
}

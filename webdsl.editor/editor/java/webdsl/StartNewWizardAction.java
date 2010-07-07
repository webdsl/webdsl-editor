package webdsl;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

public class StartNewWizardAction implements IViewActionDelegate {
    ISelection selection;

    public void init(IViewPart view) { }
    
    public void run(IAction action) {
        WebDSLEditorWizard wizard = new WebDSLEditorWizard();
        /*if ((selection instanceof IStructuredSelection) || (selection == null)){
            wizard.init(part.getSite().getWorkbenchWindow().getWorkbench(), (IStructuredSelection)selection);
        }*/
        WizardDialog dialog = new WizardDialog(null, wizard);
        dialog.create();
        dialog.open();
        
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }
}

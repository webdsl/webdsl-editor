package webdsl;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class StartNewEmptyProjectWizardAction implements IViewActionDelegate {
    ISelection selection;

    public void init(IViewPart view) { }
    
    public void run(IAction action) {
        WebDSLEditorWizard wizard = new NewEmptyProjectWizard();
        WizardDialog dialog = new WizardDialog(null, wizard);
        dialog.create();
        dialog.open();
        
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }
}

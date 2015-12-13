package ca.concordia.cssanalyser.plugin.commandhandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ca.concordia.cssanalyser.plugin.views.DuplicationRefactoringView;



public class PopupMenuHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			setCSSFileFromSelection();
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DuplicationRefactoringView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setCSSFileFromSelection() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection)selection;
				Object element = structuredSelection.getFirstElement();
				if(element instanceof IFile) {
					IFile file = (IFile)element;
					IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
					try {
						IDE.openEditor(page, file);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

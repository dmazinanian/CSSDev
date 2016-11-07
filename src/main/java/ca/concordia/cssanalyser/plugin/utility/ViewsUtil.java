package ca.concordia.cssanalyser.plugin.utility;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ca.concordia.cssanalyser.plugin.activator.Activator;
import ca.concordia.cssanalyser.plugin.utility.LocalizedStrings.Keys;

public class ViewsUtil {
	
	public static IViewPart openView(String viewID) {
		try {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IViewPart getView(String viewID) {
		IViewReference viewReferences[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getViewReferences();
		for (int i = 0; i < viewReferences.length; i++) {
			if (viewID.equals(viewReferences[i].getId())) {
				return viewReferences[i].getView(false);
			}
		}
		return null;
	}
	
	public static IEditorPart openEditor(String filePath) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		try {
			IPath path = new Path(filePath);
			return IDE.openEditor(page, ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path));
		} catch (PartInitException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public static IEditorPart openExternalFile(String filePath) {
		File fileToOpen;
		fileToOpen = new File(filePath);
		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				return IDE.openEditorOnFileStore(page, fileStore );
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void showDetailedError(Throwable throwable) {
		MultiStatus info = getStatusFromThrowable(throwable);
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			ErrorDialog.openError(activeWorkbenchWindow.getShell(), 
				LocalizedStrings.get(Keys.PARSE_ERROR_IN_FILE_TITLE), 
				LocalizedStrings.get(Keys.PARSE_ERROR_IN_FILE_MESSAGE),
				info);
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					ErrorDialog.openError(null, 
							LocalizedStrings.get(Keys.PARSE_ERROR_IN_FILE_TITLE), 
							LocalizedStrings.get(Keys.PARSE_ERROR_IN_FILE_MESSAGE),
							info);
				}
			});
		}
		
	}
	
	private static MultiStatus getStatusFromThrowable(Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		String message = sw.getBuffer().toString();
		if (message == null) {
			message = throwable.toString();
		}
		String pluginId = Activator.PLUGIN_ID;
		MultiStatus info = new MultiStatus(pluginId, 1, throwable.toString(), throwable);
		for (String s : message.split("\n")) {
			info.add(new Status(IStatus.ERROR, pluginId, 1, s.replace("\t", "    "), throwable));	
		}
		return info;
	}

	public static IEditorReference getEditorReferenceForIFileIfOpened(IFile file) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (IEditorReference editorReference : editorReferences) {
			try {
				IEditorInput editorInput = editorReference.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					IFileEditorInput iFileEditorInput = (IFileEditorInput) editorInput;
					if (iFileEditorInput.getFile().equals(file)) {
						return editorReference;
					}
				}
			} catch (PartInitException e) {
				showDetailedError(e);
			}
		}
		return null;
	}
}

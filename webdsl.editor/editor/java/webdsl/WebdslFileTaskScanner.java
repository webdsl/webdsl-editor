
 
 //Contributors:
 //  Chris Melman

package webdsl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.core.internal.provisional.tasks.IFileTaskScanner;
import org.eclipse.wst.sse.core.internal.provisional.tasks.TaskTag;



/**
 * A delegate to create TASKs for "todos" and similar comments.
 */
public class WebdslFileTaskScanner implements IFileTaskScanner {

	private ArrayList<Map> MarkerMapList = null;
	
	
	
	public WebdslFileTaskScanner() {
		super();
		MarkerMapList = new ArrayList<Map>();
	}
	
	private void setMarkerOnFile(IFile file, int lineNumber, String text, int priority, int start, int end){

		try {
			IMarker newmarker = file.createMarker(getMarkerType());
			newmarker.setAttribute(IMarker.LINE_NUMBER,lineNumber);
			newmarker.setAttribute(IMarker.MESSAGE, text);
			newmarker.setAttribute(IMarker.PRIORITY, priority);
			newmarker.setAttribute(IMarker.CHAR_START,start);
			newmarker.setAttribute(IMarker.CHAR_END, end);
			newmarker.setAttribute(IMarker.USER_EDITABLE, false);

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getMarkerType() {
		
		return "org.eclipse.core.resources.taskmarker";
	}

	@Override
	public Map[] scan(IFile file, TaskTag[] tags, IProgressMonitor arg2) {
		try {
			file.deleteMarkers(null, true, 1);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if(!arg2.isCanceled() ){
			Scanner scanner = null;
			try {
				scanner = new Scanner(file.getContents());
			} catch (CoreException e) {
				e.printStackTrace();
			}	
			int lineNumber = 0;
			int offset = 0;
				while(scanner.hasNextLine()){
					String line= "";
					try{
						lineNumber ++;
						line = scanner.nextLine();
						String[] split = line.split("//\\s*");
						if(split.length > 1){
							String comment = split[1];
							split = comment.split("\\s*:");
							if(split.length > 1){
								String tag = split[0];
								String description = split[1];
								if(stringIsTagName(tag, tags)){
									int start = offset + line.indexOf(tag);
									int end = offset + line.replaceAll("\\s+$", "").length();
									setMarkerOnFile(file, lineNumber, tag + " : "+ description.trim(), findPriorityOfTag(tag, tags), start, end);
								}							
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					} finally {
						offset += line.length() + 1;// + 1 is end of line char
					}
				}
				
		}
		
		return (Map[]) MarkerMapList.toArray(new Map[MarkerMapList.size()]);
	} 



	private int findPriorityOfTag(String tag, TaskTag[] tags) {
		for (TaskTag taskTag : tags) {
			if(taskTag.getTag().equalsIgnoreCase(tag)){
				return taskTag.getPriority();
			}
		}
		return -1;
	}

	private boolean stringIsTagName(String term, TaskTag[] tags) {
		for(TaskTag tag : tags ){
			if(tag.getTag().equalsIgnoreCase(term)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void shutdown(IProject arg0) {
		
	}

	@Override
	public void startup(IProject arg0) {

	}
	
}


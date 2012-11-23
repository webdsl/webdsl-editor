/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package webdsl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.core.internal.provisional.tasks.IFileTaskScanner;
import org.eclipse.wst.sse.core.internal.provisional.tasks.TaskTag;
import org.strategoxt.tools.var2prodrule_0_0;



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
						if (line.contains("//")){
							String[] split = line.split("//");
							if(split.length > 1){
								String comment = line.split("//")[1];
								if(comment.contains(":")) {
									split = comment.split(":");
									if(split.length > 1){
										String tag = split[0].trim();
										String description = split[1];
										if(stringIsTagName(tag, tags)){
											int start = offset + line.indexOf(tag);
											int end = offset + line.replaceAll("\\s+$", "").length();
											setMarkerOnFile(file, lineNumber, tag + " : "+ description.trim(), findPriorityOfTag(tag, tags), start, end);
										}
									}
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

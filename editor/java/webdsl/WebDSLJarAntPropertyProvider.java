package webdsl;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.Platform;

public class WebDSLJarAntPropertyProvider implements IAntPropertyValueProvider {
	
	public String getAntPropertyValue(String antPropertyName) {
     	String plugindir = webdsl.WebDSLEditorWizard.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (Platform.getOS().equals(Platform.OS_WIN32)) { // FIXME: proper paths on Windows
			plugindir = plugindir.substring(1);
		}
		return plugindir;
	}
}

package webdsl;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class WebDSLVersionPropertyProvider implements IAntPropertyValueProvider {
	
	/**
	 * Return plugin version in major.minor.micro[.qualifier] format.
	 */
	public String getAntPropertyValue(String antPropertyName) {
     	
		// Plugin might not be started, so we can't use Activator.getInstance()
		Bundle bundle = Platform.getBundle(Activator.kPluginID);
		return bundle.getVersion().toString();
		
	}
	
}

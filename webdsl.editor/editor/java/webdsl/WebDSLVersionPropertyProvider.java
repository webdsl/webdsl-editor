package webdsl;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.osgi.framework.Bundle;

public class WebDSLVersionPropertyProvider implements IAntPropertyValueProvider {
	
	/**
	 * Return plug-in version in major.minor.micro[.qualifier] format.
	 */
	public String getAntPropertyValue(String antPropertyName) {
     	
		Bundle bundle = Activator.getInstance().getBundle();
		return bundle.getVersion().toString();
		
	}
	
}

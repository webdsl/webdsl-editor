package webdsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IProject;

public class ReadApplicationIni {

	protected final static String applicationini = "application.ini";
	protected static java.util.Properties props = new java.util.Properties();

	protected static boolean pluginBuildPollServerEnabled = true;
	public static boolean isPluginBuildPollServerEnabled(){
		return pluginBuildPollServerEnabled;
	}

	protected final static int pluginBuildPollWaitTimeMin = 1000;
	protected final static int pluginBuildPollWaitTimeMax = 25000;
	protected final static int pluginBuildPollWaitTimeDefault = 10000;
	protected static int pluginBuildPollWaitTime = pluginBuildPollWaitTimeDefault;
	public static int getPluginBuildPollWaitTime(){
		return pluginBuildPollWaitTime;
	}

	protected final static int pluginBuildPollNumberOfTriesMin = 1;
	protected final static int pluginBuildPollNumberOfTriesMax = 10;
	protected final static int pluginBuildPollNumberOfTriesDefault = 3;
	protected static int pluginBuildPollNumberOfTries = pluginBuildPollNumberOfTriesDefault;
	public static int getPluginBuildPollNumberOfTries(){
		return pluginBuildPollNumberOfTries;
	}

	protected static boolean pluginBuildRestartServer = false;
	public static boolean isPluginBuildRestartServer(){
		return pluginBuildRestartServer;
	}

	static void init(IProject project) {
		try {
			props.load(new FileInputStream(new File(project.getLocation().toString()+"/"+applicationini)));

			if("false".equals(props.getProperty("pluginbuildpollserver"))){
				pluginBuildPollServerEnabled = false;
			}

			try{
				pluginBuildPollWaitTime = Integer.parseInt(props.getProperty("pluginbuildpollwaittime"));
				if(pluginBuildPollWaitTime < pluginBuildPollWaitTimeMin){
					pluginBuildPollWaitTime = pluginBuildPollWaitTimeMin;
				}
				if(pluginBuildPollWaitTime > pluginBuildPollWaitTimeMax){
					pluginBuildPollWaitTime = pluginBuildPollWaitTimeMax;
				}
			}
			catch(NumberFormatException nfe){
				//leave at default
			}

			try{
				pluginBuildPollNumberOfTries = Integer.parseInt(props.getProperty("pluginbuildpollnumberoftries"));
				if(pluginBuildPollNumberOfTries < pluginBuildPollNumberOfTriesMin){
					pluginBuildPollNumberOfTries = pluginBuildPollNumberOfTriesMin;
				}
				if(pluginBuildPollNumberOfTries > pluginBuildPollNumberOfTriesMax){
					pluginBuildPollNumberOfTries = pluginBuildPollNumberOfTriesMax;
				}
			}
			catch(NumberFormatException nfe){
				//leave at default
			}

			if("true".equals(props.getProperty("pluginbuildrestartserver"))){
				pluginBuildPollServerEnabled = true;
			}
		}
		catch(java.io.FileNotFoundException fnf) {
			System.out.println("File \""+applicationini+"\" not found");
		}
		catch(IOException io) {
			System.out.println("IOException while reading \""+applicationini+"\"");
		}
	}
}

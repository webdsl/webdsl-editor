package webdsl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

// http://wiki.eclipse.org/FAQ_How_do_I_write_to_the_console_from_a_plug-in%3F
public class EclipseConsoleUtils {

	public static String consoleName = "WebDSL Console";
	public static SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");

	public static void log(String s){
		MessageConsole myConsole = findConsole(consoleName);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(timeformat.format(new Date())+" "+s);
	}

	public static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		//no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		return myConsole;
	}

}

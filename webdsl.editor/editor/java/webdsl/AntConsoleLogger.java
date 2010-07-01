package webdsl;

import org.apache.tools.ant.DefaultLogger;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class AntConsoleLogger extends DefaultLogger {

    // the message console stream used to output to the console view
    private MessageConsoleStream mConsoleStream = null;

    public AntConsoleLogger() {
        super();
        MessageConsole myConsole = findConsole("Some console name");
        mConsoleStream = myConsole.newMessageStream();
    }

    protected void log(String message) {
        mConsoleStream.println(message);
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


package webdsl;

import static org.eclipse.core.resources.IResource.DEPTH_INFINITE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class FileUtils {
    
    public static boolean fileExists(String file){
        return new File(file).exists();
    }
    
    public static void writeStringToFile(String s, String file) throws IOException{
        FileOutputStream in = null;
        try{
            File buildxml = new File(file);
            in = new FileOutputStream(buildxml);
           FileChannel fchan = in.getChannel();
           BufferedWriter bf = new BufferedWriter(Channels.newWriter(fchan,"UTF-8"));
           bf.write(s);
           bf.close();
        }
        finally{
            if(in != null){
                in.close();
            }
        }
    }
    
    public static void createDirs(String dirs){
        new File(dirs).mkdirs();
    }
    
    public static void copyFile(String ssource, String sdest) throws IOException {
        System.out.println("Copying "+ssource+" to "+sdest);
        File dest = new File(sdest);
        File source = new File(ssource);
        if(!dest.exists()) {
            dest.createNewFile();
        }
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();
            out.transferFrom(in, 0, in.size());
        }
        finally {
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
        }
    }

    public static void refreshProject(final IProject project) {
        try {
            NullProgressMonitor monitor = new NullProgressMonitor();
            project.refreshLocal(DEPTH_INFINITE, monitor);
            project.close(monitor);
            project.open(monitor);        
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    public static String getWebDSLVersion(){
        return Activator.getInstance().getBundle().getVersion().toString();
    }

}

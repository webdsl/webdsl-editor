package webdsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;

@SuppressWarnings("restriction")
public class WTPUtils  {
   
    public static IRuntimeType getRuntimeType(String runtimeTypeName){
        IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(null, null, null);
        IRuntimeType runtimetype = null;
        if (runtimeTypes != null) {
            int size = runtimeTypes.length;
            for (int i = 0; i < size; i++) {
                IRuntimeType runtimeType = runtimeTypes[i];
                //System.out.println("  "+ runtimeType +"    "+ runtimeType.getName());
                if(runtimeType.getName().equals(runtimeTypeName)){
                    runtimetype = runtimeType;
                }
            }
        }
        System.out.println("runtime type: "+runtimetype);
        return runtimetype;
    }	
    
    public static IRuntime getRuntime(String runtimeId){
        IRuntime[] runtimes = ServerUtil.getRuntimes(null, null);
        IRuntime runtime = null;
        for(IRuntime ir : runtimes){
            System.out.println(ir.getId());
            if(ir.getId().equals(runtimeId)){
                runtime = ir;
            }
        }
        System.out.println("runtime: "+runtime);
        return runtime;
    }
    
    public static IServer getServer(IProject project, String serverId, IProgressMonitor monitor){
        IServer server = null;
        IModule module = ServerUtil.getModule(project);
        System.out.println("Module: "+module);
        if(module==null){
            return null;
        }
        for(IServer serv : ServerUtil.getServersByModule(module, monitor)){ 
            if(serv.getId().equals(serverId)){
                server = serv;
            }
        } 
        if(server==null){
            for(IServer serv : ServerUtil.getAvailableServersForModule(ServerUtil.getModule(project), true, monitor)){ 
                if(serv.getId().equals(serverId)){
                    server = serv;
                }
            } 
        }
        System.out.println("server: "+server);
        return server;
    }
    
    public static void addProjectModuleToServer(IProject project, IServer server, IProgressMonitor monitor) throws CoreException{
        IModule[] currentModules = server.getModules();
        IModule projectModule = ServerUtil.getModule(project);
        if(!Arrays.asList(currentModules).contains(projectModule)){
            IServerWorkingCopy serveraddmodule = new ServerWorkingCopy((Server) server);
            // attach the project module to the server config
            IModule[] modules = {projectModule};
            serveraddmodule.modifyModules(modules, null, null);
            serveraddmodule.saveAll(false,monitor);
        }
    }
    
    public static void removeProjectModuleFromServer(IProject project, IServer server, IProgressMonitor monitor) throws CoreException{
        if(server == null){
            System.out.println("module is currently not in server, cannot remove it");
            return;
        }
        IModule[] currentModules = server.getModules();
        IModule projectModule = ServerUtil.getModule(project);
        if(Arrays.asList(currentModules).contains(projectModule)){
            IServerWorkingCopy serveraddmodule = new ServerWorkingCopy((Server) server);
            // remove the project module from the server config
            IModule[] modules = {projectModule};
            serveraddmodule.modifyModules(null, modules, null);
            serveraddmodule.saveAll(false,monitor);
        }
     }
    
    //copy from org.eclipse.wst.server.ui.internal.wizard.page.NewRuntimeComposite (protected access)
    public static IServerType getCompatibleServerType(IRuntimeType runtimeType) {
        List<IServerType> list = new ArrayList<IServerType>();
        IServerType[] serverTypes = ServerCore.getServerTypes();
        int size = serverTypes.length;
        for (int i = 0; i < size; i++) {
            IRuntimeType rt = serverTypes[i].getRuntimeType();
            if (rt != null && rt.equals(runtimeType))
                list.add(serverTypes[i]);
        }
        if (list.size() == 1)
            return list.get(0);
        return null;
    }

}
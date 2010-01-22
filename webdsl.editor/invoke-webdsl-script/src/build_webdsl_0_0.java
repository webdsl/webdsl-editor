package invokescript;

import org.strategoxt.stratego_lib.*;
import org.strategoxt.lang.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
//****
import java.io.File;
import java.io.IOException;
import java.io.*;
import org.apache.tools.ant.*;
//****
import java.util.ArrayList;

@SuppressWarnings("all") public class build_webdsl_0_0 extends Strategy 
{ 
  public static build_webdsl_0_0 instance = new build_webdsl_0_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    context.push("build_webdsl_0_0");
    Fail1:
    { 
      /*
      File buildFile = new File("/usr/local/share/webdsl/webdsl-build.xml");
      Project p = new Project();
      p.setUserProperty("ant.file", buildFile.getAbsolutePath());
      p.init();
      ProjectHelper helper = ProjectHelper.getProjectHelper();
      p.addReference("ant.projectHelper", helper);
      helper.parse(p, buildFile);
      p.executeTarget("build");
      */

      try 
      { 
        String antinvoke="ant -f /usr/local/share/webdsl/webdsl-build.xml -Dtemplatedir=\"/usr/local/share/webdsl\" -Dcurrentdir=\""+new java.io.File(".").getCanonicalPath()+"\" -Dwebdslexec=\"java -ss4m -cp /usr/local/bin/strategoxt.jar:/usr/local/bin/webdsl.jar org.webdsl.webdslc.Main\" build";
        System.out.println(antinvoke); 
        Process p=Runtime.getRuntime().exec(antinvoke); 
        p.waitFor(); 
        BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
        String line=reader.readLine(); 
        while(line!=null) 
        { 
          System.out.println(line); 
          line=reader.readLine(); 
        } 
      } 
      catch(IOException e) {
        System.out.println(e.getMessage());
      } 
      catch(InterruptedException e) {
        System.out.println(e.getMessage());
      } 
      System.out.println("build done"); 

      /*
      term = debug_1_0.instance.invoke(context, term, lifted6.instance);
      if(term == null)
        break Fail1;
      if(true)
        break Fail1;
      */
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}
package org.webdsl.stratego_editor;

import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.MetaFileLanguageValidator;

public class StrategoWebDSLJavaXMLValidator extends MetaFileLanguageValidator 
{ 
  public Descriptor getDescriptor()
  { 
    return StrategoWebDSLJavaXMLParseController.getDescriptor();
  }
}
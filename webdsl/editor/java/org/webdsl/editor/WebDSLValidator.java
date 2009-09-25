package org.webdsl.editor;

import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.MetaFileLanguageValidator;

public class WebDSLValidator extends MetaFileLanguageValidator 
{ 
  public Descriptor getDescriptor()
  { 
    return WebDSLParseController.getDescriptor();
  }
}
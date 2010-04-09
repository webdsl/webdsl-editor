module testimport/util
 
  // imports example
 
  entity Util : BaseUtil {
    prop :: Bool
    dfdfgs -> Bla
    stringprop :: String
  }
  
  define template a(){
    "aaaaaa"
    
    var NotAglobal : Util := Util{};
  }

  entity ExtEntity{}

//var GGlobalVar : String;
var GlobalVar : Util := Util{};

globals {
  var GlobalVarInBlock : Util := Util{};
}

function testUtilFunc() {
  
  z(); // external def
  
}



entity SomeChildEntity : BaseUtil {
  
  function someFunc() {
    
    baseFunc();
    
  } 
  
}
module testimport/util
 
imports
  testimport/util2
 
  // imports example
 entity EntA {
    prop :: String
    
    function testF() {}
    
  }
  
 
  entity Util : BaseUtil {
    prop :: Bool
    //dfdfgs -> Bla
    stringprop :: String
  }
  
  define template a(){
    "aaaaaa"
    
    var NotAglobal : Util := Util{};
  }

  entity ExtEntity{}

  derive crud ExtEntity
  
    

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


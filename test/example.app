application example

  imports testimport/util
  
  entity X {
    
    z -> Y (inverse = Y.z)
    
  }
  entity Y {
    z -> X
  }
  
  /*
  entity Utilfg{}
 
  session Aaa{
    i :: Int
    fdfd -> Util
  }
  
  entity Util {}
  
  entity Foo : Bla {
    i::Int
  }
 
  define xxx() {}

  define yyy() {
    //zzz()
    xxx()
  }
  */
  
  /*
  entity Util {
    stringprop :: String  
  }
  */
  
  define template someTemplate() {
    
    navigate("Google", url("http://www.google.nl"))
    
    action xyz() {
      url("blabla", "sjasja");
    }
    
    
  }
  
  
  entity BaseUtil {
    function baseFunc() {
      
    }
  }
  
  function findUtil(s : String) : Util {
    return null;
  }
  
  extend entity Util {
    test -> Util := findUtil(stringprop)
    stringprop2 :: String
    bla -> List<Util>
    
    zzz -> ExtEntity
    
    function z() {
      this.stringprop := "x"; //stringprop;
      this.stringprop2 := "x";
      stringprop := "x";
      stringprop2 := "x";
      
      var x := Util{};
      x.z();
      
      z();
      
      baseFunc();
      x.baseFunc();
    }
    
  }
  
  entity AnEntity {
    
    function z() {
      this.z();
      
      // Global var:
      var x := GlobalVar;
      x := GlobalVarInBlock;
      //x := NotAglobal;
      
      var z : List<String> := null;
      for(x : String in z) {
        if (x == "abc") {
          
          
        }
      }
      
    }
    
  }
  
  entity Bla{
    prop :: Bool
    sdfsdf1 :: String := this.prop.toString()
    sdfsdf2 :: String (not empty) := this.prop.toString()
    fdfs -> Bla
    fdfsfdfd :: String (not empty)
    dfgdfg1 -> Set<Aaa>
    dfgdfg2 -> List<Aaa>
    dfdgdfg :: Email
  }
  
  entity Aaa {}
  
  define page root(){}
/*
  define page root(){ 
    var i
    gfhfhg
    output(i)
    a()
  }  define page roohgjhgt(){ 
    var i
    gfhfhg
    output(i)
  }
*/
   
  define fs45dghjhgjdf(i: Int, b: Bool){  }
  define fs45ddf(i: Int, b: Bool){  }

  define fsdshdf(b: Bool){
     
  } 
  
  entity Util2
  {
    p -> Util
    
    function tstFunctionInEntity(i : Int) {
      tstFunctionInEntity("x");
    }
    function tstFunctionInEntity(s : String) {
      tstFunctionInEntity(3);
      x(3);
      x(s);
    }
   }
    
  function x(i : Int) : Int{return i;}
  function x(s : String) : Int {return 2;}
  
  function noret(i : Int) {}
  
  function z() {
    x(1);
    x("s");
    noret(3);
    
    var x : Int;
    //ejklrgeriohgr := "S";
  }
  
  define template x() {
    y(3)
  }
  
  define template y(i : Int) {
  
    action xxx() {}
    
  }
  
  entity User {
    name :: String
    pass :: String  
  }
  
  principal is User with credentials name, pass
  
  function someFunc() {
    
    var x := securityContext.name;
    
  }
  
  entity Question {
    
    questionCollapsedFor -> List<User>
    testSimpleProp :: String := securityContext.name
    
    questionCollapsed :: Bool := securityContext.principal in questionCollapsedFor
  }
  
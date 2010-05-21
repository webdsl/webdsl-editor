application example

  imports testimport/util
  imports ui/test1
  
  define page root() {}
  

session SomeSession {
  
  
  
}

// --------

  entity X {
    
    z -> Y (inverse = Z.z)
    zz -> Z (inverse = Z.extendedProperty, name)
    zzz -> SomeSession
    
  }
  entity Y {
    z -> X
  }
  entity Z : Y {
    
  }
  extend entity Z {
    
    extendedProperty -> X
    
  }
  
  entity Utilfg{}
 
  entity Foo : Bla {
    i::Int
  }
 
  define xxx() {}

  define yyy() {
    //zzz()
    xxx()
  }
  
  extend entity Util {
    stringprop :: String  
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
    header {  }
  }
  
  define template y(i : Int) {
  
    action xxx() {}
    
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
  
  function globalFunc1() : Question { return null;}
  
  var globalQuestion := globalFunc1();
  
  entity LaterEntity {}
  
  var globalVar2 := globalVar3;
  var globalVar3 := User{};
  
  function globalFunc2() : LaterEntity {
    var xx1 := globalVar2;
    var xx2 := globalVar3;
    return null;
  }
  
  entity BaseUser {
  	s :: String (id)
  	
  	function SomeFuncInBase() {}
  }
  
  entity User : BaseUser {
    
    function SomeFunc() {}
    
    extend function SomeFunc() {}
    
    name :: String
    pass :: String 
    
  }
  
  extend entity User {}
  
  function forExp() {
    
    var zzz : User;
    zzz.s := "x";
    
    var users : List<User>;
    
    for(x : User in users) {
      
      var piet := x;
      isUniqueBaseUser(piet);
      
    }
    
    
  }
  
  function SomeFunction() { }
  
  extend function SomeFunction() {
    var x := 3;
  }
  
  
  
  // ac test
  access control rules
 
      rule page somePage() { true }
      rule page somePage2(i : Int) { true }
      rule template someTemplate() { true }
      rule template someTemplate(i : Int) { true }
      rule template nonOverloaded() { true }
      rule action someAction() { true }
      
      rule template someTemplate(*) { true }
      rule template some*(*) { true }
  
      rule page somePage*(*) { true }
  
section ui
  
  define template someTemplate() { }
  define template someTemplate(i : Int) { }
  define template someOtherTemplate( i: Int) { }
  define template nonOverloaded() { }

  define page somePage() {
    action action1() {}
  }
  define page somePage2(a : Int) {
    action action1() { }
  }

define page zzzz() {
    
    navigate manageExtEntity() { "Goto " }
    
  }
    

section double def problems

  
  function testScope() {
    
    var x : EntA;
    x.prop := "a";
    x.testF();
    
    var y : EntB;
    y.prop := "a";
    y.testF();
    
    var z := requestVar1;
    
  }
  
  
  function functionNoReturn() {
    
    var x := 3;
    var y := 4;
    var z := 5;
  }
  
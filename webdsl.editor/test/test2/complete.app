application example

define page root () {} 

section ac.str

access control rules
  principal is User with credentials name, pass

// ac -> def

  // Exact 
  rule page somePage() { true }
  rule page somePage2(i : Int) { true }
  rule template someTemplate() { true }
  rule template someTemplate(i : Int) { true }
  rule template nonOverloaded() { true }
  
  // Wildcard
  rule template someTemplate(*) { true }
  rule template some*(*) { true }

  // Multiple wildcards
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

function testAcResolving() {
  var x := securityContext.name;
}



section actions.str

define page testActionPage() {
	
	submit testAction() {"xxx"}
	
	action testAction() {
		return testActionPage();
	}
	
	
}






section entity.str

// find property

entity Base {
	baseProp :: String
	inverseProp -> Sub
}
entity Sub : Base {
	subProp :: String
	
	function x() {
		// Implicit field access
		baseProp := "x";
		subProp := "x";
		var z := this;
	}
	
	// Inverse prop
	inverseProp2 -> Base (inverse = Base.inverseProp)
	// Derived prop
	derivedProp -> Base := this.inverseProp2
}

// Extend entity
extend entity Sub {}

function testEntity() {
	
  var x : Sub;
  // Field access
  x.subProp := "x";
  x.baseProp := "x";	
  

}





section function.str


// Global func (overloaded)
function globalFunc1() {}
function globalFunc1(i : Int) {}

function globalTestFunc() {
  globalFunc1();
  globalFunc1(1);
}

// Extend function
extend function globalTestFunc() {}


// Entity functions
entity TestEntityFunc {
 
  function entFunc1() : String { return null; }
  function entFunc1(i : Int) : Int { return null; }
  
  function entTestFunc() {
    // Thiscall
    entFunc1().length();
    entFunc1(3);
    
    // Call
    this.entFunc1().length();
    this.entFunc1(3);
  }
  
  extend function entTestFunc() {}
  
}

entity TestEntityFunc2 : TestEntityFunc {
  function entTestFunc() {
    entFunc1();     // from super classs
    entTestFunc();  // from this class
  }
 
}

// Built ins

define template testBuiltinsPage() {
  
  init {
    
  }
  
  navigate url("xxx") { }
  
}


// Predicate hover
access control rules
  predicate isAccessible (i : Int) { true }
  predicate isAccessibleA () { isAccessible(3) }




section imports.str

imports imp1










section template.app

define template templateA() {
	templateA()
	templateA(3)
	
	navigate root() {}
	
	init {
		email (someEmail() );
	}
	
	// Built-in
	par { }
}

define template templateA(i : Int) {}


define email someEmail() {
	to("") from("") subject("")
}










section types.str

entity EntA {}
entity EntB : EntA {}
session EntS {} 

function testTypesFunc() {
	var x : List<String>;
	x.length;
}





section vars.str

function testVars() {
	
	// Object creation
	var x := User { name := "xxx" };
	
	// Var type inferred
	x;
	
}







// Helpers

section helpers

entity User {
  
  name :: String(name)
  pass :: String
  
}
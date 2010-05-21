module ui/test1

// imports ui/test2

access control rules

	rule template xyz() {
		
		securityContext.principal == null
		
	}
	
section blaat

define page blaat2() {
	
	init {
		
		var x := securityContext;
		var y := securityContext.principal;
		// Hover principal: no hover, 11/12x good, with clause failed
		// (number of worker threads???) 
		
	}
	
}

session anSession {
	
}

// TODO: VarDecl for session vars

define page blaat3() {
	
	init {
		
		var x := anSession;
		
		
	}
	
}
Datalog fact extraction for meta-programming seminar assignment

facts:

//entity(name).
entity('Author').

//super(superclass, subclass).
super('Person','Author').

//property(entity name, property name, property type).
property('Author','name','String').

//entityfunction(entity name, unique function name, overloaded function name, return type).
//combination of entity and unique function name is unique, different entities can have the same unique function name
entityfunction('Aaa','blaInt','bla','').

//entityfunctionargument(entity name, unique function name, argument number, argument name, argument type).
entityfunctionargument('Aaa','blaInt',1,'i','Int').

//page(name). 
//name must be unique, also templates may not use this name
page('person')

//pageargument(page name,argument number, argument name, argument type).
pageargument('person',1,'arg','Person').

//template(unique name, overloaded name).
template('yInt','y').

//templateargument(unique template name, argument number, argument name, argument type).
templateargument('yInt',1,'i','Int').

//templatecall(calling page name or unique template name, unique template name being called).
templatecall('root','yInt').

template signatures
template calls
navigates
function calls
actions
ac rules (only full match)
nested ac rules
inverse
forms 
databind blocks

possible analysis: 
databind/submit in form (nesting)
unreachable pages
page clusters
which ac on an action (taking into account page rules)
entity clusters
entity used in template
navigate to same page

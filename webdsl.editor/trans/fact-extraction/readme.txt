Datalog fact extraction for meta-programming seminar assignment

facts
=====

datamodel
---------

//entity(name).
entity('Author').

//super(superclass, subclass).
super('Person','Author').

//property(entity name, property name, property type).
property('Author','name','String').

//inverse(entity1, property of entity1, entity2, property of entity2).
inverse('Foo','a','Bar','b').

functions
---------

//entityfunction(entity name, unique function name, overloaded function name, return type).
//combination of entity and unique function name is unique, different entities can have the same unique function name
entityfunction('Aaa','blaInt','bla','').

//entityfunctionargument(entity name, unique function name, argument number, argument name, argument type).
entityfunctionargument('Aaa','blaInt',1,'i','Int').

//globalfunction(unique name, overloaded name, return type).
globalfunction('loadSessionMessageUUID','loadSessionMessage','SessionMessage').

//globalfunctionargument(unique function name, argument number, argument name, argument type).
globalfunctionargument('loadSessionMessageUUID',1,'prop','UUID').

//functioncallentitytoglobal(calling entity name, calling unique entity function name, called unique global function name).
functioncallentitytoglobal('Util2','tstFunctionInEntityString','xInt').

//functioncallentitytoentity(calling entity name, calling unique entity function name, called entity name, called unique entity function name).
functioncallentitytoentity('Util2','tstFunctionInEntityString','Util2','tstFunctionInEntityInt').

//functioncallglobaltoglobal(calling unique global function name, called unique global function name).
functioncallglobaltoglobal('global1','global2Int').

//functioncallglobaltoentity(calling unique global function name, called entity name, called unique entity function name).
functioncallglobaltoentity('global1','Test','foo').

pages and templates
-------------------

//page(name). 
//name must be unique, also templates may not use this name
page('person')

//pageargument(page name,argument number, argument name, argument type).
pageargument('person',1,'arg','Person').

//template(unique name, overloaded name).
template('yInt','y').

//ajaxtemplate(unique template name).
ajaxtemplate('showMessages').

//templateargument(unique template name, argument number, argument name, argument type).
templateargument('yInt',1,'i','Int').

//templatecall(calling page name or unique template name, unique template name being called).
templatecall('root','yInt').

//navigate(referring page name or unique template name, referred page name or unique template name).
navigate('root','editPerson').

//action(page name or unique template name, action name).
action('root','bla').

//actionargument(page name or unique template name, action name, argument number, argument name, argument type).
actionargument('root','bla',1,'i','Int').

//functioncallactiontoglobal(page name or unique template name, action name, unique global function name).
functioncallactiontoglobal('root','bla','global2Int').

//functioncallactiontoglobal(page name or unique template name, action name, entity name, unique entity function name).
functioncallactiontoentity('root','bla','Test','foo').

access control
--------------

//acrule(ruletype, page name or unique template name or non-unique action name).
acrule('page','root').
acrule('template','foobar').
acrule('ajaxtemplate','ajaxfoobar').
acrule('action','someAction').

//acrule(ruletype,page name or unique template name,nested ruletype (only 'action' possible), nested action name).
acrule('page','root','action','save').
acrule('template','foobar','action','cancel').
acrule('ajaxtemplate','ajaxfoobar','action','update').

//functioncallacruletoglobal(ruletype,page name or unique template name,called global function unique name).
functioncallacruletoglobal('page','root','global2Int').

//functioncallacruletoglobal(ruletype,page name or unique template name,nested ruletype (only 'action' possible), nested action name,called global function unique name).
functioncallacruletoglobal('page','root','action','save','global2Int').

//functioncallacruletoentity(ruletype,page name or unique template name,entity name, unique entity function name).
functioncallacruletoentity('page','root','Test','foo').

functioncallacruletoentity(ruletype,page name or unique template name,nested ruletype (only 'action' possible), nested action name, entity name, unique entity function name).
//functioncallacruletoentity('page','root','action','nsdndgi','Test','foo').



notes
============

split collection types
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

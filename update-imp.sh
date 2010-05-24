cp ~/spoofax-imp/org.strategoxt.imp.generator/lib/sdf2imp.jar utils
export LOCALCLASSPATH=utils/sdf2imp.jar
ant -f build.main.xml sdf2imp

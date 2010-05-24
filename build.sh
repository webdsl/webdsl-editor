#!/bin/bash
# TODO: set ${eclipse.spoofaximp.jars} to spoofax dir
# TODO: set ${eclipse.spoofaximp.strategojar} to strategoxt.jar from spoofax
# TODO: add -Xmx2700m

# export LOCALCLASSPATH=~/.nix-profile/share/strc-java/strategoxt.jar
# export LOCALCLASSPATH=/usr/local/share/strc-java/strategoxt.jar
export LOCALCLASSPATH=~/strc-java/java/strategoxt.jar
export ANT_OPTS=-Xmx1024m
ant -f build.main.xml

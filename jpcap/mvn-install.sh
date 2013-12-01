#!/bin/sh

mvn install:install-file -Dfile=0.01.18/jpcap.jar -DgroupId=net.sourceforge.jpcap -DartifactId=jpcap -Dversion=0.01.18 -Dpackaging=jar -DgeneratePom=true

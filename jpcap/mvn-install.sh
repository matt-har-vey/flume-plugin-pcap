#!/bin/sh

mvn install:install-file -Dfile=0.7/jpcap.jar -DgroupId=net.sourceforge.jpcap -DartifactId=jpcap -Dversion=0.7.0 -Dpackaging=jar -DgeneratePom=true

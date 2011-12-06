### Instructions based on Ubuntu Narwhal (11.04)

	echo "deb http://archive.canonical.com/ubuntu natty partner" > /etc/apt/sources.list.d/partner.list
	apt-get update
	apt-get install build-essential sun-java6-jdk libpcap-dev
	wget http://netresearch.ics.uci.edu/kfujii/Jpcap/jpcap-0.7.tar.gz
	tar xfz jpcap-0.7.tar.gz
	cd jpcap-0.7/src/c
	export JAVA_HOME=/usr/lib/jvm/java-6-sun-1.6.0.26
	sed -i 's/$(COMPILE_OPTION)/-shared -L. -fPIC/' Makefile
	make
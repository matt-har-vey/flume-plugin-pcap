Usage
=====

### Note: Flume needs to run as ``root`` in order to bind to an interface.

### Dependency
Install ``libpcap`` on your system. Should your version be different from the one used for the .so here just try to symlink to the version requested, the features we use should be available in most versions.

### Libraries
	cp jpcap/0.7/jpcap.jar /usr/lib/flume/lib/
	cp jpcap/0.7/libjpcap.so-i386 /usr/lib/flume/lib/libjpcap.so # Choose your architecture (i386 or x86_64)
	cp target/flume-plugin-pcap-0.0.1-SNAPSHOT.jar /usr/lib/flume/lib/

### Config
Set the following properties in your configuration (``flume-site.xml``):

	flume.plugin.classes = com.cloudera.flume.handlers.PcapSource
	flume.plugin.outputformat.classes = com.cloudera.flume.handlers.PcapOutputFormat

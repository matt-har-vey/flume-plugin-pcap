Usage
=====

### Note: Flume needs to run as ``root`` in order to bind to an interface.

### Libraries
The libjpcap.so checked in to this repository was built on Debian wheezy amd64. If you are on another platform, you may need to build jpcap from source. See also my fork of jpcap where I hacked up makefiles so that the build would actually work in my environment.

	cp jpcap/0.01.18/jpcap.jar $FLUME_HOME/plugins.d/pcap-source/libext
	cp jpcap/0.01.18/libjpcap.so $FLUME_HOME/plugins.d/pcap-source/native/libjpcap.so # Choose your architecture (i386 or x86_64)
	cp target/flume-plugin-pcap-0.0.1-SNAPSHOT.jar $FLUME_HOME/plugins.d/pcap-source/lib

### Config
The source expects two properties: device, which is required, and filter, which is optional. An example agent configuration follows:

### Example

  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1
  
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 100000
  a1.channels.c1.transactionCapacity = 1000
  
  a1.sources.r1.type = com.cloudera.flume.handlers.PcapSource
  a1.sources.r1.channels = c1
  a1.sources.r1.device = eth0
  
  a1.sinks.k1.type = hdfs
  a1.sinks.k1.channel = c1
  a1.sinks.k1.hdfs.path = hdfs://muscari/user/mharvey/flume-pcap/%y-%m-%d/%H
  a1.sinks.k1.hdfs.useLocalTimeStamp = true
  a1.sinks.k1.hdfs.rollCount = 0
  a1.sinks.k1.hdfs.rollInterval = 0
  a1.sinks.k1.hdfs.rollSize = 10000000
  a1.sinks.k1.hdfs.filePrefix = pcapSequence

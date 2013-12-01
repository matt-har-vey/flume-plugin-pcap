Usage
=====

### Note: Flume needs to run as ``root`` in order to bind to an interface.

### Issue
This is running for me with the recent changes, but after some time I get a segfault. Perhaps I can fix it by trying a different version of jpcap; perhaps your mileage will vary. I am pausing work here and changing strategy to to libpcap + Avro C into the stock Flume AvroSource.

  V  [libjvm.so+0x6498a9]  jni_SetByteArrayRegion+0xa9
  C  [libjpcap.so+0x53fc]  get_packet+0x95c
  C  [libjpcap.so+0x430c]  Java_jpcap_JpcapCaptor_getPacket+0x123
  j  jpcap.JpcapCaptor.getPacket()Ljpcap/packet/Packet;+0

### Dependency
Install ``libpcap`` on your system. Should your version be different from the one used for the .so here just try to symlink to the version requested, the features we use should be available in most versions.

### Libraries
	cp jpcap/0.7/jpcap.jar $FLUME_HOME/plugins.d/pcap-source/libext
	cp jpcap/0.7/libjpcap.so-i386 $FLUME_HOME/plugins.d/pcap-source/native/libjpcap.so # Choose your architecture (i386 or x86_64)
	cp target/flume-plugin-pcap-0.0.1-SNAPSHOT.jar $FLUME_HOME/plugins.d/pcap-source/lib

### Config
The source expects two properties: device, which is required, and filter, which is optional. An example agent configuration follows:

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

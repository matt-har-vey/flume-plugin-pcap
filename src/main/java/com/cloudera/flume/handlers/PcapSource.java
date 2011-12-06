package com.cloudera.flume.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.Packet;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SourceFactory.SourceBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSource;
import com.cloudera.flume.core.Event.Priority;
import com.cloudera.util.Clock;
import com.cloudera.util.NetUtils;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class PcapSource extends EventSource.Base {
	private final int SNAPLEN = 65535;

	private NetworkInterface device;
	private String filter;

	private JpcapCaptor captor;

	public PcapSource(String deviceName) throws IOException {
		this(deviceName, null);
	}

	public PcapSource(String deviceName, String filter) throws IOException {
		this.device = getDevice(deviceName);
		this.filter = filter;
	}

	protected PcapSource(NetworkInterface device, String filter) {
		this.device = device;
		this.filter = filter;
	}

	public static NetworkInterface getDevice(String deviceName) throws IOException {
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		for (NetworkInterface device : devices) {
			if (deviceName.equals(device.name))
				return device;
		}
		throw new IOException("Could not find device: " + deviceName);
	}

	@Override
	public Event next() throws IOException {
		Packet packet;
		// getPacket returns null if it timed out (should be disabled wherever possible) - then just retry until we get the next result
		while ((packet = captor.getPacket()) == null);
		byte[] packetBytes = ByteBuffer.allocate(packet.data.length + packet.header.length).put(packet.header).put(packet.data).array();
		Event event =  new EventImpl(packetBytes, packet.sec * 1000L, Priority.INFO, Clock.nanos(), NetUtils.localhost());
		event.set("timestamp_sec", PcapUtils.convertInt((int)packet.sec));
		event.set("timestamp_usec", PcapUtils.convertInt((int)packet.usec));
		return event;
	}

	@Override
	public void open() throws IOException {
		captor = JpcapCaptor.openDevice(device, SNAPLEN, false, 0);
		if (filter != null)
			captor.setFilter(filter, true);
	}

	@Override
	public void close() throws IOException {
		captor.close();
	}

	public static SourceBuilder builder() {
		return new SourceBuilder() {
			@Override
			public EventSource build(Context ctx, String... args) {
				Preconditions.checkArgument(args.length == 1 || args.length == 2, "usage: pcap(device[,filter])");
				String device = args[0];
				String filter = null;
				if (args.length == 2)
					filter = args[1];

				try {
					return new PcapSource(device, filter);
				} catch (IOException e) {
					throw new IllegalArgumentException("Unable to open PCAP on device " + device + " with filter " + filter, e);
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static List<Pair<String, SourceBuilder>> getSourceBuilders() {
		return Lists.newArrayList(new Pair<String, SourceBuilder>("pcap", builder()));
	}
}
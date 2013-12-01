package com.cloudera.flume.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.packet.Packet;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcapSource extends AbstractSource implements Configurable,
		Runnable {
  private static final Logger logger = LoggerFactory
	      .getLogger(PcapSource.class);

	private final int SNAPLEN = 65535;

	private static final String DEVICE = "device";
	private static final String FILTER = "filter";

	private String deviceName;
	private String filter;

	private JpcapCaptor captor;

	private Thread pcapThread;
	private boolean stopped;

	@Override
	public void configure(Context context) {
		Configurables.ensureRequiredNonNull(context, DEVICE);
		deviceName = context.getString(DEVICE);
		filter = context.getString(FILTER);
	}

	@Override
	public void run() {
		while (!stopped) {
			Packet packet;
			// getPacket returns null if it timed out (should be disabled
			// wherever possible) - then just retry until we get the next result
			while ((packet = captor.getPacket()) == null)
				;
			byte[] packetBytes = ByteBuffer
					.allocate(packet.data.length + packet.header.length)
					.put(packet.header).put(packet.data).array();

			final int isec = (int)packet.sec;
			final int iusec = (int)packet.usec;
			long timestamp = (isec << 32) | iusec;

			// Strings do not feel efficient here, but for now live with it so
			// the payload can be the packet.
			final Map<String, String> headers = new LinkedHashMap<String, String>();
			headers.put("timestamp", Long.toString(timestamp));
			headers.put("timestamp_sec", Long.toString(packet.sec));
			headers.put("timestamp_usec", Long.toString(packet.usec));

			final Event event = EventBuilder.withBody(packetBytes, headers);
			getChannelProcessor().processEvent(event);
		}
	}

	@Override
	public void start() {
		stopped = false;
		final NetworkInterface device = getDevice(deviceName);
		try {
			captor = JpcapCaptor.openDevice(device, SNAPLEN, false, 0);
			if (filter != null)
				captor.setFilter(filter, true);
		} catch (IOException e) {
			logger.error("Failed to start packet capture", e);
		}
	}

	@Override
	public void stop() {
		stopped = true;
		pcapThread.interrupt();
		captor.close();
	}

	@Override
	public String toString() {
		return "pcap source " + getName() + ": { device: " + deviceName
				+ ", filter: " + filter + " }";
	}

	private static NetworkInterface getDevice(String deviceName) {
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		for (NetworkInterface device : devices) {
			if (deviceName.equals(device.name))
				return device;
		}
		return null;
	}
}
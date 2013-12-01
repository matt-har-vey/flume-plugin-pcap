package com.cloudera.flume.handlers;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sourceforge.jpcap.capture.CaptureDeviceOpenException;
import net.sourceforge.jpcap.capture.CapturePacketException;
import net.sourceforge.jpcap.capture.InvalidFilterException;
import net.sourceforge.jpcap.capture.PacketCapture;
import net.sourceforge.jpcap.capture.RawPacketListener;
import net.sourceforge.jpcap.net.RawPacket;
import net.sourceforge.jpcap.util.Timeval;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcapSource extends AbstractSource implements Configurable,
		Runnable, EventDrivenSource, RawPacketListener {
  private static final Logger logger = LoggerFactory
	      .getLogger(PcapSource.class);

	private static final int SNAPLEN = 65535;

	private static final String DEVICE = "device";
	private static final String FILTER = "filter";

	private String deviceName;
	private String filter;

	private PacketCapture capture;

	private Thread pcapThread;
	private boolean stopped;

	@Override
	public void configure(Context context) {
		Configurables.ensureRequiredNonNull(context, DEVICE);
		deviceName = context.getString(DEVICE);
		filter = context.getString(FILTER);

		try {
			capture = newCapture(deviceName);
		} catch (CaptureDeviceOpenException e) {
			throw new IllegalArgumentException("Failed to open device", e);
		}

		if (filter != null)
			try {
				capture.setFilter(filter, true);
			} catch (InvalidFilterException e) {
				throw new IllegalArgumentException("Invalid filter", e);
			}
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				capture.capture(1);
			} catch (CapturePacketException e) {
				logger.error("Error capturing packet", e);
			}
		}
	}

	@Override
	public void rawPacketArrived(RawPacket rawPacket) {
		final Timeval timeval = rawPacket.getTimeval();
		final byte[] data = rawPacket.getData();

		final long sec = timeval.getSeconds();
		final int usec = timeval.getMicroSeconds();
		long timestamp = timeval.getDate().getTime();

		// Strings do not feel efficient here, but for now live with it so
		// the payload can be the packet.
		final Map<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("timestamp", Long.toString(timestamp));
		headers.put("timestamp_sec", Long.toString(sec));
		headers.put("timestamp_usec", Integer.toString(usec));

		final Event event = EventBuilder.withBody(data, headers);
		getChannelProcessor().processEvent(event);
	}

	@Override
	public void start() {
		capture.addRawPacketListener(this);

		pcapThread = new Thread(this);
		pcapThread.setName("pcap source capture loop");
		stopped = false;
		pcapThread.start();
	}

	@Override
	public void stop() {
		if (!stopped) {
			stopped = true;
			try {
				pcapThread.join();
			} catch (InterruptedException e) {
			}
		}
		capture.close();
	}

	@Override
	public String toString() {
		return "pcap source " + getName() + ": { device: " + deviceName
				+ ", filter: " + filter + " }";
	}

	private static PacketCapture newCapture(String deviceName) throws CaptureDeviceOpenException {
		final PacketCapture capture = new PacketCapture();
		capture.open(deviceName, SNAPLEN, false, 0);
		return capture;
	}
}
package com.cloudera.flume.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.flume.Event;
import org.apache.flume.serialization.EventSerializer;

class PcapOutputFormat implements EventSerializer {
	private static final int MAGIC_NUMBER = 0xA1B2C3D4;
	private static final short VERSION_MAJOR = 2;
	private static final short VERSION_MINOR = 4;
	private static final int TIMEZONE_OFFSET = 0;
	private static final int ACCURACY = 0;
	private static final int SNAPLEN = 65535;
	private static final int DATA_LINK_TYPE = 1;

	private static final byte[] HEADER = ByteBuffer.allocate(24).
	                                                putInt(MAGIC_NUMBER).
	                                                putShort(VERSION_MAJOR).
	                                                putShort(VERSION_MINOR).
	                                                putInt(TIMEZONE_OFFSET).
	                                                putInt(ACCURACY).
	                                                putInt(SNAPLEN).
	                                                putInt(DATA_LINK_TYPE).
	                                                array();

	private boolean headerWritten = false;
	private OutputStream out;

	PcapOutputFormat(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(Event event) throws IOException {
		final int sec = Integer.valueOf(event.getHeaders().get("timestamp_sec"));
		final int usec = Integer.valueOf(event.getHeaders().get("timestamp_usec"));
		out.write(sec);
		out.write(usec);
		byte[] body = event.getBody();
		long bodyLength = body.length;
		out.write(PcapUtils.convertInt((int)bodyLength));
		out.write(PcapUtils.convertInt((int)bodyLength));
		out.write(body);
	}

	@Override
	public void afterCreate() throws IOException {
		writeHeader();
	}

	@Override
	public void afterReopen() throws IOException {
	}

	@Override
	public void beforeClose() throws IOException {
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public boolean supportsReopen() {
		return true;
	}

	private synchronized void writeHeader() throws IOException {
		if (!headerWritten) {
			out.write(HEADER);
			headerWritten = true;
		}
	}
}
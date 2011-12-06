package com.cloudera.flume.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.cloudera.flume.core.Event;
import com.cloudera.flume.handlers.text.FormatFactory.OutputFormatBuilder;
import com.cloudera.flume.handlers.text.output.OutputFormat;
import com.google.common.base.Preconditions;

public class PcapOutputFormat extends OutputFormatBuilder implements OutputFormat {
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

	private OutputFormatBuilder builder;

	private boolean headerWritten = false;

	@Override
	public void format(OutputStream out, Event event) throws IOException {
		writeHeader(out);
		writePacket(out, event);
		out.flush();
	}

	private synchronized void writeHeader(OutputStream out) throws IOException {
		if (!headerWritten) {
			out.write(HEADER);
			headerWritten = true;
		}
	}

	private void writePacket(OutputStream out, Event event) throws IOException {
		out.write(event.get("timestamp_sec"));
		out.write(event.get("timestamp_usec"));
		byte[] body = event.getBody();
		long bodyLength = body.length;
		out.write(PcapUtils.convertInt((int)bodyLength));
		out.write(PcapUtils.convertInt((int)bodyLength));
		out.write(body);
	}

	@Override
	public void setBuilder(OutputFormatBuilder builder) {
		this.builder = builder;
	}

	@Override
	public OutputFormatBuilder getBuilder() {
		return builder;
	}

	@Override
	public OutputFormat build(String... args) {
		Preconditions.checkArgument(args.length == 0, "usage: pcap");
		OutputFormat format = new PcapOutputFormat();
		format.setBuilder(this);
		return format;
	}

	@Override
	public String getName() {
		return "pcap";
	}
}
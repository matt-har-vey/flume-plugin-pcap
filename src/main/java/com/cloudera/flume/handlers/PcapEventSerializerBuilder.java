package com.cloudera.flume.handlers;

import java.io.OutputStream;

import org.apache.flume.Context;
import org.apache.flume.serialization.EventSerializer;

public class PcapEventSerializerBuilder implements EventSerializer.Builder {
	@Override
	public EventSerializer build(Context context, OutputStream out) {
		return new PcapOutputFormat(out);
	}
}

package com.cloudera.flume.handlers;

import java.nio.ByteBuffer;

public class PcapUtils {
	public static byte[] convertShort(short data) {
		return ByteBuffer.allocate(2).putShort(data).array();		
	}

	public static byte[] convertInt(int data) {
		return ByteBuffer.allocate(4).putInt(data).array();
	}

	public static byte[] convertLong(long data) {
		return ByteBuffer.allocate(8).putLong(data).array();		
	}
}
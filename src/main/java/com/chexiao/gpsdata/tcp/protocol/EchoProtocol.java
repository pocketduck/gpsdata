package com.chexiao.gpsdata.tcp.protocol;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface EchoProtocol {
	void handleAccept(SelectionKey key) throws IOException; 
	 void handleRead(SelectionKey key) throws IOException; 
	 void handleWrite(SelectionKey key) throws IOException; 
}

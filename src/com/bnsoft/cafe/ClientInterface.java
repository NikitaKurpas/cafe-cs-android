package com.bnsoft.cafe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientInterface {

	Socket client;

	InputStream IS;
	InputStreamReader ISR;
	BufferedReader in;

	OutputStream OS;
	OutputStreamWriter OSW;
	BufferedWriter BW;
	PrintWriter out;

	Logger log;

	/*
	 * CONNECTION ERROR IDs:
	 */

	private boolean isConnected = false;
	@SuppressWarnings("unused")
	private boolean isMsgSend = false;

	public ClientInterface() {
		// TODO Method Stub
		log = new Logger();
		client = new Socket();
	}

	public boolean isConnected() {
		return isConnected;
	}

	public String connect(String HOST, int PORT) {
		String returnMsg = "";

		try {
			log.l(log.TAG_DBG, "Trying to connect...");
			client.connect(new InetSocketAddress(HOST, PORT), 5000);

			if (client.isConnected()) {
				IS = client.getInputStream();
				ISR = new InputStreamReader(IS);
				in = new BufferedReader(ISR);

				OS = client.getOutputStream();
				OSW = new OutputStreamWriter(OS);
				BW = new BufferedWriter(OSW);
				out = new PrintWriter(BW);

				returnMsg = "Connected to " + HOST + ":"
						+ Integer.toString(PORT) + "!";
				isConnected = true;
			} else {
				returnMsg = "Could not connect to " + HOST + ":"
						+ Integer.toString(PORT) + "!";
				isConnected = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.l(log.TAG_DBG, "Unable to connect! DATA: HOST = " + HOST + " | PORT = " + PORT + " | e.msg = " + e.getMessage()
					+ " | e = " + e.toString());
			isConnected = false;
			returnMsg = "Could not connect to " + HOST + ":"
					+ Integer.toString(PORT) + "!";
		}

		return returnMsg;
	}

	public String disconnect() {
		String returnMsg = "";

		try {
			client.close();
			if (client.isClosed()) {
				returnMsg = "Connection closed!";
				isConnected = false;
			} else {
				returnMsg = "Connection not closed!";
				isConnected = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.l(log.TAG_DBG,
					"Unable to close socket! msg = " + e.getMessage()
							+ " | e = " + e.toString());
			isConnected = true;
		}
		return returnMsg;
	}
	
	public boolean isMsgSend() {
		return isMsgSend();
	}

	public String sendMsg(String msg) {
		String returnMsg = "";

		try {
			if (client.isConnected()) {
				out.println(msg);
				isMsgSend = true;
				returnMsg = "Data sent!";
			} else {
				returnMsg = "Socket is not connected!";
				isMsgSend = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.l(log.TAG_DBG,
					"Unable to send message! msg = " + e.getMessage()
							+ " | e = " + e.toString());
			isMsgSend = false;
		}
		return returnMsg;

	}

}

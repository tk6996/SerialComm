package com.epam;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Scanner;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

public class TestReadSerial {

	public static void main(String[] args) {
		CommPortIdentifier portId = null;
		OutputStream outStream = null;
		BufferedReader bufStreamRead = null;
		InputStream inStream = null;
		SerialPort serialPort = null;
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Port name: " + portId.getName());
				if (portId.getName().equals("COM13")) {
					break;
				}
			}
		}
		if (portId == null) {
			System.err.println("Could not find serial port " + "COM13");
			System.exit(1);
		}
		try {
			serialPort = (SerialPort) portId.open("Arduino", 10000);
			outStream = serialPort.getOutputStream();
			inStream = serialPort.getInputStream();
			bufStreamRead = new BufferedReader(new InputStreamReader(inStream));
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			Scanner sysin = new Scanner (System.in);
			while (true)
			{
				System.out.println(bufStreamRead.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
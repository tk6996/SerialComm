package com.epam;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;

public class Control {
    
    public CommPortIdentifier portId;
	public InputStream inputStream;
	public OutputStream outputStream;
    public SerialPort serialPort;
    
    public Control(CommPortIdentifier pid)
    {
        try {
			portId = pid;
			serialPort = (SerialPort) portId.open("Control", 1000);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    public int waitingStart() throws IOException
    {
       // while (inputStream.available() == 0)
       // {}
        BufferedInputStream bw = new BufferedInputStream(inputStream);
        int ch =  bw.read();
        System.out.println((char)ch);
        return ch;
    }
}
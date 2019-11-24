package com.epam;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class Control {

    public CommPortIdentifier portId;
    public InputStream inputStream;
    public OutputStream outputStream;
    public SerialPort serialPort;
    public BufferedInputStream bw;

    public Control(CommPortIdentifier pid) throws PortInUseException, IOException, UnsupportedCommOperationException
    {
			portId = pid;
			serialPort = (SerialPort) portId.open("Control", 1000);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            bw = new BufferedInputStream(inputStream);
			serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
    }
    public int waitingStart() throws IOException
    {
        int ch =  bw.read();
        System.out.println((char)ch);
        return ch;
    }
}
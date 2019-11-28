package com.epam;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

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
    public static void main(final String[] args) {
        try {
            final Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
            CommPortIdentifier pid = null;
            while (portList.hasMoreElements()) {
                pid = (CommPortIdentifier) portList.nextElement();
                if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    System.out.println("Port name: " + pid.getName());
                    if (pid.getName().equals("COM7")) {
                        Control ctr = new Control(pid);
                        while (true)
                        {
                            System.out.println(ctr.waitingStart());
                        }
                        
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Control(CommPortIdentifier pid) throws PortInUseException, IOException, UnsupportedCommOperationException {
        portId = pid;
        serialPort = (SerialPort) portId.open("Control", 1000);
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
        bw = new BufferedInputStream(inputStream);
        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    }

    public int waitingStart() throws IOException {
        int ch =  bw.read();
        System.out.println("Received : " + (char)ch + " Binary : "+ Integer.toBinaryString(ch));
        return ch;
    }
}
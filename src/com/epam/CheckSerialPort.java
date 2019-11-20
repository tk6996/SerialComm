package com.epam;

import java.util.Enumeration;

import javax.comm.CommPortIdentifier;

public class CheckSerialPort {
    private static CommPortIdentifier portId;
    public static void main(String[] args) {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
           portId = (CommPortIdentifier) portList.nextElement();
           if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
               System.out.println("Port name: " + portId.getName());
           }
       }
   }
}
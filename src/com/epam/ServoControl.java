package com.epam;

import java.io.*;
import javax.comm.*;

/**
 * ServoControl
 */
public class ServoControl {
    public CommPortIdentifier portId;
    public InputStream inputStream;
    public OutputStream outputStream;
    public SerialPort serialPort;

    public ServoControl(CommPortIdentifier pid)
            throws UnsupportedCommOperationException, IOException, PortInUseException {
        portId = pid;
        serialPort = (SerialPort) portId.open("Servo", 1000);
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    }

    public void servoHorizontalRotage(int angle) throws IOException {
        this.outputStream.write(1);
        this.outputStream.write(angle);
    }

    public void servoVerticalRotage(int angle) throws IOException {
        this.outputStream.write(2);
        this.outputStream.write(angle);
    }

    public void Degrees45() throws IOException {
        this.servoHorizontalRotage((byte) 45);
        sleep(1000);
        this.servoVerticalRotage((byte) 156);
    }

    public void Degrees90() throws IOException {
        this.servoHorizontalRotage((byte) 90);
        sleep(1000);
        this.servoVerticalRotage((byte) 148);
    }

    public void Degrees135() throws IOException {
        this.servoHorizontalRotage((byte) 139);
        sleep(1000);
        this.servoVerticalRotage((byte) 145);
    }

    public void rotage(int i) throws IOException {
        if (i == 0)
            Degrees45();
        if (i == 1)
            Degrees90();
        if (i == 2)
            Degrees135();
    }

    private void sleep(long ms) {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < ms) {

        }
    }
}
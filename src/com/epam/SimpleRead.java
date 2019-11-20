package com.epam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class SimpleRead {
	private static final  char[]COMMAND = {'*', 'R', 'D', 'Y', '*'};
	private static final int WIDTH = 320; //640;
	private static final int HEIGHT = 240; //480;
	private static final int NEWWIDTH = 240; //640;
    private static final int NEWHEIGHT = 180; //480;
    	
    private static CommPortIdentifier portId;
    InputStream inputStream;
    SerialPort serialPort;

    public static void main(String[] args) {
    	 Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
        	portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            	System.out.println("Port name: " + portId.getName());
                if (portId.getName().equals("COM14")) {
					SimpleRead sRead = new SimpleRead();
					sRead.readPic();
                }
            }
        }
    }
	public SimpleRead(){
		try {
			serialPort = (SerialPort) portId.open("SimpleReadApp", 1000);
            inputStream = serialPort.getInputStream();
            serialPort.setSerialPortParams(1000000,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    public void readPic() {
       	int[][]rgb = new int[HEIGHT][WIDTH];
       	int[][]rgb2 = new int[WIDTH][HEIGHT];
    	
    	try {
        	int counter = 0;

        	while(true) {
        		System.out.println("Looking for image");
        		while(!isImageStart(inputStream, 0)){};
        	
	        	System.out.println("Found image: " + counter);
	        	
	        	for (int y = 0; y < HEIGHT; y++) {
	        		for (int x = 0; x < WIDTH; x++) {
		       			int temp = read(inputStream);
		    			rgb[y][x] = ((temp&0xFF) << 16) | ((temp&0xFF) << 8) | (temp&0xFF);
	        		}
	        	}
	        	
	        	for (int y = 0; y < HEIGHT; y++) {
		        	for (int x = 0; x < WIDTH; x++) {
		        		rgb2[x][y]=rgb[y][x];
		        	}	        		
	        	}
				BMP bmp = new BMP();
				String namePic = "c:/datacom/pic/file" + counter + ".bmp";
				bmp.saveBMP(namePic, rgb2);
				ImageResizer.resize(namePic, namePic, NEWHEIGHT, NEWWIDTH);
				File file = new File("c:/datacom/data/file" + counter + ".csv");
				FileWriter fw = new FileWriter(file);
				for (int x = 0; x < WIDTH; x++) {
					StringBuilder buffStringBuilder = new StringBuilder();
					for (int y = 0; y < HEIGHT; y++){
						buffStringBuilder.append(0xFF & rgb2[x][y]).append(',');
					}
					buffStringBuilder.append('\n');
					fw.write(buffStringBuilder.toString());
				}
				fw.close();
				System.out.println("Saved image: " + counter);
				counter++;  
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private int read(InputStream inputStream) throws IOException {
    	int temp = (char) inputStream.read();
		if (temp == -1) {
			throw new  IllegalStateException("Exit");
		}
		return temp;
    }
    	
    private boolean isImageStart(InputStream inputStream, int index) throws IOException {
    	if (index < COMMAND.length) {
    		if (COMMAND[index] == read(inputStream)) {
    			return isImageStart(inputStream, ++index);
    		} else {
    			return false;
    		}
    	}
    	return true;
    }
}
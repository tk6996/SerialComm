package com.epam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class SimpleRead {
	private static final char[] COMMAND = { '*', 'R', 'D', 'Y', '*' };
	private static final int WIDTH = 320; // 640;
	private static final int HEIGHT = 240; // 480;
	private static final int NEWWIDTH = 240;
	private static final int NEWHEIGHT = 240;

	public CommPortIdentifier portId;
	public InputStream inputStream;
	public OutputStream outputStream;
	public SerialPort serialPort;

	public static void main(String[] args) {
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier pid = null;
		while (portList.hasMoreElements()) {
			pid = (CommPortIdentifier) portList.nextElement();
			if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Port name: " + pid.getName());
				if (pid.getName().equals("COM14")) {
					SimpleRead sRead = new SimpleRead(pid);
					// sRead.readPic();
					sRead.readPicCheck();
				}
			}
		}
	}

	public SimpleRead(CommPortIdentifier pid) {
		try {
			portId = pid;
			serialPort = (SerialPort) portId.open("SimpleReadApp", 1000);
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
			serialPort.setSerialPortParams(1000000, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int[][] readPic() {
		int[][] rgb = new int[HEIGHT][WIDTH];
		int[][] rgb2 = new int[WIDTH][HEIGHT];
		try {
			System.out.println("Looking for image");
			// waiting image start
			//while(inputStream.available() > 0) {System.out.println((char)inputStream.read());}
			while (!isImageStart(inputStream, 0)) {
				
			}
			System.out.println("Found image");
			// read image data from serial
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					int temp = read(inputStream);
					rgb[y][x] = ((temp & 0xFF) << 16) | ((temp & 0xFF) << 8) | (temp & 0xFF);
				}
			}
			// rotage image
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					rgb2[x][y] = rgb[y][x];
				}
			}
			// imagefile
			String namePic = "c:/datacom/use/file.bmp";
			String namePicBW = "c:/datacom/use/fileBW.bmp";
			String namePicBWWithCrop = "c:/datacom/use/fileBWWithCrop.bmp";
			// save image
			BMP bmp = new BMP();
			bmp.saveBMP(namePic, rgb2);
			// resize image
			// blackwhite filter
			ImageResizer.resize(namePic, namePic, NEWWIDTH, NEWHEIGHT);
			ConvertBlackWhite.Convert(namePic, namePicBW);
			cropImage(namePicBW, namePicBWWithCrop,0, 30, NEWWIDTH,NEWHEIGHT - 60);
			ImageResizer.resize(namePicBWWithCrop, namePicBWWithCrop,180, 180);
			BufferedImage image = ImageIO.read(new File(namePicBW));
			// // image .bmp -> to int[][] .csv
			// File file = new File("c:/datacom/use/fileData.csv");
			// FileWriter fw = new FileWriter(file);
			// for (int y = 0; y < image.getHeight(); y++) {
			// StringBuilder buffStringBuilder = new StringBuilder();
			// for (int x = 0; x < image.getWidth(); x++) {
			// buffStringBuilder.append(image.getRGB(x,y)).append(',');
			// }
			// buffStringBuilder.append('\n');
			// fw.write(buffStringBuilder.toString());
			// }
			// fw.close();
			int[][] bw = new int[image.getHeight()][image.getWidth()];
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					bw[y][x] = (image.getRGB(x, y) & 0xFF) < 0x40 ? 0 : 1;
				}
			}
			System.out.println("Saved image");
			return bw;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void readPicCheck() {
		int[][] rgb = new int[HEIGHT][WIDTH];
		int[][] rgb2 = new int[WIDTH][HEIGHT];

		try {
			int counter = 0;

			while (true) {
				System.out.println("Looking for image");
				while (!isImageStart(inputStream, 0)) {
					
				}

				System.out.println("Found image: " + counter);

				for (int y = 0; y < HEIGHT; y++) {
					for (int x = 0; x < WIDTH; x++) {
						int temp = read(inputStream);
						rgb[y][x] = ((temp & 0xFF) << 16) | ((temp & 0xFF) << 8) | (temp & 0xFF);
					}
				}

				for (int y = 0; y < HEIGHT; y++) {
					for (int x = 0; x < WIDTH; x++) {
						rgb2[x][y] = rgb[y][x];
					}
				}
				
				BMP bmp = new BMP();
				String namePic = "c:/datacom/pic/file" + counter + ".bmp";
				String namePicBW = "c:/datacom/pic/fileBW" + counter + ".bmp";
				bmp.saveBMP(namePic, rgb2);
				ImageResizer.resize(namePic, namePic, NEWWIDTH, NEWHEIGHT);
				ConvertBlackWhite.Convert(namePic, namePicBW);
				BufferedImage image = ImageIO.read(new File(namePicBW));
				File file = new File("c:/datacom/data/file" + counter + ".csv");
				FileWriter fw = new FileWriter(file);
				for (int y = 0; y < image.getHeight(); y++) {
					StringBuilder buffStringBuilder = new StringBuilder();
					for (int x = 0; x < image.getWidth(); x++) {
						buffStringBuilder.append(0xFF & image.getRGB(x, y)).append(',');
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
			throw new IllegalStateException("Exit");
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
	public static void cropImage(String inputPic,String outputPic,int x, int y, int width, int height) throws IOException{
		BufferedImage img = ImageIO.read(new File(inputPic));
		ImageIO.write(img.getSubimage(x, y, width, height),"bmp", new File(outputPic));
	}
}
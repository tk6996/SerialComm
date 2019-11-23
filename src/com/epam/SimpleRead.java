package com.epam;

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

	public CommPortIdentifier portId;
	public InputStream inputStream;
	public OutputStream outputStream;
	public SerialPort serialPort;

	public static void main(final String[] args) {
		try {
			final Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
			CommPortIdentifier pid = null;
			while (portList.hasMoreElements()) {
				pid = (CommPortIdentifier) portList.nextElement();
				if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					System.out.println("Port name: " + pid.getName());
					if (pid.getName().equals("COM14")) {
						final SimpleRead sRead = new SimpleRead(pid);
						// sRead.readPic();
						sRead.readPicCheck();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SimpleRead(final CommPortIdentifier pid)
			throws IOException, PortInUseException, UnsupportedCommOperationException {

		portId = pid;
		serialPort = (SerialPort) portId.open("SimpleReadApp", 1000);
		inputStream = serialPort.getInputStream();
		outputStream = serialPort.getOutputStream();
		serialPort.setSerialPortParams(1000000, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

	}

	public int[][] readPic() {
		final int[][] rgb = new int[HEIGHT][WIDTH];
		final int[][] rgb2 = new int[WIDTH][HEIGHT];
		try {
			System.out.println("Looking for image");
			// waiting image start
			// while(inputStream.available() > 0)
			// {System.out.println((char)inputStream.read());}
			while (!isImageStart(inputStream, 0)) {

			}
			System.out.println("Found image");
			// read image data from serial
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					final int temp = read(inputStream);
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
			final String namePicRaw = "c:/datacom/use/raw.bmp";
			final String namePic = "c:/datacom/use/file.bmp";
			final String namePicBW = "c:/datacom/use/fileBW.bmp";
			final String namePicBWWithCrop = "c:/datacom/use/fileBWWithCrop.bmp";
			// save image
			final BMP bmp = new BMP();
			bmp.saveBMP(namePicRaw, rgb2);
			// resize image
			// blackwhite filter
			ImageResizer.resize(namePicRaw, namePic, 240, 240);
			ConvertBlackWhite.Convert(namePic, namePicBW);
			cropImage(namePicBW, namePicBWWithCrop, 0, 30, 240, 180);
			ImageResizer.resize(namePicBWWithCrop, namePicBWWithCrop, 180, 180);
			final BufferedImage image = ImageIO.read(new File(namePicBWWithCrop));
			// image .bmp -> to int[][] .csv
			File file = new File("c:/datacom/use/fileData.csv");
			final BufferedImage img = ImageIO.read(new File(namePicRaw));
			FileWriter fw = new FileWriter(file);
			for (int y = 0; y < img.getHeight(); y++) {
				StringBuilder buffStringBuilder = new StringBuilder();
				for (int x = 0; x < img.getWidth(); x++) {
					buffStringBuilder.append(img.getRGB(x, y) & 0xFF).append(',');
				}
				buffStringBuilder.append('\n');
				fw.write(buffStringBuilder.toString());
			}
			fw.close();
			final int[][] bw = new int[image.getHeight()][image.getWidth()];
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					bw[y][x] = (image.getRGB(x, y) & 0xFF) < 0x40 ? 0 : 1;
				}
			}
			System.out.println("Saved image");
			return bw;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void readPicCheck() {
		final int[][] rgb = new int[HEIGHT][WIDTH];
		final int[][] rgb2 = new int[WIDTH][HEIGHT];

		try {
			int counter = 0;

			while (true) {
				System.out.println("Looking for image");
				while (!isImageStart(inputStream, 0)) {

				}

				System.out.println("Found image: " + counter);

				for (int y = 0; y < HEIGHT; y++) {
					for (int x = 0; x < WIDTH; x++) {
						final int temp = read(inputStream);
						rgb[y][x] = ((temp & 0xFF) << 16) | ((temp & 0xFF) << 8) | (temp & 0xFF);
					}
				}

				for (int y = 0; y < HEIGHT; y++) {
					for (int x = 0; x < WIDTH; x++) {
						rgb2[x][y] = rgb[y][x];
					}
				}

				final BMP bmp = new BMP();
				final String namePic = "c:/datacom/pic/file" + counter + ".bmp";
				final String namePicBW = "c:/datacom/pic/fileBW" + counter + ".bmp";
				bmp.saveBMP(namePic, rgb2);
				ImageResizer.resize(namePic, namePic, 240, 240);
				ConvertBlackWhite.Convert(namePic, namePicBW);
				final BufferedImage image = ImageIO.read(new File(namePicBW));
				final File file = new File("c:/datacom/data/file" + counter + ".csv");
				final FileWriter fw = new FileWriter(file);
				for (int y = 0; y < image.getHeight(); y++) {
					final StringBuilder buffStringBuilder = new StringBuilder();
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
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private int read(final InputStream inputStream) throws IOException {
		final int temp = (char) inputStream.read();
		if (temp == -1) {
			throw new IllegalStateException("Exit");
		}
		return temp;
	}

	private boolean isImageStart(final InputStream inputStream, int index) throws IOException {
		if (index < COMMAND.length) {
			if (COMMAND[index] == read(inputStream)) {
				return isImageStart(inputStream, ++index);
			} else {
				return false;
			}
		}
		return true;
	}

	public static void cropImage(final String inputPic, final String outputPic, final int x, final int y,
			final int width, final int height) throws IOException {
		final BufferedImage img = ImageIO.read(new File(inputPic));
		ImageIO.write(img.getSubimage(x, y, width, height), "bmp", new File(outputPic));
	}
}
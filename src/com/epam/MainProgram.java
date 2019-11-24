package com.epam;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import java.util.Enumeration;

import javax.comm.CommPortIdentifier;
import javax.imageio.ImageIO;

public class MainProgram {
	public static char[] angle = new char[3]; // -45* 0* 45*

	public static void main(String[] args) {
		try {
			final String Arduino2 = "COM15";
			final String Arduino3 = "COM14";
			final String Arduino4 = "COM16";
			SimpleRead sRead = null;
			Control ctr = null;
			ServoControl servo = null;
			final Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
			CommPortIdentifier pid = null;
			while (portList.hasMoreElements()) {
				pid = (CommPortIdentifier) portList.nextElement();
				if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					System.out.println("Port name: " + pid.getName());

					if (pid.getName().equals(Arduino2)) {
						ctr = new Control(pid);
					}
					if (pid.getName().equals(Arduino3)) {
						sRead = new SimpleRead(pid);
					}
					if (pid.getName().equals(Arduino4)) {
						servo = new ServoControl(pid);
					}
				}
			}

			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() - time < 5000) {

			}
			System.out.println("Ready Command");
			while (true) {
				// char comm = (char) System.in.read();
				char comm = (char) ctr.waitingStart();
				// System.out.println(comm);
				if (comm == 'S') {
					angle[0] = angle[1] = angle[2] = (char) -1;
					for (int i = 0; i < 3; i++) {
						// System.out.println(i + (int)'1');
						servo.rotage(i);
						char type = (char) -1;
						do {
							int[][] data = sRead.readPic();
							type = analysis(data);
						} while (type == (char) -1);
						angle[i] = type;
						System.out.println("Finish image " + i);
					}
					for (int i = 0; i < 3; i++) {
						switch (i) {
						case 0:
							System.out.println("angle = -45* , type = " + angle[0]);
							ctr.outputStream.write((byte)1);
							while((char)ctr.waitingStart() == -1){}
							ctr.outputStream.write(angle[0]);
							while((char)ctr.waitingStart() == -1){}
							break;
						case 1:
							System.out.println("angle = 0* , type = " + angle[1]);
							ctr.outputStream.write((byte)2);
							while((char)ctr.waitingStart() == -1){}
							ctr.outputStream.write(angle[1]);
							while((char)ctr.waitingStart() == -1){}
							break;
						case 2:
							System.out.println("angle = 45* , type = " + angle[2]);
							ctr.outputStream.write((byte)3);
							while((char)ctr.waitingStart() == -1){}
							ctr.outputStream.write(angle[2]);
							while((char)ctr.waitingStart() == -1){}
							break;
						default:
							break;
						}
					}
					servo.rotage(1);
				}
				if ("TBLRUD".indexOf(comm) >= 0) {
					for (int i = 0; i < 3; i++) {
						if (angle[i] == comm) {
							servo.rotage(i);
							char type = (char) -1;
							do {
								int[][] data = sRead.readPic();
								type = analysis(data);
							} while (type != angle[i]);
							sendDataPixel(ctr);
							// sendDataPixel(System.out);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static char analysis(int[][] data /* coordinate [y][x] */) {
		// System.out.println(data.length + " " + data[0].length);
		// pixelAnalysis coordinate [x][y]
		int[][] pixelAnalysis = { { 23, 68 }, // triangle lower left
				{ 68, 23 }, // triangle upper left
				{ 135, 45 }, // upper right
				{ 45, 135 }, // lower left
				{ 113, 158 }, // triangle lower right
				{ 158, 113 } }; // triangle upper right
		int collectWhite = 0; // check error
		for (int i = 0; i < 6; i++) {
			if (data[pixelAnalysis[i][1]][pixelAnalysis[i][0]] != data[pixelAnalysis[i][1]][pixelAnalysis[i][0] - 1]
					&& data[pixelAnalysis[i][1]][pixelAnalysis[i][0]] != data[pixelAnalysis[i][1]][pixelAnalysis[i][0]
							+ 1]) {
				System.out.println("Image Error  at x = " + pixelAnalysis[i][0] + " y = " + pixelAnalysis[i][1]);
				return (char) -1;
			}
		}
		for (int i = 0; i < pixelAnalysis.length; i++)
			collectWhite |= ((data[pixelAnalysis[i][1]][pixelAnalysis[i][0]] > 0 ? 1 : 0) << i);
		if (collectWhite == 0b000111)
			return 'T';
		else if (collectWhite == 0b111000)
			return 'B';
		else if (collectWhite == 0b001011)
			return 'L';
		else if (collectWhite == 0b110100)
			return 'R';
		else if (collectWhite == 0b100110)
			return 'U';
		else if (collectWhite == 0b011001)
			return 'D';
		else {
			System.out.println("Error Detect Value : " + Integer.toBinaryString(collectWhite));
			return (char) -1;
		}

	}

	public static void sendDataPixel(Control ctr) throws IOException {

		System.out.println("Waiting");
		BufferedImage buf = ImageIO.read(new File("c:/datacom/use/raw.bmp"));

		for (int i = 1; i < 5; i++) {
			for (int j = 1; j < 5; j++) {
				System.out.println("Pos x = " + j * buf.getWidth() / 5 + " Pos y = " + i * buf.getHeight() / 5
						+ " PixelValue = " + (buf.getRGB(j * buf.getWidth() / 5, i * buf.getHeight() / 5) & 0xFF));

				ctr.outputStream.write((byte) (0x1F & (byte) (j * buf.getWidth() / 5)));
				while((char)ctr.waitingStart() == -1){}
				ctr.outputStream.write((byte) ((0xF & (byte) ((j * buf.getWidth() / 5) >> 5)) ));
				while((char)ctr.waitingStart() == -1){}
				ctr.outputStream.write((byte) (0x1F & (int) (i * buf.getHeight() / 5)));
				while((char)ctr.waitingStart() == -1){}
				ctr.outputStream.write((byte) ((0xF & (byte) ((j * buf.getHeight() / 5) >> 5)) ));
				while((char)ctr.waitingStart() == -1){}
				ctr.outputStream.write((byte) (buf.getRGB(j * buf.getWidth() / 5, i * buf.getHeight() / 5) & 0xFF));
				while((char)ctr.waitingStart() == -1){}

			}
		}

		// return;*/
	}

}
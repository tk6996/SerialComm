package com.epam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.comm.CommPortIdentifier;

public class MainProgram {
	public static char[] angle = new char[3]; // -45* 0* 45*

	public static void main(String[] args) {
		final String Arduino2 = "COM7";
		final String Arduino3 = "COM14";
		SimpleRead sRead = null;
		Control ctr = null;
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier pid = null;
		while (portList.hasMoreElements()) {
			pid = (CommPortIdentifier) portList.nextElement();
			if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Port name: " + pid.getName());
				if (pid.getName().equals(Arduino3)) {
					sRead = new SimpleRead(pid);
				}
				if (pid.getName().equals(Arduino2)) {
					ctr = new Control(pid);
				}
			}
		}
		try {
			while (!(ready(sRead.inputStream, 0))) {
			}
			System.out.println("Ready Command");
			while (true) {
				char comm = (char) ctr.waitingStart();
				// System.out.println(comm);
				if (comm == 'S') {
					angle[0] = angle[1] = angle[2] = (char) -1;
					for (int i = 0; i < 3; i++) {
						// System.out.println(i + (int)'1');
						sRead.outputStream.write(i + (int) '1');
						while (!(ready(sRead.inputStream, 0))) {
						}
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
							ctr.outputStream.write(-45);
							ctr.outputStream.write(angle[0]);
							break;
						case 1:
							System.out.println("angle = 0* , type = " + angle[1]);
							ctr.outputStream.write(0);
							ctr.outputStream.write(angle[1]);
							break;
						case 2:
							System.out.println("angle = 45* , type = " + angle[2]);
							ctr.outputStream.write(45);
							ctr.outputStream.write(angle[2]);
							break;
						default:
							break;
						}
					}
				}
				if ("TBLRUD".indexOf(comm) >= 0) {
					for (int i = 0; i < 3; i++) {
						if (angle[i] == comm) {
							sRead.outputStream.write(i + '1');
							while (!(ready(sRead.inputStream, 0))) {
							}
							char type = (char) -1;
							do {
								int[][] data = sRead.readPic();
								type = analysis(data);
							} while (type != angle[i]);
							sendDataPixel(ctr.outputStream);
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
		// pixelAnalysis coordinate [x][y]
		return (char) -1;
		/*
		 * int[][][] pixelAnalysis = { { { 14, 46 }, { 24, 90 }, { 76, 96 } }, //
		 * triangle lower left { { 50, 13 }, { 97, 22 }, { 101, 70 } }, // triangle
		 * upper left { { 149, 59 }, { 190, 62 }, { 185, 108 } }, // upper right { { 44,
		 * 131 }, { 85, 156 }, { 76, 200 } }, // lower left { { 136, 155 }, { 162, 201
		 * }, { 186, 195 } }, // triangle lower right { { 155, 130 }, { 223, 136 }, {
		 * 203, 177 } } }; // triangle upper right int collectWhite = 0; // check error
		 * for (int i = 0; i < 18; i++) { if (data[pixelAnalysis[i / 3][i %
		 * 3][1]][pixelAnalysis[i / 3][i % 3][0]] != data[pixelAnalysis[i / 3][i %
		 * 3][1]][pixelAnalysis[i / 3][i % 3][0] - 1] && data[pixelAnalysis[i / 3][i %
		 * 3][1]][pixelAnalysis[i / 3][i % 3][0]] != data[pixelAnalysis[i / 3][i %
		 * 3][1]][pixelAnalysis[i / 3][i % 3][0] + 1]) return (char) -1; } for (int i =
		 * 0; i < pixelAnalysis.length; i++) { collectWhite |=
		 * ((data[pixelAnalysis[i][0][1]][pixelAnalysis[i][0][0]] * 2 - 1 +
		 * data[pixelAnalysis[i][1][1]][pixelAnalysis[i][1][0]] * 2 - 1 +
		 * data[pixelAnalysis[i][2][1]][pixelAnalysis[i][2][0]] * 2 - 1) > 0 ? 1 : 0) <<
		 * i; } if (collectWhite == 0b000111) return 'T'; else if (collectWhite ==
		 * 0b111000) return 'B'; else if (collectWhite == 0b001011) return 'L'; else if
		 * (collectWhite == 0b110100) return 'R'; else if (collectWhite == 0b100110)
		 * return 'U'; else if (collectWhite == 0b011001) return 'D'; else return (char)
		 * -1;
		 */
	}

	public static void sendDataPixel(OutputStream out) throws IOException {
		//
		return;
	}

	public static boolean ready(InputStream inputStream, int index) throws IOException {
		if (index < "READY".length()) {
			if ("READY".charAt(index) == (char) read(inputStream)) {
				// System.out.println("READY".charAt(index));
				return ready(inputStream, ++index);
			} else {
				return false;
			}
		}
		return true;
	}

	public static int read(InputStream inputStream) throws IOException {
		int temp = (char) inputStream.read();
		if (temp == -1) {
			throw new IllegalStateException("Exit");
		}
		return temp;
	}
}
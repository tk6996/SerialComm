package com.epam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ConvertBlackWhite {

    public static void main(String[] args) {
        try {
            Convert("C:/datacom/pic/file8.bmp", "C:/datacom/pic/file8filter.bmp");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Convert(String inputFile, String outputFile) throws IOException {

        File input = new File(inputFile);
        BufferedImage image = ImageIO.read(input);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int black = 0x00000000;
                int white = 0xFFFFFFFF;
                image.setRGB(x, y, (0xFF & image.getRGB(x, y)) < 0x40 ? black : white);
            }
        }
        File output = new File(outputFile);
        ImageIO.write(image, "bmp", output);

    }

}
package Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Config.BotConfig.properties;

public class ImageProcessor {


    public static Map<String, BufferedImage> downloadedImages = new ConcurrentHashMap<>();

    public void loadImages() {
        File downloadDirectory = new File(properties.getProperty("DownloadPath"));
        File[] items = downloadDirectory.listFiles();
        for (File file : items) {
            File[] downloadedFiles = file.listFiles();
            //save downloaded files
            if (downloadedFiles != null) {
                for (File fl : downloadedFiles) {
                    if (fl.getPath().endsWith(".jpg")) {
                        try {
                            downloadedImages.put(fl.getPath(), ImageIO.read(new File(fl.getPath())));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public String compare(String imagePath) {
        BufferedImage imageA;

        // Read the first image
        try {
            imageA = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Error reading the first image: " + e.getMessage(), e);
        }

        // Compare the newly downloaded image with the previous ones
        for (Map.Entry<String, BufferedImage> entry : downloadedImages.entrySet()) {
            // Get dimensions for both images
            int width1 = imageA.getWidth();
            int width2 = entry.getValue().getWidth();
            int height1 = imageA.getHeight();
            int height2 = entry.getValue().getHeight();

            // Check if both images have the same dimensions (width AND height must be equal)
            if (width1 == width2 && height1 == height2) {

                long difference = 0;

                // Loop over all pixels (2D matrix) in both images
                for (int y = 0; y < height1; y++) {
                    for (int x = 0; x < width1; x++) {

                        // Get RGB values for each pixel in both images
                        int rgbA = imageA.getRGB(x, y);
                        int rgbB = entry.getValue().getRGB(x, y);

                        // Extract the red, green, and blue values from each pixel
                        int redA = (rgbA >> 16) & 0xff;
                        int greenA = (rgbA >> 8) & 0xff;
                        int blueA = (rgbA) & 0xff;
                        int redB = (rgbB >> 16) & 0xff;
                        int greenB = (rgbB >> 8) & 0xff;
                        int blueB = (rgbB) & 0xff;

                        // Calculate the absolute difference between color channels
                        difference += Math.abs(redA - redB);
                        difference += Math.abs(greenA - greenB);
                        difference += Math.abs(blueA - blueB);

                    }
                }

                // Calculate the total number of pixels (width * height * 3 color channels)
                double totalPixels = width1 * height1 * 3;

                // Calculate the average difference per pixel
                double avgDifference = difference / totalPixels;

                // Normalize the difference based on the 255 pixel color range
                double percentageDifference = (avgDifference / 255) * 100;

                // Print the difference percentage
                if (percentageDifference < 5) {  // You can adjust the threshold here
                    return "Aviso: se ha detectado una igualdad del " + (100 - percentageDifference) +
                            "% entre:\n" + imagePath + " y: \n" + entry.getKey();
                }
            }
        }

        //if there is no coincidence, add image to list for comparison
        try {
            downloadedImages.put(imagePath, ImageIO.read(new File(imagePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "noCoincidence";
    }
}

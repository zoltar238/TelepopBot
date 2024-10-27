package DAO;

import Model.ItemModel;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static Config.BotConfig.properties;

public class ItemDAOImp implements ItemDAO {

    @Override
    public void writeInfoFile(File file, String title, String description) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("titulo:" + title + "\n" + "descripcion:" + description + "\nstatus:sinSubir");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void modifyInfoFile(ItemModel itemModel) {
        StringBuilder modifiedInfo = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(itemModel.getInfoFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("status:")) {
                    modifiedInfo.append("status:subido");
                } else {
                    modifiedInfo.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        File tempFile = new File(itemModel.getInfoFile().getAbsolutePath() + "Temp");
        try (FileWriter fr = new FileWriter(tempFile)) {
            fr.write(modifiedInfo.toString());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        itemModel.renameFile(tempFile);
    }

    @Override
    public String[] readInfoFile(File file) {
        String title = "";
        StringBuilder description = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // return {tile, description}
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("titulo:")) {
                    title = line.substring(line.indexOf("titulo:") + 7);
                } else if (line.startsWith("descripcion:")) {
                    description.append(line.substring(line.indexOf("descripcion:") + 12).trim()).append("\n");
                } else if (!line.startsWith("status")) {
                    description.append(line.trim()).append("\n");
                }
            }
            return new String[]{title.trim(), description.toString().trim()};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readStatus(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("status:")) {
                    return line.substring(line.indexOf("status:") + 7);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "status not found";
    }

    @Override
// Download images
    public String downloadImage(String fileUrl, String localPath, int imageCounter) throws IOException {
        URL url = new URL(fileUrl);

        try (InputStream in = url.openStream(); OutputStream out = new FileOutputStream(localPath)) {

            byte[] buffer = new byte[2048];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            // Check if image downloaded correctly
            File downloadedFile = new File(localPath);
            if (downloadedFile.exists() && downloadedFile.length() > 0) {
                return "Imagen " + imageCounter + "descargada con éxito";
            } else {
                return "Error: La imagen " + imageCounter + "no se descargó correctamente";
            }

        } catch (IOException e) {
            return "Error al descargar la imagen " + imageCounter;
        }
    }

    @Override
    public Map<String, BufferedImage> loadImages() {
        Map<String, BufferedImage> downloadedImages = new ConcurrentHashMap<>();
        File downloadDirectory = new File(properties.getProperty("DownloadPath"));
        File[] items = downloadDirectory.listFiles();
        if (items != null) {
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
        return downloadedImages;
    }

    @Override
    public String compareImage(Map<String, BufferedImage> downloadedImages, String imagePath) {
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

    @Override
    public ImmutablePair<List<ItemModel>, List<ItemModel>> loadFiles(File downloadPath) {
        String[] items = downloadPath.list();
        List<ItemModel> uploadedItemModels = new CopyOnWriteArrayList<>();
        List<ItemModel> nonUploadedItemModels = new CopyOnWriteArrayList<>();
        //if item list is not empty check for uploaded items
        if (items != null) {
            for (String item : items) {
                String pathnameInfoFile = downloadPath.getAbsolutePath() + "\\" + item + "\\" + item + ".txt";
                String status = readStatus(new java.io.File(pathnameInfoFile));
                String[] files = Objects.requireNonNull(new java.io.File(downloadPath.getAbsolutePath() + "\\" + item).list());
                String[] uploadedImages = Arrays.copyOfRange(files, 1, files.length);
                //categorize into uploaded and nonUploaded
                if (status.equals("subido")) {
                    // get all files inside directory except for the first
                    // add absolute path
                    for (int i = 0; i < uploadedImages.length; i++) {
                        uploadedImages[i] = downloadPath.getAbsolutePath() + "\\" + item + "\\" + uploadedImages[i];
                    }
                    uploadedItemModels.add(new ItemModel(new java.io.File(pathnameInfoFile), new ArrayList<>(Arrays.asList(uploadedImages))));
                } else if (status.equals("sinSubir")) {
                    // get all files inside directory except for the first
                    // add absolute path
                    for (int i = 0; i < uploadedImages.length; i++) {
                        uploadedImages[i] = downloadPath.getAbsolutePath() + "\\" + item + "\\" + uploadedImages[i];
                    }
                    nonUploadedItemModels.add(new ItemModel(new java.io.File(pathnameInfoFile), new ArrayList<>(Arrays.asList(uploadedImages))));
                }
            }
        }
        return new ImmutablePair<>(uploadedItemModels, nonUploadedItemModels);
    }

    @Override
    public Boolean deleteDirectory(File directory) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) return false;
                }
            }
        }
        return directory.delete();
    }

}

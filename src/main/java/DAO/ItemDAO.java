package DAO;

import Model.ItemModel;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public interface ItemDAO {
    void writeInfoFile(File file, String title, String description);

    void modifyInfoFile(ItemModel itemModel);

    String[] readInfoFile(File file);

    String readStatus(File file);

    String downloadImage(String fileUrl, String localPath, int imageCounter) throws IOException;

    Map<String, BufferedImage> loadImages();

    String compareImage(Map<String, BufferedImage> downloadedImages, String imagePath);

    Pair<ArrayList<ItemModel>, ArrayList<ItemModel>> loadFiles(File downloadPath);

    Boolean deleteDirectory(File directory);
}

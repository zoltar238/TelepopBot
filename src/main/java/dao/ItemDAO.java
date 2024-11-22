package dao;

import model.ItemModel;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ItemDAO {
    void writeInfoFile(File file, String title, String description);

    void modifyInfoFile(ItemModel itemModel);

    String[] readInfoFile(File file);

    String readStatus(File file);

    String downloadImage(String fileUrl, String localPath, int imageCounter) throws IOException;

    Map<String, BufferedImage> loadImages();

    String compareImage(Map<String, BufferedImage> downloadedImages, String imagePath);

    ImmutablePair<List<ItemModel>, List<ItemModel>> loadFiles(File downloadPath);

    Boolean deleteDirectory(File directory);
}

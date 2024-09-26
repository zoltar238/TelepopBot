package DAO;

import Model.Item;

import java.io.File;

public interface ItemDAO {
    void writeInfoFile(File file, String title, String description);
    void modifyInfoFile(Item item);
    String[] readInfoFile(File file);
}

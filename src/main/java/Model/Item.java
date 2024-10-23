package Model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public class Item {
    private File infoFile;
    private ArrayList<String> paths = new ArrayList<>();

    public Item(File infoFile) {
        this.infoFile = infoFile;
    }

    public Item(File infoFile, ArrayList<String> paths) {
        this.infoFile = infoFile;
        this.paths = paths;
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public void renameFile(File tempFile) {
        this.infoFile.delete();
        tempFile.renameTo(this.infoFile);
    }
}
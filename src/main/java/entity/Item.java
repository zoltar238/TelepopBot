package entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;

@Getter
@Setter
@Data
public class Item {
    private File file;
    private ArrayList<String> paths = new ArrayList<>();

    public Item(File file){
        this.file = file;
    }

    public Item(File file, ArrayList<String> paths){
        this.file = file;
        this.paths = paths;
    }

    public void addPath(String path) {
        paths.add(path);
    }

    public void renameFile(File tempFile){
        this.file.delete();
        tempFile.renameTo(this.file);
    }

}
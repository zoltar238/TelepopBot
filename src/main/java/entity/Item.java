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

    public void addPath(String path) {
        paths.add(path);
    }
}
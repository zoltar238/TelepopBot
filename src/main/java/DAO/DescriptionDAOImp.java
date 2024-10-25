package DAO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DescriptionDAOImp implements DescriptionDAO{
    @Override
    public String getDescription(String path) {
        // extract description suffix from file
        try {
            // Read all lines from file
            List<String> lines = Files.readAllLines(Paths.get(path));

            // Write all lines to string
            return String.join("\n", lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

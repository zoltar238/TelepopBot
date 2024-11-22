package dao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DescriptionDAOImp implements DescriptionDAO {

    @Override
    public String getDescription(String path) {
        // Load resource as an input stream

        if (path.equals("src/main/resources/description.txt")) {
            try {
                // Read all lines from file
                List<String> lines = Files.readAllLines(Paths.get(path));

                // Write all lines to string
                return String.join("\n", lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource not found: " + path);
                }

                // Read all lines from the input stream
                List<String> lines = new BufferedReader(new InputStreamReader(inputStream))
                        .lines()
                        .collect(Collectors.toList());

                // Join all lines into a single string
                return String.join("\n", lines);
            } catch (IOException e) {
                throw new RuntimeException("Error reading description file", e);
            }
        }
    }
}


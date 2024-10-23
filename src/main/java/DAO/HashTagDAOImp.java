package DAO;

import Config.BotConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class HashTagDAOImp implements HashTagDAO {
    @Override
    public String[] extractHashTags() {
        try {
            // try to read file from jar
            InputStream in = BotConfig.class.getClassLoader().getResourceAsStream("hashtags.txt");
            List<String> lines;
            if (in != null) {
                //read all lines from jar
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    lines = reader.lines().collect(Collectors.toList());
                }
            } else {
                // read all lines
                lines = Files.readAllLines(Paths.get("src/main/resources/hashtags.txt"));
            }
            // convert to array
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

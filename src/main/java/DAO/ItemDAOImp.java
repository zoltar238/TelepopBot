package DAO;

import Model.Item;

import java.io.*;

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
    public void modifyInfoFile(Item item) {
        StringBuilder modifiedInfo = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(item.getInfoFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("status:")) {
                    modifiedInfo.append("status:subido");
                } else {
                    modifiedInfo.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File tempFile = new File(item.getInfoFile().getAbsolutePath() + "Temp");
        try (FileWriter fr = new FileWriter(tempFile)) {
            fr.write(modifiedInfo.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        item.renameFile(tempFile);
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
}

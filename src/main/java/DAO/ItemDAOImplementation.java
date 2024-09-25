package DAO;

import entity.Item;

import java.io.*;

public class ItemDAOImplementation implements ItemDAO{

    @Override
    public void writeInfoFile(File file, String title, String description) {
        try (FileWriter writer = new FileWriter(file)){
            writer.write(title + "\n" + description + "\nsinSubir");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void modifyInfoFile(Item item) {
        String modifiedInfo = "";
        try (BufferedReader br = new BufferedReader(new FileReader(item.getInfoFile()))){
            modifiedInfo = br.readLine() + "\n" + br.readLine() + "\n" + "subido";
        } catch (IOException e){
            e.printStackTrace();
        }
        File tempFile = new File(item.getInfoFile().getAbsolutePath() + "Temp");
        try (FileWriter fr = new FileWriter(tempFile)){
            fr.write(modifiedInfo);
        } catch (IOException e){
            e.printStackTrace();
        }

        item.renameFile(tempFile);
    }

    @Override
    public String[] readInfoFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            // return {tile, description}
            return new String[] {br.readLine(), br.readLine()};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

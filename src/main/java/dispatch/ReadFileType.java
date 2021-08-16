package dispatch;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

public class ReadFileType {
    private static final String[] fileTypes = {
            "src/main/resources/mime_type/application.txt",
            "src/main/resources/mime_type/audio.txt",
            "src/main/resources/mime_type/image.txt",
            "src/main/resources/mime_type/text.txt",
            "src/main/resources/mime_type/video.txt"
    };

    private static HashMap<String, String> allFileType;


    public static HashMap<String, String> getAllFileType() {
        if (Objects.equals(allFileType, null)) {
            readFile();
        }
        return allFileType;
    }

    private static void readFile() {
        allFileType= new HashMap<>();
        for (String fileType : fileTypes) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileType))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] temp = line.split(":");
                    allFileType.put(temp[0], temp[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryWord {
    String variable;
    Object data;

    public MemoryWord(String variable, Object data) {
        this.variable = variable;
        this.data = data;
    }


    public static void writeMemoryWordsToFile(List<MemoryWord> memoryWords, int processID) {
        List<String> lines = memoryWords.stream()
                .map(word -> processID + "," + word.variable + "," + word.data)
                .collect(Collectors.toList());
        try {
            Files.write(Paths.get("src/disk.txt"), lines, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<MemoryWord> readMemoryWordsFromFile(int processID) {
        List<MemoryWord> memoryWords = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get("src/disk.txt"), StandardCharsets.UTF_8);

            memoryWords = lines.stream()
                    .filter(line -> Integer.parseInt(line.split(",")[0]) == processID)
                    .map(line -> {
                        String[] parts = line.split(",");
                        return new MemoryWord(parts[1], parts[2]);
                    })
                    .collect(Collectors.toList());
            lines.removeIf(line -> Integer.parseInt(line.split(",")[0]) == processID);

            Files.write(Paths.get("src/disk.txt"), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return memoryWords;
    }


}

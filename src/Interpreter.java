import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Interpreter {

    public static List<String> readProgram(String address){
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(address));
        } catch (IOException e) {
            System.out.println("Error reading file at " + address);
            e.printStackTrace();
            return new ArrayList<>();
        }

        // Convert the List<String> to String[]
        return lines;
    }


}

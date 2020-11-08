import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static final String ALPHABET_DIR = "alphabet/";
    public static final String INPUT_DIR = "input/";
    public static final String JSON_PATH = "frequencies.json";

    public static int[][] readPictureAsArray(String path) throws IOException, URISyntaxException {
        BufferedImage bImage = ImageIO.read(new File(Utils.class.getResource(path).toURI()));
        int[][] picture = new int[bImage.getHeight()][bImage.getWidth()];
        for (int i = 0; i < bImage.getHeight(); i++) {
            for (int j = 0; j < bImage.getWidth(); j++) {
                Color color = new Color(bImage.getRGB(j, i));
                picture[i][j] = (color.getRed() + color.getBlue() + color.getGreen()) / (255 * 3);
            }
        }
        return picture;
    }

    public static void printArray(int[][] input) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                System.out.print("" + input[i][j]);
            }
            System.out.println("");
        }
    }

    public static List<String> readAllFilesInPath(String path) throws IOException {
        return IOUtils.readLines(
                Objects.requireNonNull(
                        Utils.class.getClassLoader().getResourceAsStream(path)
                ),
                Charsets.UTF_8
        );
    }

    public static String readJsonFromFileAsString(String path) throws URISyntaxException, IOException {
        byte[] fileBytes = FileUtils.readFileToByteArray(
                new File(Utils.class.getResource(path).toURI())
        );
        return new String(fileBytes, Charsets.UTF_8);
    }
}

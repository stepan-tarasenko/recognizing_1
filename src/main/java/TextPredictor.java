import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class TextPredictor {
    private Map<String, int[][]> alphabet = new Hashtable<>();
    private Map<int[][], Double> inputPictures = new Hashtable<>();


    public void main() throws IOException, URISyntaxException {
        initAlphabet();
        initInputArrays();
    }

    private void initAlphabet() throws IOException, URISyntaxException {
        List<String> files = Utils.readAllFilesInPath(Utils.ALPHABET_DIR);

        for (String fileName : files) {
            String nameWithoutExtension = fileName.split("\\.")[0];
            if (fileName.contains("space")) {
                alphabet.put(
                        " ",
                        Utils.readPictureAsArray(Utils.ALPHABET_DIR + fileName)
                );
            } else {
                alphabet.put(
                        nameWithoutExtension,
                        Utils.readPictureAsArray(Utils.ALPHABET_DIR + fileName)
                );
            }
        }
    }

    private void initInputArrays() throws URISyntaxException, IOException {
        List<String> files = Utils.readAllFilesInPath(Utils.INPUT_DIR);

        for (String fileName : files) {
            String probabilityNoise = fileName.split("_")[1];
            inputPictures.put(
                    Utils.readPictureAsArray(Utils.INPUT_DIR + fileName),
                    Double.parseDouble(probabilityNoise.substring(0, probabilityNoise.length() - 4))
            );
            System.out.println(fileName + " was initialized!");
        }
    }

    private double calculateProbability(int[][] noisedArray, int[][] etaloneArray, double noiseProbability) {
        int[][] tempArray = new int[noisedArray.length][noisedArray[0].length];
        for (int i = 0; i < noisedArray.length; i++) {
            for (int j = 0; j < noisedArray[i].length; j++) {
                tempArray[i][j] = (noisedArray[i][j] ^ etaloneArray[i][j]);
            }
        }
        double sum = 0.0;
        for (int i = 0; i < noisedArray.length; i++) {
            for (int j = 0; j < noisedArray[i].length; j++) {
                sum += tempArray[i][j];
            }
        }
        return Math.log(noiseProbability / (1 - noiseProbability)) * sum + Math.log(1 - noiseProbability)
                * noisedArray.length * noisedArray[0].length;
    }
}

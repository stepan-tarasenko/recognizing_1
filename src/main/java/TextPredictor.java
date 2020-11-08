import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class TextPredictor {
    private Map<String, int[][]> alphabet = new Hashtable<>();
    private Map<int[][], Double> inputPictures = new Hashtable<>();
    private Map<String, Double> bigramsProbability = new Hashtable<>();
    private ArrayList<Map<String, MetaData>> graph = new ArrayList<>();

    public void main() throws IOException, URISyntaxException {
        initAlphabet();
        initInputArrays();
        bigramsProbability = Collections.emptyMap();

        for (Map.Entry<int[][], Double> picture : inputPictures.entrySet()) {
            System.out.println("Picture length: " + picture.getKey()[0].length);
            System.out.println("Picture noise: " + picture.getValue());
            initGraph(picture.getKey(), picture.getValue());
            predictTextOnPicture(picture.getKey(), picture.getValue());
            printPredictedText(picture);
        }
    }

    private void printPredictedText(Map.Entry<int[][], Double> picture) {
        int lastIndex = picture.getKey()[0].length - 1;
        double maxProb = -Double.MAX_VALUE;
        for (Map.Entry<String, MetaData> entry : graph.get(lastIndex).entrySet()){
            if (entry.getValue().getProbability() > maxProb) {
                maxProb = entry.getValue().getProbability();
            }
        }
        for (Map.Entry<String, MetaData> entry : graph.get(lastIndex).entrySet()){
            if (entry.getValue().getProbability() == maxProb) {
                System.out.println(entry.getKey() + entry.getValue().getWord());
            }
        }
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

    private void initGraph(int[][] picture, double noiseProbability) {
        graph.clear();
        for (int i = 0; i < picture[0].length; i++) {
            graph.add(i, new Hashtable<>());
        }
        for (Map.Entry<String, int[][]> entry : alphabet.entrySet()) {
            int currentWidth = entry.getValue()[0].length;

            int[][] currentCharacter = new int[entry.getValue().length][];
            for (int j = 0; j < currentCharacter.length; j++) {
                currentCharacter[j] = Arrays.copyOfRange(picture[j], 0, currentWidth);
            }

            double probability = calculateProbability(currentCharacter, entry.getValue(), noiseProbability);

            if (!bigramsProbability.isEmpty()) {
                probability += Math.log(bigramsProbability.get(" " + entry.getKey()));
            }
            graph.get(currentWidth - 1).put(entry.getKey(), new MetaData("", probability));
        }
        System.out.println(graph.toString());
    }

    public void predictTextOnPicture(int[][] picture, double noiseProbability) {
        for (int i = 0; i < picture[0].length; i++) {
            for (Map.Entry<String, int[][]> letter : alphabet.entrySet()) {
                int currentWidth = letter.getValue()[0].length;
                if (i < currentWidth || graph.get(i - currentWidth).isEmpty()) {
                    continue;
                }
                double maxProb = -Double.MAX_VALUE;
                String bestSequence = "";
                for (Map.Entry<String, MetaData> node : graph.get(i - currentWidth).entrySet()) {
                    double prevProbability = node.getValue().getProbability();
                    String prevString = node.getValue().getWord();

                    int[][] currentCharacter = new int[letter.getValue().length][];
                    for (int j = 0; j < currentCharacter.length; j++) {
                        currentCharacter[j] = Arrays.copyOfRange(picture[j], i - currentWidth + 1, i + 1);
                    }

                    double currentProbability = calculateProbability(
                            currentCharacter,
                            letter.getValue(),
                            noiseProbability
                    );

                    double newProbability = currentProbability + prevProbability;

                    if (!bigramsProbability.isEmpty()) {
                        double pb = bigramsProbability.get(node.getKey() + letter.getKey());
                        newProbability += Math.log(pb);
                    }

                    if (newProbability > maxProb) {
                        maxProb = newProbability;
                        bestSequence = prevString + letter.getKey();
                    }

                    graph.get(i).put(letter.getKey(), new MetaData(bestSequence, maxProb));
                }
            }
        }
    }
}

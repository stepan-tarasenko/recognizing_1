import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class TextPredictor {
    private static Map<String, int[][]> alphabet = new Hashtable<>();
    private static Map<String, Map<int[][], Double>> inputPictures = new Hashtable<>();
    private static Map<String, Double> bigramsProbability = new Hashtable<>();
    private static ArrayList<Map<String, MetaData>> graph = new ArrayList<>();
    public static double epsilon = 1e-16;

    public void main() throws IOException, URISyntaxException {
        initAlphabet(alphabet);
        initInputArrays(inputPictures);
        initBigramsProbabilities(bigramsProbability, alphabet);

        for (Map.Entry<String, Map<int[][], Double>> picture : inputPictures.entrySet()) {
            for (Map.Entry<int[][], Double> pic : picture.getValue().entrySet()) {
                System.out.println("Picture length: " + pic.getKey()[0].length);
                System.out.println("Picture noise: " + pic.getValue());
                initGraph(pic.getKey(), pic.getValue(), graph, alphabet, bigramsProbability);
                predictTextOnPicture(pic.getKey(), pic.getValue(), graph, bigramsProbability, alphabet);
                System.out.println(getPredictedText(pic, graph));
            }
        }
    }

    public static void initBigramsProbabilities(Map<String, Double> bigramsProbability,
                                                Map<String, int[][]> alphabet
    ) throws URISyntaxException, IOException {
        JSONObject jsonObject = new JSONObject(Utils.readJsonFromFileAsString(Utils.JSON_PATH));
        int totalOccurrence = 0;
        for (String bigram : jsonObject.keySet()) {
            totalOccurrence += jsonObject.getInt(bigram);
        }

        for (String bigram : jsonObject.keySet()) {
            bigramsProbability.put(bigram, jsonObject.getDouble(bigram) / totalOccurrence);
        }

        for (String l1 : alphabet.keySet()) {
            for (String l2 : alphabet.keySet()) {
                if (!bigramsProbability.containsKey(l1.concat(l2))) {
                    bigramsProbability.put(l1.concat(l2), 0d);
                }
            }
        }
    }

    public static String getPredictedText(Map.Entry<int[][], Double> picture, ArrayList<Map<String, MetaData>> graph) {
        int lastIndex = picture.getKey()[0].length;
        double maxProb = -Double.MAX_VALUE;
        for (Map.Entry<String, MetaData> entry : graph.get(lastIndex).entrySet()){
            if (entry.getValue().getProbability() > maxProb) {
                maxProb = entry.getValue().getProbability();
            }
        }
        for (Map.Entry<String, MetaData> entry : graph.get(lastIndex).entrySet()){
            if (entry.getValue().getProbability() == maxProb) {
                return entry.getValue().getWord();
            }
        }
        return "";
    }

    public static void initAlphabet(Map<String, int[][]> alphabet) throws IOException, URISyntaxException {
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

    public static List<String> initInputArrays(Map<String, Map<int[][], Double>> input) throws URISyntaxException, IOException {
        List<String> files = Utils.readAllFilesInPath(Utils.INPUT_DIR);

        for (String fileName : files) {
            String probabilityNoise = fileName.split("_")[1];
            Map<int[][], Double> curMap = new Hashtable<>();
            curMap.put(
                    Utils.readPictureAsArray(Utils.INPUT_DIR + fileName),
                    Double.parseDouble(probabilityNoise.substring(0, probabilityNoise.length() - 4))
            );
            input.put(fileName, curMap);
            System.out.println(fileName + " was initialized!");
        }
        return files;
    }

    public static double calculateProbability(int[][] noisedArray, int[][] etaloneArray, double noiseProbability) {
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
        double res = Math.log(noiseProbability / (1 - noiseProbability)) * sum + Math.log(1 - noiseProbability)
                * noisedArray.length * noisedArray[0].length;

        if (noiseProbability == 0.0) {
            if (Double.isNaN(res)) {
                return Float.MAX_VALUE;
            } else if (Double.isInfinite(res)) {
                return -Float.MAX_VALUE;
            }
        } else if (noiseProbability == 1.0){
            res = Math.log(noiseProbability / (1 - noiseProbability + epsilon)) * sum + Math.log(1 - noiseProbability + epsilon)
                    * noisedArray.length * noisedArray[0].length;
        }
        return res;
    }

    public static void initGraph(int[][] picture,
                                 double noiseProbability,
                                 ArrayList<Map<String, MetaData>> graph,
                                 Map<String, int[][]> alphabet,
                                 Map<String, Double> bigramsProbability
                                 ) {
        graph.clear();
        for (int i = 0; i < picture[0].length + 1; i++) {
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
            graph.get(currentWidth).put(entry.getKey(), new MetaData(entry.getKey(), probability));
        }
        System.out.println(graph.toString());
    }

    public static void predictTextOnPicture(
            int[][] picture,
            double noiseProbability,
            ArrayList<Map<String, MetaData>> graph,
            Map<String, Double> bigramsProbability,
            Map<String, int[][]> alphabet
                                     ) {
        for (int i = 0; i < picture[0].length + 1; i++) {
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
                        currentCharacter[j] = Arrays.copyOfRange(picture[j], i - currentWidth, i);
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

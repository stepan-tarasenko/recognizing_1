import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class TestMain {
    private static Map<String, Map<int[][], Double>> inputPictures = new Hashtable<>();
    private static Map<String, int[][]> alphabet = new Hashtable<>();
    private static Map<String, Double> bigramsProbability = new Hashtable<>();
    private static ArrayList<Map<String, MetaData>> graph = new ArrayList<>();

    @Before
    public void init() throws IOException, URISyntaxException {
        TextPredictor.initAlphabet(alphabet);
        TextPredictor.initBigramsProbabilities(bigramsProbability, alphabet);
    }

    @Test
    public void zeroNoiseTest() {
        for (Map.Entry<String, Map<int[][], Double>> picture : inputPictures.entrySet()) {
            for (Map.Entry<int[][], Double> pic : picture.getValue().entrySet()) {
                if (pic.getValue() == 0) {
                    TextPredictor.initGraph(pic.getKey(), 0,  graph, alphabet, bigramsProbability);
                    TextPredictor.predictTextOnPicture(pic.getKey(), 0, graph, bigramsProbability, alphabet);
                    Assert.assertEquals(TextPredictor.getPredictedText(pic, graph), picture.getKey().split("_")[0]);
                }
            }
        }
    }

    @Test
    public void oneNoiseTest() {
        for (Map.Entry<String, Map<int[][], Double>> picture : inputPictures.entrySet()) {
            for (Map.Entry<int[][], Double> pic : picture.getValue().entrySet()) {
                if (pic.getValue() == 1) {
                    TextPredictor.initGraph(pic.getKey(), 1,  graph, alphabet, bigramsProbability);
                    TextPredictor.predictTextOnPicture(pic.getKey(), 1, graph, bigramsProbability, alphabet);
                    Assert.assertEquals(TextPredictor.getPredictedText(pic, graph), picture.getKey().split("_")[0]);
                }
            }
        }
    }
}

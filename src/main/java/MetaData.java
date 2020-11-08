public class MetaData {
    private String word;
    private double probability;

    public MetaData(String word, double probability) {
        this.word = word;
        this.probability = probability;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "word='" + word + '\'' +
                ", probability=" + probability +
                '}';
    }
}

package readability;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class TextData {
    private int syllables = 0;
    private int polysyllables = 0;
    private int chars = 0;
    private int words = 0;
    private int sentences = 0;
    private String input = "";
    private File file;
    private ArrayList<String> lines = new ArrayList<>();
    private Map<Integer, Integer> ageMapping = new HashMap<>();


    TextData(String fileName) {
        file = new File(fileName);
        ageMapping.put(1, 6);
        ageMapping.put(2, 7);
        ageMapping.put(3, 9);
        ageMapping.put(4, 10);
        ageMapping.put(5, 11);
        ageMapping.put(6, 12);
        ageMapping.put(7, 13);
        ageMapping.put(8, 14);
        ageMapping.put(9, 15);
        ageMapping.put(10, 16);
        ageMapping.put(11, 17);
        ageMapping.put(12, 18);
        ageMapping.put(13, 24);
        ageMapping.put(14, 24);
        ageMapping.put(15, 24);
        ageMapping.put(16, 24);
    }

    public void readData() {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.printf("error: %s", e.getMessage());
        }
    }

    void calculateData(TextData textData) {
        input = String.join(" ", lines);
        String[] parts = input.split("[\\.!\\?]\\s*");
        sentences = parts.length;
        chars = input.replaceAll("\\s", "").length();
        int wordSyllables;

        for (String part : parts) {
            String[] wordsInPart = part.split("\\s");

            for (String word : wordsInPart) {
                wordSyllables = word.replaceAll("e$", "t").replaceAll("[aeiouyY]{1,2}", "!").replaceAll("\\w|,", "").length();
                syllables += wordSyllables > 0 ? wordSyllables : 1;
                polysyllables += wordSyllables > 2 ? 1 : 0;
            }

            words += wordsInPart.length;
        }
    }

    public int getSyllables() {
        return syllables;
    }

    public int getPolysyllables() {
        return polysyllables;
    }

    public int getChars() {
        return chars;
    }

    public int getWords() {
        return words;
    }

    public int getSentences() {
        return sentences;
    }

    public String getInput() {
        return input;
    }

    public Map<Integer, Integer> getAgeMapping() {
        return ageMapping;
    }
}

class CheckScoreProcessor {

    void processData(String[] args) {
        TextData textData = new TextData(args[0]);
        textData.readData();
        textData.calculateData(textData);
        outputMainData(textData);
        calculateReadability(textData);
    }

    void calculateReadability(TextData textData) {
        System.out.printf("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): all%n%n");
        Algorithm ari, fk, smog, cl;

        switch (new Scanner(System.in).nextLine()) {
            case "ARI":
                ari = new ARI();
                ari.countScore(textData);
                System.out.printf("Automated Readability Index: %.2f (about %d year olds).%n", ari.score, ari.age);
                break;
            case "FK":
                fk = new FK();
                fk.countScore(textData);
                System.out.printf("Flesch–Kincaid readability tests: %.2f (about %d year olds).%n", fk.score, fk.age);
                break;
            case "SMOG":
                smog = new SMOG();
                smog.countScore(textData);
                System.out.printf("Simple Measure of Gobbledygook: %.2f (about %d year olds).%n", smog.score, smog.age);
                break;
            case "CL":
                cl = new CL();
                cl.countScore(textData);
                System.out.printf("Coleman–Liau index: %.2f (about %d year olds).%n", cl.score, cl.age);
                break;
            case "all":
                ari = new ARI();
                fk = new FK();
                smog = new SMOG();
                cl = new CL();

                ari.countScore(textData);
                fk.countScore(textData);
                smog.countScore(textData);
                cl.countScore(textData);

                System.out.printf("Automated Readability Index: %.2f (about %d year olds).%n", ari.score, ari.age);
                System.out.printf("Flesch–Kincaid readability tests: %.2f (about %d year olds).%n", fk.score, fk.age);
                System.out.printf("Simple Measure of Gobbledygook: %.2f (about %d year olds).%n", smog.score, smog.age);
                System.out.printf("Coleman–Liau index: %.2f (about %d year olds).%n%n", cl.score, cl.age);

                System.out.printf("This text should be understood in average by %.2f year olds.%n",
                        (ari.score + fk.score + smog.score + cl.score) / 4);
                break;
        }
    }

    void outputMainData(TextData textData) {
        System.out.printf("The text is:%n%s%n%nWords: %d%nSentences: %d%nCharacters: %d%nSyllables: %d%nPolysyllables: %d%n",
                textData.getInput(),
                textData.getWords(),
                textData.getSentences(),
                textData.getChars(),
                textData.getSyllables(),
                textData.getPolysyllables()
        );
    }
}

class Algorithm {
    protected double score;
    protected int age;

    void countScore(TextData textData) {};
}

class ARI extends Algorithm {
    void countScore(TextData t) {
        score = 4.71 * t.getChars() / t.getWords() + 0.5 * t.getWords() / t.getSentences() - 21.43;
        age = t.getAgeMapping().get((int) Math.ceil(score));
    }
}

class FK extends Algorithm {
    void countScore(TextData t) {
        score = 0.39 * t.getWords() / t.getSentences() + 11.8 * t.getSyllables() / t.getWords() - 15.59;
        age = t.getAgeMapping().get((int) Math.ceil(score));
    }
}

class SMOG extends Algorithm {
    void countScore(TextData t) {
        score = 1.043 * Math.sqrt(t.getPolysyllables() * 30 / t.getSentences()) + 3.1291;
        age = t.getAgeMapping().get((int) Math.ceil(score));
    }
}

class CL extends Algorithm {
    void countScore(TextData t) {
        score = 0.0588 * t.getChars() / t.getWords() * 100 - 0.296 * t.getSentences() / t.getWords() * 100 - 15.8;
        age = t.getAgeMapping().get((int) Math.ceil(score));
    }
}

public class Main {
    public static void main(String[] args) {
        CheckScoreProcessor processor = new CheckScoreProcessor();
        processor.processData(args);
    }
}

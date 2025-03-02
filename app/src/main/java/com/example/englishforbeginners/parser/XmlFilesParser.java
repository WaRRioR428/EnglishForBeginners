package com.example.englishforbeginners.parser;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.example.englishforbeginners.entity.ConstructSentenceTask;
import com.example.englishforbeginners.entity.GrammarTest;
import com.example.englishforbeginners.entity.MissingWordSentenceTask;
import com.example.englishforbeginners.entity.WrongAnswer;
import com.example.englishforbeginners.entity.Word;
import com.example.englishforbeginners.entity.WordTest;

public class XmlFilesParser {

    public List<GrammarTest> parseGrammarTestsFromXml(InputStream is) {
        List<GrammarTest> grammarTests = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Test");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nNode;
                    String theme = element.getElementsByTagName("theme").item(0).getTextContent();
                    int testId = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
                    GrammarTest grammarTest = new GrammarTest(theme, testId, 0);

                    NodeList constructSentenceTasks = element.getElementsByTagName("ConstructTask");
                    List<ConstructSentenceTask> constructSentenceTaskList = new ArrayList<>();

                    if (Objects.nonNull(constructSentenceTasks)) {
                        for (int j = 0; j < constructSentenceTasks.getLength(); j++) {
                            Node constructSentenceTaskNode = constructSentenceTasks.item(j);

                            if (constructSentenceTaskNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element single = (Element) constructSentenceTaskNode;

                                String correctSentence = single.getElementsByTagName("sentence").item(0).getTextContent();
                                String translation = single.getElementsByTagName("translation").item(0).getTextContent();
                                String wrongSentence = single.getElementsByTagName("errorSentence").item(0).getTextContent();

                                ConstructSentenceTask constructSentenceTask = new ConstructSentenceTask(correctSentence, translation, wrongSentence);
                                constructSentenceTaskList.add(constructSentenceTask);
                            }
                        }

                    }
                    grammarTest.setConstructSentenceTasks(constructSentenceTaskList);

                    NodeList missingSentenceTasks = element.getElementsByTagName("MissingTask");
                    List<MissingWordSentenceTask> missingTaskList = new ArrayList<>();

                    if (Objects.nonNull(missingSentenceTasks)) {
                        for (int j = 0; j < missingSentenceTasks.getLength(); j++) {
                            Node missingTaskNode = missingSentenceTasks.item(j);

                            if (missingTaskNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element single = (Element) missingTaskNode;

                                String sentence = single.getElementsByTagName("sentence").item(0).getTextContent();
                                String translation = single.getElementsByTagName("translation").item(0).getTextContent();
                                String correctAnswer = single.getElementsByTagName("answer").item(0).getTextContent();
                                int id = Integer.parseInt(single.getElementsByTagName("id").item(0).getTextContent());
                                MissingWordSentenceTask missingTask = new MissingWordSentenceTask(sentence, id, translation, correctAnswer);

                                NodeList wrongAnswersList = single.getElementsByTagName("wrongAnswers");
                                List<WrongAnswer> missingTaskWrongAnswerList = new ArrayList<>();

                                for (int k = 0; k < wrongAnswersList.getLength(); k++) {
                                    Node wrongAnswerNode = wrongAnswersList.item(k);

                                    if (wrongAnswerNode.getNodeType() == Node.ELEMENT_NODE) {
                                        Element wrongAnswerElement = (Element) wrongAnswerNode;
                                        NodeList wrongAnswerElementList = wrongAnswerElement.getElementsByTagName("answer");

                                        for (int l = 0; l < wrongAnswerElementList.getLength(); l++) {
                                            final Node wrongAnswerElementNode = wrongAnswerElementList.item(l);

                                            if (wrongAnswerElementNode.getNodeType() == Node.ELEMENT_NODE) {
                                                Element wrongAnswerElementNodeElement = (Element) wrongAnswerElementNode;
                                                WrongAnswer wrongAnswer = new WrongAnswer(wrongAnswerElementNodeElement.getTextContent());
                                                missingTaskWrongAnswerList.add(wrongAnswer);
                                            }
                                        }
                                    }
                                }

                                missingTask.setWrongAnswers(missingTaskWrongAnswerList);
                                missingTaskList.add(missingTask);
                            }
                        }

                    }
                    grammarTest.setMissingWordTasks(missingTaskList);
                    grammarTests.add(grammarTest);
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace(System.out);
        }

        return grammarTests;
    }

    public List<WordTest> parseWordTestsFromXml(InputStream is) {
        List<WordTest> wordTests = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Test");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    int id = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
                    String theme = element.getElementsByTagName("theme").item(0).getTextContent();
                    WordTest wordTest = new WordTest(id, theme, 0);

                    NodeList words = element.getElementsByTagName("WordTask");
                    List<Word> wordsList = new ArrayList<>();
                    for (int j = 0; j < words.getLength(); j++) {
                        Node wordNode = words.item(j);
                        if (wordNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element single = (Element) wordNode;

                            int wordId = Integer.parseInt(single.getElementsByTagName("id").item(0).getTextContent());
                            String engWord = single.getElementsByTagName("word").item(0).getTextContent();
                            String translation = single.getElementsByTagName("translation").item(0).getTextContent();
                            String transcription = single.getElementsByTagName("transcription").item(0).getTextContent();
                            Word word = new Word(wordId, engWord, transcription, translation);

                            wordsList.add(word);
                        }
                    }

                    wordTest.setWords(wordsList);
                    wordTests.add(wordTest);
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace(System.out);
        }

        return wordTests;
    }

    public List<Word> parseGameWordsFromXml(InputStream is) {
        List<Word> gameWords = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("GameWord");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    int id = Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
                    String engWord = element.getElementsByTagName("word").item(0).getTextContent();
                    String transcription = element.getElementsByTagName("transcription").item(0).getTextContent();
                    Word word = new Word(id, engWord, transcription, "");
                    gameWords.add(word);
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace(System.out);
        }

        return gameWords;
    }
}

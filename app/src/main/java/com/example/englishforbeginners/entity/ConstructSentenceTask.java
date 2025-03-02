package com.example.englishforbeginners.entity;

import java.io.Serializable;

public record ConstructSentenceTask(String correctSentence, String translation,
                                    String wrongSentence) implements Serializable {
}

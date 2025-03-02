package com.example.englishforbeginners.entity;

import java.io.Serializable;

public record Word(int id, String word, String transcription,
                   String translation) implements Serializable {

}

package com.example.itpassportapp.Question;

import java.time.LocalDateTime;

public class QuestionProgress {

    private long id;
    private long userId;
    private String field;
    private int nextIndex;
    private LocalDateTime updetedAt;

    public QuestionProgress(String field,int nextIndex){
        this.field = field;
        this.nextIndex= nextIndex;
    }

    public long getId(){return id;}
    public long getUserId(){return userId;}
    public String getField(){return field;}
    public int getNextIndex(){return nextIndex;}

    public LocalDateTime getUpdetedAt() {
        return updetedAt;
    }
}

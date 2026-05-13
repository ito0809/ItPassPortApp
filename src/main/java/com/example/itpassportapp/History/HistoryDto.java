package com.example.itpassportapp.History;

import java.time.LocalDateTime;

//最近の履歴用
public class HistoryDto {

    private int questionId;
    private LocalDateTime answerAt;
    private String field;
    private boolean isCorrect;

    public HistoryDto(int questionId,LocalDateTime answerAt, String field, boolean isCorrect){

        this.questionId = questionId;
        this.answerAt = answerAt;
        this.field = field;
        this.isCorrect = isCorrect;
    }

    public int getQuestionId(){
        return questionId;
    }

    public LocalDateTime getAnswerAt(){
        return answerAt;
    }

    public String getField(){
        return field;
    }

    public boolean isCorrect(){
        return isCorrect;
    }

}

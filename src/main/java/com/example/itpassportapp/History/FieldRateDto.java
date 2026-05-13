package com.example.itpassportapp.History;

//分野別正解率
public class FieldRateDto{

    private String field;
    private int total;
    private int correct;
    private double correctRate; //正解率

    public FieldRateDto(String field, int total, int correct,double correctRate){

        this.field = field;
        this.total= total;
        this.correct= correct;
        this.correctRate = correctRate;

    }
    public String getField(){
        return field;
    }
    public int getTotal(){
        return total;
    }
    public int getCorrect(){
        return correct;
    }
    public double getCorrectRate(){
        return correctRate;
    }
}


package com.example.itpassportapp.History;

//昨日、今日の学習履歴
public class TodayResultDto {

    private int total;
    private int correct;
    private double rate;

    public TodayResultDto(int total, int correct, double rate){
        this.total = total;
        this.correct = correct;
        this.rate = rate;
    }

    public int getTotal() {
        return total;
    }

    public int getCorrect() {
        return correct;
    }

    public double getRate() {
        return rate;
    }
}

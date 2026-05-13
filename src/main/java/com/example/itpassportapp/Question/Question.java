package com.example.itpassportapp.Question;

public class Question {

    private  int id;
    private String field; //分野
    private String questionText; //問題文
    private String choice1; //選択肢
    private String choice2;
    private String choice3;
    private String choice4;
    private int correctAnswer; //正解番号
    private  String explanation; //解説よう文

    private String realExamExample; //実際の試験テキスト



    public Question(int id,String field,String questionText,String choice1,String choice2,
                    String choice3,String choice4,
                    int correctAnswer ,String explanation, String realExamExample){
        this.id=id;
        this.field= field;
        this.questionText= questionText;
        this.choice1=choice1;
        this.choice2=choice2;
        this.choice3=choice3;
        this.choice4=choice4;
        this.correctAnswer=correctAnswer;
        this.explanation= explanation;
        this.realExamExample = realExamExample;

    }

    public int getId(){
            return id;
    }
    public String getField(){
        return field;
    }
    public String getQuestionText(){
        return questionText;
    }
    public String getChoice1(){
        return choice1;
    }
    public String getChoice2(){
        return choice2;
    }
    public String getChoice3(){
        return choice3;
    }
    public String getChoice4(){
        return choice4;
    }
    public int getCorrectAnswer(){
        return correctAnswer;
    }
    public String getExplanation(){
        return  explanation;
    }
    public String getRealExamExample(){
        return realExamExample;
    }

}

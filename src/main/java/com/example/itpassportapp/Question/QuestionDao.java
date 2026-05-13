package com.example.itpassportapp.Question;

import com.example.itpassportapp.History.FieldRateDto;
import com.example.itpassportapp.History.HistoryDto;
import com.example.itpassportapp.History.TodayResultDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestionDao {

    //sqlを簡単に実行できる道具クラス
    private final JdbcTemplate jdbcTemplate;

    //コンストラクタ　JdbcTemplateが使えるように登録
    public QuestionDao(JdbcTemplate jdbcTemplate){

        this.jdbcTemplate= jdbcTemplate;
    }

    //問題を一覧取得
    public List<Question> findAll(){
        String sql = """
                    SELECT id, field, question_text, choice1, choice2,
                           choice3, choice4, correct_answer,
                           explanation, real_exam_example
                    FROM questions
                    ORDER BY
                      CASE field
                        WHEN 'strategy' THEN 1
                        WHEN 'management' THEN 2
                        WHEN 'technology' THEN 3
                      END,
                      id
                    """;
        return jdbcTemplate.query(sql,(rs, rowNum) -> new Question(
                rs.getInt("id"),
                rs.getString("field"),
                rs.getString("question_text"),
                rs.getString("choice1"),
                rs.getString("choice2"),
                rs.getString("choice3"),
                rs.getString("choice4"),
                rs.getInt("correct_answer"),
                rs.getString("explanation"),
                rs.getString("real_exam_example")
        ));
    }
    //idを引数に一件ずつ問題を取得用
    public Question findById(int id){
        System.out.println("id引数DAO IN");
        String sql = """
                SELECT id,field,question_text,choice1,choice2,choice3,choice4,
                correct_answer,explanation,real_exam_example 
                FROM questions
                WHERE id =?
                """;
        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new Question(
                        rs.getInt("id"),
                        rs.getString("field"),
                        rs.getString("question_text"),
                        rs.getString("choice1"),
                        rs.getString("choice2"),
                        rs.getString("choice3"),
                        rs.getString("choice4"),
                        rs.getInt("correct_answer"),
                        rs.getString("explanation"),
                        rs.getString("real_exam_example")
                ),id
        );
    }

    //分野ごとの一覧取得用
    public List<Question> findByField(String field){
        String sql= """
                SELECT field, question_text, choice1, choice2,
                        choice3, choice4, correct_answer,
                        explanation, real_exam_example
                        FROM questions
                        WHERE field=?
                        ORDER BY id
                """;
        System.out.println("DB接続成功");
        return jdbcTemplate.query(sql,(ResultSet rs, int rowNum) ->new Question(
                rs.getInt("id"),
                rs.getString("field"),
                rs.getString("question_text"),
                rs.getString("choice1"),
                rs.getString("choice2"),
                rs.getString("choice3"),
                rs.getString("choice4"),
                rs.getInt("correct_answer"),
                rs.getString("explanation"),
                rs.getString("real_exam_example")
        ), field);
    }

    //問題追加用
    public int questionAdd(String field,
                           String questionText,
                           String choice1,
                           String choice2,
                           String choice3,
                           String choice4,
                           int correctAnswer,
                           String explanation,
                           String realExamExample) {
        String sql = """
                INSERT INTO questions(
                field, question_text, choice1, choice2,
                        choice3, choice4, correct_answer,
                        explanation, real_exam_example)
                        VALUES(?,?,?,?,?,?,?,?,?)
                """;
        return jdbcTemplate.update(sql,
                field,
                questionText,
                choice1,
                choice2,
                choice3,
                choice4,
                correctAnswer,
                explanation,
                realExamExample);
    }

    //直前にINSERTしたIDを取得
    public int findLastInsertId() {
        String sql = "SELECT LAST_INSERT_ID()";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class);
        return (id == null) ? 0 : id;
    }


    //問題数取得
    public int countByField(String field) {
        String sql = "SELECT COUNT(*) FROM questions WHERE field = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, field);
        return (count == null) ? 0 : count;
    }

    //一問ずつ取得　問題を出題するときに使用しているメソッド
    public Optional<Question> findOneByFindAndIndex(String field, int index) {

        String sql = """
    SELECT id, field, question_text, choice1, choice2,
           choice3, choice4, correct_answer,
           explanation, real_exam_example
    FROM questions
    WHERE field = ?
    ORDER BY id
    LIMIT 1 OFFSET ?
    """;

        List<Question> list = jdbcTemplate.query(sql, (rs, rowNum) -> new Question(
                rs.getInt("id"),
                rs.getString("field"),
                rs.getString("question_text"),
                rs.getString("choice1"),
                rs.getString("choice2"),
                rs.getString("choice3"),
                rs.getString("choice4"),
                rs.getInt("correct_answer"),
                rs.getString("explanation"),
                rs.getString("real_exam_example")
        ), field, index);

        return list.stream().findFirst(); // ← 0件なら Optional.empty()
    }

    //削除用
    public void questionDelete(int id){
        System.out.println("DB接続完了・削除メソッドIN");
        String sql= """
                DELETE FROM questions
                WHERE id =?
                """;
        jdbcTemplate.update(sql,id);
        System.out.println("DB接続完了・削除メソッドOUT");
    }

    //更新用
    public int questionUpdate(Question question){
        System.out.println("更新用メソッドIN");

        String sql= """
                UPDATE questions
                SET field =?,
                question_text =?,
                choice1=?,
                choice2=?,
               choice3=?, choice4=?, correct_answer=?,
               explanation=?, real_exam_example=?
               WHERE id =?
               """;

        return jdbcTemplate.update(sql,
                question.getField(),
                question.getQuestionText(),
                question.getChoice1(),
                question.getChoice2(),
                question.getChoice3(),
                question.getChoice4(),
                question.getCorrectAnswer(),
                question.getExplanation(),
                question.getRealExamExample(),
                question.getId()
        );
    }

    //検索用
    public List<Question> questionSearch(String field, String keyword){

        String sql = "SELECT * FROM questions WHERE 1=1 ";
        List<Object> params = new ArrayList<>();

        if(field != null && !field.isEmpty()){
            sql += " AND field = ?";
            params.add(field);
        }

        if(keyword != null && !keyword.isEmpty()){
            sql += " AND question_text LIKE ?";
            params.add("%" + keyword + "%");
        }
        System.out.println("sql=" + sql); // DAO内で
        System.out.println("params=" + params); // DAO内で

        return jdbcTemplate.query(sql,
                (rs,rowNum)-> new Question(
                        rs.getInt("id"),
                        rs.getString("field"),
                        rs.getString("question_text"),
                        rs.getString("choice1"),
                        rs.getString("choice2"),
                        rs.getString("choice3"),
                        rs.getString("choice4"),
                        rs.getInt("correct_answer"),
                        rs.getString("explanation"),
                        rs.getString("real_exam_example")
                ),
                params.toArray()
        );
    }

    //ユーザーが答えた選択肢を保存
    public void userAnswer(int userid,
                                      int questionId,
                                      int selectedNubmer,
                                      boolean isCorrect){
        System.out.println("userAnswer IN");

        String sql= """
                INSERT INTO user_questions(
                user_id,
                question_id,
                selected_number,
                is_correct)
                VALUES(?,?,?,?)
                """;

        jdbcTemplate.update(sql,
                userid,
                questionId,
                selectedNubmer,
                isCorrect);
    }

    //間違えた問題のみを取り出す
    public List<Question> wrongList(long userId) {

        String sql = """
                    SELECT q.*
                    FROM questions q
                    JOIN user_questions uq
                    ON q.id = uq.question_id
                    WHERE uq.user_id = ?
                    AND uq.answered_at = (
                        SELECT MAX(answered_at)
                        FROM user_questions
                        WHERE user_id = ?
                        AND question_id = q.id)
                    AND uq.is_correct = false
                    """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Question(
                        rs.getInt("id"),
                        rs.getString("field"),
                        rs.getString("question_text"),
                        rs.getString("choice1"),
                        rs.getString("choice2"),
                        rs.getString("choice3"),
                        rs.getString("choice4"),
                        rs.getInt("correct_answer"),
                        rs.getString("explanation"),
                        rs.getString("real_exam_example")
                ),userId,userId
        );
    }

    //昨日の学習数
    public TodayResultDto yesterdayHistory(long userId){

        String sql = """
        SELECT
        COUNT(*) AS total,
        COALESCE(SUM(is_correct),0) AS correct
        FROM user_questions
        WHERE user_id = ?
        AND DATE(answered_at) = CURDATE() - INTERVAL 1 DAY
        """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs,rowNum)->{

                    int total = rs.getInt("total");
                    int correct = rs.getInt("correct");

                    double rate = total == 0 ? 0 : (double) correct / total * 100;

                    return new TodayResultDto(total, correct, rate);
                },
                userId
        );
    }

    //今日の学習結果
    public TodayResultDto todayHistory(long userId){

        String sql = """
        SELECT
        COUNT(*) AS total,
        COALESCE(SUM(is_correct),0) AS correct
        FROM user_questions
        WHERE user_id = ?
        AND DATE(answered_at) = CURDATE()
        """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs,rowNum)->{

                    int total = rs.getInt("total");
                    int correct = rs.getInt("correct");

                    double rate = total == 0 ? 0 : (double) correct / total * 100;

                    return new TodayResultDto(total, correct, rate);
                },
                userId
        );
    }

    //分野別正解率
    public List<FieldRateDto> fieldRate(long userId){

        String sql = """
        SELECT
        q.field,
        COUNT(*) AS total,
        SUM(uq.is_correct) AS correct
        FROM user_questions uq
        JOIN questions q
        ON uq.question_id = q.id
        WHERE uq.user_id = ?
        GROUP BY q.field
        """;

        return jdbcTemplate.query(
                sql,
                (rs,rowNum) -> {

                    int total = rs.getInt("total");
                    int correct = rs.getInt("correct");

                    double correctRate = 0;

                    if(total != 0){
                        correctRate = (double) correct / total * 100;
                    }

                    return new FieldRateDto(
                            rs.getString("field"),
                            total,
                            correct,
                            correctRate
                    );
                },
                userId
        );
    }

    //最近の学習結果
    public List<HistoryDto> historyDao(long userId){

        String sql = """
        SELECT
        uq.question_id,
        uq.answered_at,
        q.field,
        uq.is_correct
        FROM user_questions uq
        JOIN questions q
        ON uq.question_id = q.id
        WHERE uq.user_id = ?
        ORDER BY uq.answered_at DESC
        LIMIT 10
        """;

        return jdbcTemplate.query(
                sql,
                (rs,rowNum)->{

                    int questionId = rs.getInt("question_id");
                    LocalDateTime answerAt =
                            rs.getTimestamp("answered_at").toLocalDateTime();
                    String field = rs.getString("field");
                    boolean correct = rs.getBoolean("is_correct");

                    return new HistoryDto(
                            questionId,
                            answerAt,
                            field,
                            correct
                    );
                },
                userId
        );
    }

    //今の進捗状態を保存
    public void saveProgress(int userId,String field,int next_index){

        String sql = """
                    INSERT INTO progress (
                        user_id,
                        field,
                        next_index
                    )
                    VALUES (
                        ?,?,?
                    )
                    ON DUPLICATE KEY UPDATE
                        next_index = VALUES(next_index),
                        updated_at = CURRENT_TIMESTAMP
                    """;

        jdbcTemplate.update(sql,
                userId,
                field,
                next_index);
    }

    //進捗を取り出す
    public QuestionProgress showProgress(long userId, String field) {
        String sql = """
            SELECT field, next_index
            FROM progress
            WHERE user_id = ?
              AND field = ?
            """;

        List<QuestionProgress> list = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new QuestionProgress(
                        rs.getString("field"),
                        rs.getInt("next_index")
                ),
                userId, field
        );

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}


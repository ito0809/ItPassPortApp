package com.example.itpassportapp.Controller;

import com.example.itpassportapp.Question.Question;
import com.example.itpassportapp.Question.QuestionDao;
import com.example.itpassportapp.user.User;
import com.example.itpassportapp.user.UserDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

//管理者画面
@Controller
public class AdminController {

    //UserDaoをこのControllerで使用するため保持する（依存性)
    private final UserDao userDao;

    private QuestionDao questionDao;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //Springの依存性注入によりUserDaoを受け取るコンストラクタ
    public AdminController(QuestionDao questionDao,UserDao userDao) {
        this.questionDao = questionDao;
        this.userDao = userDao;
    }

    //管理者ページ表示
    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {

        User user = checkAdmin(session);

        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "admin/admin";
    }

    //問題管理ページ表示用
    @GetMapping("/question_control_page")
    public String question_control_page(@RequestParam(required = false)String field,
                                        @RequestParam (required= false) String keyword,
                                        HttpSession session,
                                        Model model) {
        //管理者かチェック
        User user = checkAdmin(session);
        if (user == null) {
            return "redirect:/login";
        }
        //DBから取得
        List<Question> questions;

        if ((field != null && !field.isEmpty()) ||
                (keyword != null && !keyword.isEmpty())) {

            questions = questionDao.questionSearch(field, keyword);

            System.out.println("field=" + field);
            System.out.println("keyword=" + keyword);

            model.addAttribute("questions", questions);
            model.addAttribute("field", field);
            model.addAttribute("keyword", keyword);

            return "admin/questionSearchResult";

        }
        questions = questionDao.findAll();

        model.addAttribute("questions", questions);
        model.addAttribute("field", field);
        model.addAttribute("keyword", keyword);

        return "admin/question_control_page";//問題一覧ページに

    }

    //問題を追加画面用の表示するための画面
    @GetMapping("/question_add")
    public String questionAdd(HttpSession session, Model model){

        User user = checkAdmin(session);

        if(user == null){
            return "redirect:/login";
        }

        return "admin/question_add";
    }

    //問題追加用
    @PostMapping("/question_add")
    public String questionAdd(HttpSession session,
                              @RequestParam List<String> field,
                              @RequestParam List<String> questionText,
                              @RequestParam List<String> choice1,
                              @RequestParam List<String> choice2,
                              @RequestParam List<String> choice3,
                              @RequestParam List<String> choice4,
                              @RequestParam List<Integer> correctAnswer,
                              @RequestParam List<String> explanation,
                              @RequestParam(required = false) List<String> realExamExample,
                              RedirectAttributes ra){

        User user = checkAdmin(session);
        if(user == null){
            return "redirect:/login";
        }

        int successCount = 0;

        List<Question> newQuestions = new ArrayList<>();

        for(int i = 0; i < questionText.size(); i++){

            String realExam = "";

            if(realExamExample != null && realExamExample.size() > i){
                realExam = realExamExample.get(i);
            }

            // 問題文が空なら登録しない
            if(questionText.get(i) == null || questionText.get(i).trim().isEmpty()){
                continue;
            }

            int rows = questionDao.questionAdd(
                    field.get(i),
                    questionText.get(i),
                    choice1.get(i),
                    choice2.get(i),
                    choice3.get(i),
                    choice4.get(i),
                    correctAnswer.get(i),
                    explanation.get(i),
                    realExam
            );

            if(rows == 1){
                successCount++;

                // 登録した問題のIDを取得
                int newId = questionDao.findLastInsertId();

                // Questionオブジェクトを作ってListに追加
                Question q = new Question(
                        newId,
                        field.get(i),
                        questionText.get(i),
                        choice1.get(i),
                        choice2.get(i),
                        choice3.get(i),
                        choice4.get(i),
                        correctAnswer.get(i),
                        explanation.get(i),
                        realExam
                );

                newQuestions.add(q);
            }
        }

        if(successCount > 0){
            ra.addFlashAttribute("success", successCount + "問登録しました！");
            ra.addFlashAttribute("newQuestions", newQuestions);
            return "redirect:/questionResult";
        }else{
            ra.addFlashAttribute("error", "登録に失敗しました");
            return "redirect:/question_add";
        }
    }

    //問題の編集画面へのリンク
    @GetMapping("/questionUpdate")
    public String questionUpdate(@RequestParam int id,
                                 HttpSession session,
                                 Model model){

        System.out.println("更新getコントロールメソッドIN");

        User user =checkAdmin(session);
        if(user ==null){
            return "redirect:/login";
        }
        Question question = questionDao.findById(id);

        model.addAttribute("question",question);

        return "/admin/questionUpdate";

    }

    // 問題を更新するための画面用ページ
    @PostMapping("/questionUpdate")
    public String questionUpdate(Question question,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        User user = checkAdmin(session);
        if (user == null) {
            return "redirect:/login";
        }

        int rows = questionDao.questionUpdate(question);

        if (rows == 1) {
            List<Question> newQuestions = new ArrayList<>();
            newQuestions.add(question);

            ra.addFlashAttribute("success", "問題を更新しました！");
            ra.addFlashAttribute("newQuestions", newQuestions);

            return "redirect:/questionResult";

        } else {
            ra.addFlashAttribute("error", "データがありません。");
            return "redirect:/questionResult";
        }
    }

    //問題追加・更新結果画面用
    @GetMapping("/questionResult")
    public String questionUpdateResult(HttpSession session){

        User user =checkAdmin(session);
        if(user == null){
            return "redirect:/login";
        }
        return "/admin/questionResult";
    }

    //問題削除
    @PostMapping("/questionDelete")
    public String questionDelete(@RequestParam int id,
                                 HttpSession session){
        System.out.println("削除用コントロールメソッドIN");

        User user =checkAdmin(session);
        if(user ==null){
            return "redirect:/login";
        }

        questionDao.questionDelete(id);
        System.out.println("DB接続・削除完了");

        return "redirect:/question_control_page";
    }

    //管理者登録表示用
    @GetMapping("admin_add")
        public String adminAdd(HttpSession session) {

        User user = checkAdmin(session);
        if (user == null) {
            return "redirect:/login";
        }
        return "/admin/admin_add";
    }

    //管理者登録処理
    @PostMapping("admin_add")
    public String createAdmin(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              HttpSession session,
                              Model model
                              ) {
        User user = checkAdmin(session);
        if(user == null){
            return "redirect:/login";
        }

        if(!password.equals(confirmPassword)){
            model.addAttribute("error","パスワードが一致しません");
            return "/admin/admin_add";
        }
        //すでに登録されているかチェック
        User existing = userDao.findByEmail(email);

        if(existing != null) {
            model.addAttribute("error","このメールアドレスは既に登録されています");
            return "/admin/admin_add";
        }
        //パスワードをハッシュ化
        String passwordHash = encoder.encode(password);

        //DBに登録処理
        userDao.insertAdmin(email,passwordHash,name);


        model.addAttribute("user","登録に成功しました");
        return "/admin/admin_add";

    }

    //sessionから管理者か判断するメソッド
    private User checkAdmin(HttpSession session) {
        String email = (String) session.getAttribute("loginUser");
        if (email == null) return null;

        User user = userDao.findByEmail(email);
        if (user == null) {
            session.invalidate();
            return null;
        }

        // ADMIN以外は弾く
        if (!"ADMIN".equals(user.getRole())) {
            return null;
        }
        return user;
    }
    private String normalizeField(String field){
        if(field == null) return null;
        field = field.trim();

        return switch (field) {
            case "ストラテジ" -> "strategy";
            case "マネジメント" -> "management";
            case "テクノロジ" -> "technology";
            default -> field; // すでに英語ならそのまま
        };
    }
}

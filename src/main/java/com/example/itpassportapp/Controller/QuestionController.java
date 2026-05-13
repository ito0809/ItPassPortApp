package com.example.itpassportapp.Controller;

import com.example.itpassportapp.Question.Question;
import com.example.itpassportapp.Question.QuestionDao;
import com.example.itpassportapp.Question.QuestionProgress;
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

import java.util.List;
import java.util.Optional;


@Controller
public class QuestionController {

    private final UserDao userDao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final QuestionDao questionDao;
    private List<Question> wrongList;

    //ログイン画面や問題を管理するコントローラ
    public QuestionController(UserDao userDao, QuestionDao questionDao) {
        this.userDao = userDao;
        this.questionDao = questionDao;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    //ログイン画面
    @GetMapping("/login")
    public String showLogin(
            @RequestParam(required = false) String needLogin,
            RedirectAttributes ra) {

        ra.addFlashAttribute("openLoginModal", true);
        if (needLogin != null) {
            ra.addFlashAttribute("error", "ログインしてください");
        }
        return "redirect:/index";
    }

    //ログイン処理
    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam(required = false) String from,
                          HttpSession session,
                          Model model,
                          RedirectAttributes ra){

        //ログインチェックためのDB接続
        User user = userDao.findByEmail(email);

        //チェック
        //間違っているとき
        if (user == null || !encoder.matches(password, user.getPasswordHash())){
            if ("index".equals(from)) {
                ra.addFlashAttribute("error", "メールアドレスまたはパスワードが違います");
                ra.addFlashAttribute("email", email);
                ra.addFlashAttribute("openLoginModal", true);
                return "redirect:/index";
            }
            model.addAttribute("error", "メールアドレスまたはパスワードが違います");
            model.addAttribute("email", email);
            return "login";
        }
        //あっているとき
        session.setAttribute("loginUser", user.getEmail());
        session.setAttribute("loginUserName",user.getName());

        if("ADMIN".equals(user.getRole())){
            return "redirect:/admin";
        }
        return "redirect:/index";
    }

    //ログアウト
    @GetMapping("/logout")
    public String logout(HttpSession session){

        //セッションを破棄
        session.invalidate();
        return "redirect:/index";
    }

    //画面表示用index
    @GetMapping("/index")
    public String showIndex(@RequestParam(required = false) String needLogin,
                            @RequestParam(required = false) String openLoginModal,
                            HttpSession session,
                            Model model) {

        if (openLoginModal != null) {
            model.addAttribute("openLoginModal", true);
        }
        if (needLogin != null) {
            model.addAttribute("error", "ログインしてください");
        }

        User user = checkUser(session);
        if (user == null) {
            model.addAttribute("strategyTotal", 0);
            model.addAttribute("strategyProgress", 0);
            model.addAttribute("managementTotal", 0);
            model.addAttribute("managementProgress", 0);
            model.addAttribute("technologyTotal", 0);
            model.addAttribute("technologyProgress", 0);
            return "index";
        }

        Long userId = user.getId();

        // ストラテジ
        int strategyTotal = questionDao.countByField("strategy");
        QuestionProgress sp = questionDao.showProgress(userId, "strategy");
        int strategyProgress = (sp == null) ? 0 : sp.getNextIndex();

        // マネジメント
        int managementTotal = questionDao.countByField("management");
        QuestionProgress mp = questionDao.showProgress(userId, "management");
        int managementProgress = (mp == null) ? 0 : mp.getNextIndex();

        // テクノロジ
        int technologyTotal = questionDao.countByField("technology");
        QuestionProgress tp = questionDao.showProgress(userId, "technology");
        int technologyProgress = (tp == null) ? 0 : tp.getNextIndex();

        // 画面に渡す
        model.addAttribute("strategyTotal", strategyTotal);
        model.addAttribute("strategyProgress", strategyProgress);

        model.addAttribute("managementTotal", managementTotal);
        model.addAttribute("managementProgress", managementProgress);

        model.addAttribute("technologyTotal", technologyTotal);
        model.addAttribute("technologyProgress", technologyProgress);

        return "index";
    }

    //新規登録画面表示
    @GetMapping("/signUp")
    public String signFrom(){
        return "signUp";
    }

    //新規登録処理
    @PostMapping("/signUp")
    public String signUp(@RequestParam String name,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         @RequestParam(required = false) String from,
                         HttpSession session,
                         Model model,
                         RedirectAttributes ra){

        //登録するパスワードが一致していない時
        if(!password.equals(confirmPassword)){
            if ("index".equals(from)) {
                ra.addFlashAttribute("error", "パスワードが一致していません。");
                ra.addFlashAttribute("signupName", name);
                ra.addFlashAttribute("signupEmail", email);
                ra.addFlashAttribute("openSignupModal", true);
                return "redirect:/index";
            }
            model.addAttribute("error", "パスワードが一致していません。");
            return "signUp";
        }

        //同じアドレスが登録されているかのチェック
        //もしDBにメールアドレスがあるときに代入する変数を設定
        User existing = userDao.findByEmail(email);

        //用意した変数にアドレスが入っているなら（すでに使用されているなら）
        if(existing != null){
            if ("index".equals(from)) {
                ra.addFlashAttribute("error","このアドレスは使用されています。");
                ra.addFlashAttribute("signupName", name);
                ra.addFlashAttribute("signupEmail", email);
                ra.addFlashAttribute("openSignupModal", true);
                return "redirect:/index";
            }
            model.addAttribute("error","このアドレスは使用されています。");
            return "signUp";//新規登録画面に戻す
        }

        //パスワードをハッシュ化
        String passwordHash= encoder.encode(password);
        //DBに登録準備
        userDao.insert(email, passwordHash, name);

        // ★ここが必要（Interceptorを通す）
        session.setAttribute("loginUser", email);
        session.setAttribute("loginUserName", name);

        return "redirect:/index";
    }

    //分野選択
    @GetMapping("/field")
    public String field(Model model, HttpSession session,
                        RedirectAttributes ra) {
        User login = checkUser(session);
        if(login == null){
            ra.addFlashAttribute("error","ログインしてください。");
            return "redirect:/login";
        }
        return "field";
    }

    //問題文
    @GetMapping("/question")
    public String question(@RequestParam String field,
                           @RequestParam(name = "index", defaultValue = "0") int index,
                           Model model,
                           HttpSession session) {

        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }
        //問題番号が0より小さい時（ない時）は0
        if (index < 0) index = 0;

        //１問ずつチェック
        int size = questionDao.countByField(field);

        // ✅ まず範囲チェック（ここが超重要）
        if (size == 0) {
            model.addAttribute("field", field);
            model.addAttribute("message", "この分野に問題がありません");
            model.addAttribute("hasNext", false);
            return "question";
        }
        if (index >= size) {
            index = size - 1; // 最後に丸める（または message 出して戻すでもOK）
        }

        Optional<Question> opt = questionDao.findOneByFindAndIndex(field, index);

        if (opt.isEmpty()) {
            model.addAttribute("field", field);
            model.addAttribute("message", "問題の取得に失敗しました（index=" + index + "）");
            model.addAttribute("hasNext", false);
            return "question";
        }

        Question q = opt.get();

        boolean hasNext = (index + 1) < size;

        // ★ 追加：前の問題
        boolean hasPrev = index > 0;
        int prevIndex = index - 1;

        model.addAttribute("field", field);

        model.addAttribute("prevIndex", prevIndex);  // ★追加
        model.addAttribute("hasPrev", hasPrev);      // ★追加
        model.addAttribute("question", q.getQuestionText());
        model.addAttribute("choice1", q.getChoice1());
        model.addAttribute("choice2", q.getChoice2());
        model.addAttribute("choice3", q.getChoice3());
        model.addAttribute("choice4", q.getChoice4());
        model.addAttribute("index", index);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("nextIndex", index + 1);

        model.addAttribute("hasPrev", index > 0);
        model.addAttribute("prevIndex", index - 1);

        return "question";
    }

    //解答画面
    @PostMapping("/answer")
    public String answer(@RequestParam("field") String field,
                         @RequestParam("selected") String selected,
                         @RequestParam("index") int index,
                         Model model,
                         HttpSession session,
                         RedirectAttributes ra) {

        User login = checkUser(session);
        if (login == null) {
            ra.addFlashAttribute("error", "ログインしてください。");
            return "redirect:/login";
        }

        int selectedNum = Integer.parseInt(selected);

        // DAOに「この分野のこの番号の問題を1件ください」とお願いして
        //結果をOptionalという箱に入れてoptという変数に保存している
        Optional<Question> opt = questionDao.findOneByFindAndIndex(field, index);

        //もし空だったら　isEmpty()空かどうか調べるメソッド
        if (opt.isEmpty()) {
            model.addAttribute("message", "この分野の問題はここまでです");
            model.addAttribute("field", field);
            model.addAttribute("index", index);
            model.addAttribute("hasNext", false);
            return "question";
        }
        //ユーザが選んだ撰択肢と正解肢
        Question q = opt.get();

        boolean isCorrect = (q.getCorrectAnswer() == selectedNum);

        //正解不正解を登録DBに接続
        questionDao.userAnswer((int) login.getId(),
                q.getId(),
                selectedNum,
                isCorrect);

        String selectedText = switch (selectedNum) {
            case 1 -> q.getChoice1();
            case 2 -> q.getChoice2();
            case 3 -> q.getChoice3();
            default -> q.getChoice4();
        };

        // ★countもdbField　次へ等のボタン用
        int size = questionDao.countByField(field);
        int nextIndex = index + 1;
        boolean hasNext = nextIndex < size;

        //どこまで解いたかを自動保存
        questionDao.saveProgress((int)login.getId(), field, nextIndex);

        model.addAttribute("field", field);
        model.addAttribute("selectedText", selectedText);
        model.addAttribute("isCorrect", isCorrect);
        model.addAttribute("explanation_text", q.getExplanation());
        model.addAttribute("nextIndex", nextIndex);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("examText", q.getRealExamExample());

        // ★ここを追加
        model.addAttribute("index", index);//今の問題
        model.addAttribute("hasPrev", index > 0);//前の問題があるかどうか
        model.addAttribute("prevIndex", index - 1);//１つ前の問題

        return "answer";
    }

    //復習問題リスト表示ページ
    @GetMapping("/reviewWrongList")
    public String reviewWrongList(HttpSession session,
                                  Model model){

        User user = checkUser(session);
        if(user == null){
            return "redirect:/login";
        }

        List<Question> wrongList = questionDao.wrongList(user.getId());

        if(wrongList.isEmpty()){
            model.addAttribute("message","復習問題はありません");
        }

        //wrongListをsessionに一時保存
        session.setAttribute("wrongList", wrongList);
        model.addAttribute("questions", wrongList);

        return "/myPage/reviewWrongList";
    }

    //間違えた問題を表示画面用
    @GetMapping("/wrongQuestions")
    public String wrongQuestions(@RequestParam int index,
                                 HttpSession session,
                                 Model model){

        User user = checkUser(session);
        if(user == null){
            return "redirect:/login";
        }

        //問題一覧からsessionで保存していたをwrongListを受け取る
        List<Question> wrongList = (List<Question>) session.getAttribute("wrongList");
        if(wrongList == null || wrongList.isEmpty()){
            return "redirect:/reviewWrongList";
        }

        if(index < 0 || index >= wrongList.size()){
            return "redirect:/reviewWrongList";
        }

        Question question = wrongList.get(index);

        int nextIndex = index + 1;
        int prevIndex = index - 1;
        boolean hasNext = nextIndex < wrongList.size();
        boolean hasPrev = index > 0;

        model.addAttribute("question", question);
        model.addAttribute("index", index);
        model.addAttribute("nextIndex", nextIndex);
        model.addAttribute("prevIndex", prevIndex);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrev", hasPrev);

        return "myPage/wrongQuestions";
    }

    //復習したとき用の解答表示画面
    @PostMapping("/wrongQuestionsAnswer")
    public String wrongQuestionsAnswer(@RequestParam int questionId,
                                       @RequestParam int selected,
                                       @RequestParam int index,
                                       HttpSession session,
                                       Model model){

        User user = checkUser(session);
        if(user == null){
            return "redirect:/login";
        }

        List<Question> wrongList = (List<Question>) session.getAttribute("wrongList");
        if(wrongList == null || wrongList.isEmpty()){
            return "redirect:/reviewWrongList";
        }

        if(index < 0 || index >= wrongList.size()){
            return "redirect:/reviewWrongList";
        }

        Question question = wrongList.get(index);

        boolean isCorrect = (question.getCorrectAnswer() == selected);

        long userId = user.getId();

        questionDao.userAnswer((int) userId, questionId, selected, isCorrect);

        int size = wrongList.size();
        int nextIndex = index + 1;
        int prevIndex = index - 1;
        boolean hasNext = nextIndex < size;
        boolean hasPrev = index > 0;

        model.addAttribute("question", question);
        model.addAttribute("selected", selected);
        model.addAttribute("isCorrect", isCorrect);
        model.addAttribute("explanation_text", question.getExplanation());
        model.addAttribute("examText", question.getRealExamExample());

        model.addAttribute("index", index);
        model.addAttribute("nextIndex", nextIndex);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("prevIndex", prevIndex);
        model.addAttribute("hasPrev", hasPrev);

        return "myPage/answerReview";
    }

    //続きから始める　continued
    @GetMapping("/continued")
    public String continued(@RequestParam String field,
                            HttpSession session,
                            Model model) {

        User user = checkUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        QuestionProgress progress = questionDao.showProgress(user.getId(), field);

        // まだ続きがないなら最初から
        if (progress == null) {
            return "redirect:/question?field=" + field + "&index=0";
        }

        // 続きがあるならその位置へ
        return "redirect:/question?field=" + field + "&index=" + progress.getNextIndex();
    }

    //登録されているユーザーチェック
    public User checkUser(HttpSession session) {
        String email = (String) session.getAttribute("loginUser");
        if(email == null) return null;

        return  userDao.findByEmail(email);
    }


}

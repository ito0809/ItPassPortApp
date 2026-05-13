package com.example.itpassportapp.Controller;

import java.util.List;
import com.example.itpassportapp.History.FieldRateDto;
import com.example.itpassportapp.History.HistoryDto;
import com.example.itpassportapp.History.TodayResultDto;
import com.example.itpassportapp.Question.QuestionDao;
import com.example.itpassportapp.user.User;
import com.example.itpassportapp.user.UserDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class MyPageController {

    private final UserDao userDao;
    //private final PasswordEncoder encoder;

    private final QuestionDao questionDao;

    public MyPageController(UserDao userDao, QuestionDao questionDao/*, PasswordEncoder encoder*/) {
        this.userDao = userDao;
        this.questionDao = questionDao;
    }

    // マイページ表示
    @GetMapping("/myPage")
    public String myPage(HttpSession session,
                         Model model){

        //ログインしているかのチェック
        User user = checkLoginUser(session);
        if(user == null){
            return "redirect:/login";
        }

        long userId = user.getId();

        //今日の学習結果
        TodayResultDto todayCount = questionDao.todayHistory(userId);

        //昨日の学習結果
        TodayResultDto yesterdayCount = questionDao.yesterdayHistory(userId);

        //分野別の正解率
        List<FieldRateDto> rates = questionDao.fieldRate(userId);

        //最近の学習履歴を見る
        List<HistoryDto> history = questionDao.historyDao(userId);

        model.addAttribute("todayCount",todayCount);
        model.addAttribute("yesterdayCount",yesterdayCount);
        model.addAttribute("rates",rates);
        model.addAttribute("history",history);

        return "myPage/myPage";
    }

    //登録更新のための現在の情報取得
    @GetMapping("/myPageUpDate")
    public String upDateInsert(HttpSession session, Model modle){

        //セッションからログインしているメール取得
        String email = (String) session.getAttribute("loginUser");

        //DBからメール取得
        User user = userDao.findByEmail(email);

        //画面に渡す
        modle.addAttribute("user",user);
        return "myPage/myPageUpdate";
    }

    //メールアドレス更新
    @PostMapping("/emailUpdate")
    public String emailUpdate(@RequestParam ("newEmail")String newEmail,
                              HttpSession session,
                              Model model) {

        String currentEmail = (String) session.getAttribute("loginUser");
        if (currentEmail == null) {
            return "redirect:/login";//現在のアドレスがセッションに入ってない時はログインへ返す
        }
        //アドレスがすでに使用されているかのチェック
        User existinUser = userDao.findByEmail(newEmail);

        //代入したものが空でないとき
        if(existinUser != null){
            User user = userDao.findByEmail(currentEmail);
            model.addAttribute("user", user);
            model.addAttribute("error","そのアドレスは既に使用されています");
            return "myPage/myPageUpdate";
        }
        //更新
        userDao.updateEmail(currentEmail,newEmail);

        //セッション更新
        session.setAttribute("loginUser", newEmail);
        //model.addAttribute("user","メールアドレスを変更しました");
        return "redirect:/myPageUpdate?success=true";
    }

    //更新成功 画面表示
    @GetMapping("/myPageUpdate")
    public String showUpdateForm(
            @RequestParam(required = false) String success,
            HttpSession session,
            Model model){

        String email = (String) session.getAttribute("loginUser");
        if(email == null){
            return "redirect:/login";
        }

        User user = userDao.findByEmail(email);
        model.addAttribute("user", user);

        if(success != null){
            model.addAttribute("success", "メールアドレスを変更しました");
        }

        return "myPage/myPageUpdate";
    }


    //アカウント削除
    @PostMapping("/mypageDelete")
    public String myPageDelete(HttpSession session) {
        String email =(String) session.getAttribute("loginUser");

        userDao.deleteByEmail(email);
            session.invalidate();
            return "redirect:/login";

    }

    //ログインしているかのチェック
    private User checkLoginUser(HttpSession session){

        String email = (String) session.getAttribute("loginUser");

        if(email ==null){
            return null;
         }
        User user = userDao.findByEmail(email);
        return user;
    }


}


package nagasawakenji.englishWordQuizFinal.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PostPersist;
import jakarta.validation.Valid;
import nagasawakenji.englishWordQuizFinal.dto.QuizSetting;
import nagasawakenji.englishWordQuizFinal.dto.SearchForm;
import nagasawakenji.englishWordQuizFinal.dto.WordForm;
import nagasawakenji.englishWordQuizFinal.entity.Word;
import nagasawakenji.englishWordQuizFinal.repository.WordRepository;
import nagasawakenji.englishWordQuizFinal.service.ApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/app")
@SessionAttributes("quizSetting")
public class ApplicationController {

    WordRepository wordRepository;
    ApplicationService applicationService;

    public ApplicationController(WordRepository wordRepository,
                                 ApplicationService applicationService) {
        this.wordRepository = wordRepository;
        this.applicationService = applicationService;
    }

    @ModelAttribute("quizSetting")
    public QuizSetting initQuizSetting() {
        return new QuizSetting();
    }

    // クイズのメニュー画面の表示
    // メニュー画面にはクイズ、単語編集、単語検索の3つの機能がある
    @GetMapping("/menu")
    public String showMenu(Model model) {

        return "/app/menu";
    }

    // クイズのメニュー画面の処理
    @PostMapping("/menu")
    public String processMenu(Model model,
                              @RequestParam(name = "mode") String mode) {

        return switch (mode) {
            case "quiz" -> "redirect:/app/quiz";
            case "search" -> "redirect:/app/search";
            case "edit" -> "redirect:/app/edit";
            case "register" -> "redirect:/app/register";
            default -> {
                model.addAttribute("error", "エラーが発生しました");
                yield "error";
            }
        };
    }

    // 単語検索、編集機能の表示
    // 編集する単語はキーワード検索で探せる(英単語のスペルや意味や品詞で絞れる)
    // 検索した英単語を編集することもできる
    @GetMapping("/search")
    public String showSearchForm(Model model) {

        model.addAttribute("searchForm", new SearchForm());
        model.addAttribute("results", List.of());

        return "/app/search";
    }

    // 検索結果の取得
    // 検索に副作用はないので、リダイレクトはしない
    @PostMapping("/search")
    public String doSearch(Model model,
                           @ModelAttribute(name = "searchForm") SearchForm searchForm) {
        List<Word> results = applicationService.searchWords(searchForm.getKeyWord(), searchForm.getPartOfSpeech());

        model.addAttribute("results", results);

        return "/app/search";
    }

    // 単語編集画面の表示
    @GetMapping("/edit")
    public String showEditForm(Model model,
                               @RequestParam(name = "wordId") long wordId) {
       Word word = wordRepository.findById(wordId)
               .orElseThrow(() -> new EntityNotFoundException("単語が見つかりません"));

       WordForm wordForm = applicationService.convertWordForm(word);

       model.addAttribute("wordForm", wordForm);

        return "/app/edit";
    }

    // 受け取った入力の処理
    @PostMapping("/edit")
    public String editWord(Model model,
                           @ModelAttribute(name = "wordForm") WordForm wordForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "/app/edit";
        }

        applicationService.editWord(wordForm);

        redirectAttributes.addAttribute("success", wordForm.getName() + "を編集しました");

        return "redirect:/app/edit";
    }

    // 単語登録画面の表示
    // 単語編集画面にはWordFormの各フィールドを入力するフォームがある
    // メニューに戻るボタンもある
    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        model.addAttribute("wordForm", new WordForm());
        return "/app/register";
    }

    // 単語登録画面から送られた単語の処理
    @PostMapping("/register")
    public String registerWord(Model model,
                               @Valid @ModelAttribute("wordForm") WordForm wordForm,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "/app/register";
        }
        Word newWord = applicationService.convertWord(wordForm);
        applicationService.saveWord(newWord);
        redirectAttributes.addAttribute("successMessage", newWord.getName() + "を登録しました");

        return "redirect:/app/register";

    }

    // クイズの出題形式選択画面の表示
    @GetMapping("/quiz")
    public String showQuizSettingForm(Model model) {

        QuizSetting quizSetting = new QuizSetting();
        model.addAttribute("quizSetting", quizSetting);

        return "app/quiz";
    }

    @PostMapping("/quiz")
    public String makeQuizSetting(Model model,
                                  @Valid @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "redirect:/app/quiz";
        }
        redirectAttributes.addAttribute("success", "クイズ形式は正常に選択されます");

        List<Long> wordIdList = applicationService.makeQuizList(quizSetting.getPartOfSpeech());
        quizSetting.setWordIdList(wordIdList);
        redirectAttributes.addFlashAttribute("quizSetting", quizSetting);

        return "redirect:/app/start";
    }

    //クイズのスタート画面の表示
    // 真ん中にクイズスタートのボタンがあるだけ
    @GetMapping("/start")
    public String quizStartForm(Model model,
                                @ModelAttribute(name = "quizSetting") QuizSetting quizSetting) {

        return "app/start";
    }

    @PostMapping("/start")
    public String makeQuestion(Model model,
                        @Valid @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/app/quiz";
        }

        Long randomId = applicationService.choseRandomId(quizSetting.getWordIdList());
        redirectAttributes.addFlashAttribute("quizSetting", quizSetting);
        redirectAttributes.addFlashAttribute("randomId", randomId);

        return "redirect:/app/answer";
    }

    // クイズの解答入力画面表示
    // randomIdの単語を表示する
    // 答え(英単語の意味)を"answer"としてバインディングする
    @GetMapping("/answer")
    public String showAnsForm(Model model,
                              @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                              @RequestParam(name = "randomId") Long randomId) {

        return "app/answer";
    }

    // クイズの正誤判定
    @PostMapping("/answer")
    public String judgePartialResult(Model model,
                                    @Valid @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                    @RequestParam(name = "randomId") Long randomId,
                                    @RequestParam(name = "answer") String answer) {

        if (bindingResult.hasErrors()){
            return "redirect:/app/quiz";
        }

        boolean isCorrect = applicationService.judgeIsCorrect(randomId, answer);

        if (isCorrect) {
            quizSetting.setCorrectCnt(quizSetting.getCorrectCnt() + 1);
        }
        quizSetting.setNowCnt(quizSetting.getNowCnt() + 1);

        redirectAttributes.addFlashAttribute("quizSetting", quizSetting);
        redirectAttributes.addFlashAttribute("randomId", randomId);
        redirectAttributes.addFlashAttribute("isCorrect", isCorrect);

        return "redirect:/app/partialresult";
    }

    // クイズの正誤画面表示
    // isCorrectにより正解or不正解を表示する
    @GetMapping("/partialresult")
    public String showPartialResult(Model model,
                                    @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                    @RequestParam(name = "randomId") Long randomId,
                                    @RequestParam(name = "isCorrect") boolean isCorrect) {


        return "/app/partialresult";
    }

    // 次のクイズ画面への移動
    @PostMapping("/partialresult")
    public String moveNextQuestion(Model model,
                                   @Valid @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()){
            return "redirect:/app/quiz";
        }
        redirectAttributes.addFlashAttribute("quizSetting", quizSetting);

        if (quizSetting.getNowCnt() > quizSetting.getQuizCnt()) {
            return "redirect:/app/result";
        }

        return "redirect:/app/start";
    }

    // クイズの最終結果
    //　今までの問題の正誤などをリストにして表示する
    @GetMapping("/result")
    public String showResult(Model model,
                             @ModelAttribute(name = "quizSetting") QuizSetting quizSetting) {
        return "app/result";
    }


}

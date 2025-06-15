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
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

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
            case "list" -> "redirect:/app/list";
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
        model.addAttribute("results", List.<WordForm>of());

        return "app/search";
    }

    // 検索結果の取得
    // 検索に副作用はないので、リダイレクトはしない
    @PostMapping("/search")
    public String doSearch(@ModelAttribute SearchForm searchForm, Model model) {
        List<WordForm> results = applicationService.searchWords(
                        searchForm.getKeyWord(),
                        searchForm.getPartOfSpeech())
                .stream()
                .map(applicationService::convertWordForm)
                .collect(Collectors.toList());
        model.addAttribute("searchForm", searchForm);
        model.addAttribute("results", results);
        return "app/search";
    }

    // 編集する単語の選択画面の表示
    // これで登録されている単語が全て表示され、そこから編集したい単語を選択する
    @GetMapping("/list")
    public String showSelectForm(Model model) {
        List<Word> wordList = wordRepository.findAll();
        List<WordForm> wordFormList = wordList.stream()
                .map(word -> {
                    WordForm wordForm = applicationService.convertWordForm(word);
                    return wordForm;
                }).collect(Collectors.toList());

        model.addAttribute("wordFormList", wordFormList);

        return "/app/list";
    }


    // 選択した単語の処理
    @PostMapping("/list")
    public String toEditForm(Model model,
                             @RequestParam(name = "wordId") Long wordId,
                             RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("wordId", wordId);
        return "redirect:/app/edit";
    }


    // 単語編集画面の表示
    @GetMapping("/edit")
    public String showEditForm(Model model,
                               @RequestParam(name = "wordId", required = false) Long wordId,
                               RedirectAttributes redirectAttributes) {


        if (wordId == null) {
            redirectAttributes.addFlashAttribute("error", "編集対象の単語を選択してください");
            return "redirect:/app/list";
        }

        Word word = wordRepository.findById(wordId)
               .orElseThrow(() -> new EntityNotFoundException("単語が見つかりません"));

       WordForm wordForm = applicationService.convertWordForm(word);

       model.addAttribute("wordForm", wordForm);

        return "app/edit";
    }

    // 受け取った入力の処理
    @PostMapping("/edit")
    public String editWord(Model model,
                           @ModelAttribute(name = "wordForm") WordForm wordForm,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "app/edit";
        }

        applicationService.editWord(wordForm);

        redirectAttributes.addAttribute("wordId", wordForm.getId());
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
    public String showQuizSettingForm(Model model,
                                      SessionStatus sessionStatus) {

        sessionStatus.setComplete();
        model.addAttribute("quizSetting", new QuizSetting());

        return "app/quiz";
    }

    @PostMapping("/quiz")
    public String makeQuizSetting(
            @Valid @ModelAttribute("quizSetting") QuizSetting quizSetting,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {

            return "app/quiz";
        }
        quizSetting.setWordIdList(applicationService.makeQuizList(quizSetting.getPartOfSpeech()));
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
                        @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                        RedirectAttributes redirectAttributes) {

        Long randomId = applicationService.choseRandomId(quizSetting.getWordIdList());
        redirectAttributes.addAttribute("randomId", randomId);


        return "redirect:/app/answer";
    }

    // クイズの解答入力画面表示
    // randomIdの単語を表示する
    // 答え(英単語の意味)を"answer"としてバインディングする
    @GetMapping("/answer")
    public String showAnsForm(Model model,
                              @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                              @RequestParam(name = "randomId") Long randomId) {

        Word randomWord = wordRepository.findById(randomId)
                .orElseThrow(() -> new EntityNotFoundException("単語が見つかりません"));

        WordForm randomWordForm = applicationService.convertWordForm(randomWord);

        model.addAttribute("randomWord",  randomWordForm.getName());
        model.addAttribute("meaningList", randomWordForm.getMeaningListString());
        model.addAttribute("randomId",    randomId);

        return "app/answer";
    }

    // クイズの正誤判定
    @PostMapping("/answer")
    public String judgePartialResult(Model model,
                                    @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                     RedirectAttributes redirectAttributes,
                                    @RequestParam(name = "randomId") Long randomId,
                                    @RequestParam(name = "answer") String answer) {


        boolean isCorrect = applicationService.judgeIsCorrect(randomId, answer);

        if (isCorrect) {
            quizSetting.setCorrectCnt(quizSetting.getCorrectCnt() + 1);
        }
        quizSetting.setNowCnt(quizSetting.getNowCnt() + 1);

        redirectAttributes.addAttribute("randomId", randomId);
        redirectAttributes.addAttribute("isCorrect", isCorrect);

        return "redirect:/app/partialresult";
    }

    // クイズの正誤画面表示
    // isCorrectにより正解or不正解を表示する
    @GetMapping("/partialresult")
    public String showPartialResult(Model model,
                                    @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                    @RequestParam(name = "randomId") Long randomId,
                                    @RequestParam(name = "isCorrect") boolean isCorrect) {

        model.addAttribute("randomId", randomId);
        model.addAttribute("isCorrect", isCorrect);

        return "app/partialresult";
    }

    // 次のクイズ画面への移動
    @PostMapping("/partialresult")
    public String moveNextQuestion(Model model,
                                   @ModelAttribute(name = "quizSetting") QuizSetting quizSetting,
                                   RedirectAttributes redirectAttributes) {


        if (quizSetting.getNowCnt() >= quizSetting.getQuizCnt()) {
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

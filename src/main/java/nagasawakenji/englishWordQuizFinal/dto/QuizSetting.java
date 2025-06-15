package nagasawakenji.englishWordQuizFinal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nagasawakenji.englishWordQuizFinal.entity.PartOfSpeech;

import java.util.List;

public class QuizSetting {

    @Min(value = 1, message = "クイズの問題数は1問以上にしてください")
    private int quizCnt;

    private int nowCnt = 0;


    private PartOfSpeech partOfSpeech;
    private int correctCnt;
    private List<Long> wordIdList;



    private List<PartOfSpeech> partOfSpeechList;

    public QuizSetting() {};

    public void setQuizCnt(int quizCnt) {
        this.quizCnt = quizCnt;
    }

    public int getQuizCnt() {
        return quizCnt;
    }

    public void setCorrectCnt(int correctCnt) {
        this.correctCnt = correctCnt;
    }

    public int getCorrectCnt() {
        return correctCnt;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setWordIdList(List<Long> wordIdList) {
        this.wordIdList = wordIdList;
    }

    public List<Long> getWordIdList() {
        return wordIdList;
    }

    public void setNowCnt(int nowCnt) {
        this.nowCnt = nowCnt;
    }

    public int getNowCnt() {
        return nowCnt;
    }

    public void setPartOfSpeechList(List<PartOfSpeech> partOfSpeechList) {
        this.partOfSpeechList = partOfSpeechList;
    }

    public List<PartOfSpeech> getPartOfSpeechList() {
        return partOfSpeechList;
    }
}

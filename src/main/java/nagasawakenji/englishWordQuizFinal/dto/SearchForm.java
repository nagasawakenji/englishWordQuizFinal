package nagasawakenji.englishWordQuizFinal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import nagasawakenji.englishWordQuizFinal.entity.PartOfSpeech;


public class SearchForm {

    @Size(max = 100, message = "キーワードは100文字以内にしてください")
    private String keyWord;

    private PartOfSpeech partOfSpeech;

    public SearchForm() {};

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }
}

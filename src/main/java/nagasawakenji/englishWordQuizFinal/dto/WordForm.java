package nagasawakenji.englishWordQuizFinal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import nagasawakenji.englishWordQuizFinal.entity.PartOfSpeech;

import java.util.List;

public class WordForm {
    private Long id;

    @NotBlank(message = "単語名を入力してください")
    private String name;

    @NotNull(message = "品詞を選択してください")
    private PartOfSpeech partOfSpeech;

    @NotBlank(message = "意味を入力してください")
    private String meaningListString;

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setMeaningListString(String meaningListString) {
        this.meaningListString = meaningListString;
    }

    public String getMeaningListString() {
        return meaningListString;
    }
}

package nagasawakenji.englishWordQuizFinal.entity;

import jakarta.persistence.*;

@Entity
public class Mean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String meaningText;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    public Long getId() {
        return id;
    }

    public void setMeaningText(String meaningText) {
        this.meaningText = meaningText;
    }

    public String getMeaningText() {
        return meaningText;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public Word getWord() {
        return word;
    }
}

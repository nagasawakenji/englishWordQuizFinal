package nagasawakenji.englishWordQuizFinal.service;


import jakarta.persistence.EntityNotFoundException;
import nagasawakenji.englishWordQuizFinal.dto.WordForm;
import nagasawakenji.englishWordQuizFinal.entity.Mean;
import nagasawakenji.englishWordQuizFinal.entity.PartOfSpeech;
import nagasawakenji.englishWordQuizFinal.entity.Word;
import nagasawakenji.englishWordQuizFinal.exception.DuplicationWordException;
import nagasawakenji.englishWordQuizFinal.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    WordRepository wordRepository;

    public ApplicationService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public WordForm convertWordForm(Word word) {
        WordForm wordForm = new WordForm();

        wordForm.setId(word.getId());
        wordForm.setName(word.getName());
        wordForm.setPartOfSpeech(word.getPartOfSpeech());

        List<String> meaningListString = word.getMeaningList().stream()
                .map(mean -> mean.getMeaningText())
                .collect(Collectors.toList());

        String meaningTextString = String.join(",", meaningListString);
        wordForm.setMeaningListString(meaningTextString);

        return wordForm;

    }

    public Word convertWord(WordForm wordForm) {

        Word word = new Word();
        word.setId(word.getId());
        word.setName(wordForm.getName());

       word.setPartOfSpeech(wordForm.getPartOfSpeech());

        List<String> meaningListString = Arrays.stream(
                wordForm.getMeaningListString().split("\\s*,\\s*")
        )
                .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());

        List<Mean> meaningList = meaningListString.stream()
                .map(text -> {
                    Mean mean = new Mean();
                    mean.setMeaningText(text);
                    mean.setWord(word);
                    return mean;
                }).collect(Collectors.toList());

        word.setMeaningList(meaningList);

        return word;

    }

    @Transactional
    public void saveWord(Word word) {

        if (wordRepository.existsByName(word.getName())) {
            throw new DuplicationWordException("既に同じ単語があります: " + word.getName());
        }
        wordRepository.save(word);

    }

    @Transactional(readOnly = true)
    public List<Word> searchWords(String keyWord, PartOfSpeech pos) {
        if (pos == null) {
            return wordRepository.searchByKeyword(keyWord);
        }

        return wordRepository.searchByPartOfSpeechAndKeyword(pos, keyWord);
    }

    @Transactional(readOnly = true)
    public Word findById(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("単語が見つかりません"));
    }

    @Transactional
    public void editWord(WordForm wordForm) {
        Word word = wordRepository.findById(wordForm.getId()).orElseThrow();

        word.setName(wordForm.getName());
        word.setPartOfSpeech(wordForm.getPartOfSpeech());

        List<String> meaningListString = Arrays.stream(
                wordForm.getMeaningListString().split("\\s*,\\s*")
        ).filter(text -> !text.isBlank())
                .collect(Collectors.toList());

        List<Mean> meaningList = word.getMeaningList();

        word.getMeaningList().clear();

        for (String meaningString : meaningListString) {
           Mean mean = new Mean();
           mean.setWord(word);
           mean.setMeaningText(meaningString);
           meaningList.add(mean);
        }

    }

    @Transactional(readOnly = true)
    public List<Long> makeQuizList(PartOfSpeech partOfSpeech) {
        List<Word> wordList;
        if (partOfSpeech == null) {
            wordList = wordRepository.findAll();
        } else {
            wordList = wordRepository.findByPartOfSpeech(partOfSpeech);
        }
        return wordList.stream()
                .map(Word::getId)
                .collect(Collectors.toList());
    }

    public Long choseRandomId(List<Long> wordIdList) {

        int randomIndex = (int) (Math.random() * wordIdList.size());
        Long randomId = wordIdList.get(randomIndex);

        return randomId;
    }

    @Transactional
    public boolean judgeIsCorrect(Long wordId, String answer) {

        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new EntityNotFoundException("単語が見つかりません: id=" + wordId));

        String userAnswer = (answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT));

        return word.getMeaningList().stream()
                .map(mean -> mean.getMeaningText() == null
                ? ""
                        : mean.getMeaningText().trim().toLowerCase(Locale.ROOT))
                .anyMatch(correctMeaning -> correctMeaning.equals(userAnswer));
    }

}

package nagasawakenji.englishWordQuizFinal.repository;

import nagasawakenji.englishWordQuizFinal.entity.PartOfSpeech;
import nagasawakenji.englishWordQuizFinal.entity.Word;
import nagasawakenji.englishWordQuizFinal.entity.Mean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    public List<Word> findByName(String name);

    public List<Word> findByPartOfSpeech(PartOfSpeech partOfSpeech);

    public boolean existsByName(String name);

    @Query("""
              SELECT DISTINCT w
              FROM   Word w
              JOIN   w.meaningList m
              WHERE  LOWER(w.name) LIKE LOWER(CONCAT('%', :kw, '%'))
                 OR  LOWER(m.meaningText) LIKE LOWER(CONCAT('%', :kw, '%'))
            """)
    List<Word> searchByKeyword(@Param("kw") String keyword);

    @Query("""
              SELECT w
              FROM   Word w
              WHERE  w.partOfSpeech = :pos
                AND LOWER(w.name) LIKE LOWER(CONCAT('%', :kw, '%'))
            """)
    List<Word> searchByPartOfSpeechAndKeyword(
            @Param("pos") PartOfSpeech pos,
            @Param("kw") String keyword
    );

}

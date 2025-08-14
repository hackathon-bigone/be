package hackathon.bigone.sunsak.recipe.board.repository;

import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 여러 키워드를 리스트로 받아서 검색
    @Query("SELECT DISTINCT b FROM Board b JOIN b.ingredients i WHERE b.title IN :keywords OR i.ingredientName IN :keywords")
    List<Board> findByKeywords(@Param("keywords") List<String> keywords);

    // 단일 키워드로 제목 또는 재료명을 검색
    @Query("SELECT DISTINCT b FROM Board b JOIN b.ingredients i WHERE b.title LIKE %:keyword% OR i.ingredientName LIKE %:keyword%")
    List<Board> findBySingleKeyword(@Param("keyword") String keyword);
}
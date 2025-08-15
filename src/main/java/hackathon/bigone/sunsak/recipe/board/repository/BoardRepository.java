package hackathon.bigone.sunsak.recipe.board.repository;// BoardRepository.java
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT DISTINCT b FROM Board b JOIN b.ingredients i WHERE b.title LIKE %:keyword% OR i.ingredientName LIKE %:keyword%")
    List<Board> findBySingleKeyword(@Param("keyword") String keyword);

    @Query("SELECT b FROM Board b LEFT JOIN b.likes l GROUP BY b.postId ORDER BY COUNT(l) DESC")
    List<Board> findAllByPopularity();

    List<Board> findByCategoriesContaining(String category);
    long countByCategoriesContaining(String category);
}
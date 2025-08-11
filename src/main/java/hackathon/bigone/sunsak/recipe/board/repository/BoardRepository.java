package hackathon.bigone.sunsak.recipe.board.repository;

import hackathon.bigone.sunsak.recipe.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Long> {

}

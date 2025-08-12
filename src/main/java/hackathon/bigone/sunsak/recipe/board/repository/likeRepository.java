package hackathon.bigone.sunsak.recipe.board.repository;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.entity.RecipeLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<RecipeLike, Long> {
    Optional<RecipeLike> findByBoardAndUser(Board board, SiteUser user);
    List<RecipeLike> findByUser(SiteUser user);
    long countByBoard(Board board);
}

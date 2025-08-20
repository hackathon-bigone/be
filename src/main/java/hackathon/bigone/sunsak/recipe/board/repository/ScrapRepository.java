package hackathon.bigone.sunsak.recipe.board.repository;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.entity.RecipeScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<RecipeScrap,Long> {
    Optional<RecipeScrap> findByBoardAndUser(Board board,  SiteUser user);
    List<RecipeScrap> findByUser(SiteUser user);
    long countByBoard(Board board);
    long countByUser_Id(long userId);
}

package hackathon.bigone.sunsak.recipe.comment.repository;

import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByBoard(Board board);
    List<Comment> findByBoard_PostId(Long boardPostId);
    List<Comment> findByAuthor_Id(Long authorId);
    long countByAuthor_Id(Long authorId);
}
package hackathon.bigone.sunsak.recipe.comment.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.repository.BoardRepository;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentDto;
import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import hackathon.bigone.sunsak.recipe.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Comment addComment(Long boardPostId, String content, SiteUser author){
        Board board = boardRepository.findById(boardPostId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setAuthor(author);
        comment.setContent(content);

        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long boardPostId){
        Board board = boardRepository.findById(boardPostId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));

        return commentRepository.findByBoard(board).stream()
                .map(c -> {
                    CommentDto dto = new CommentDto();
                    dto.setCommentId(c.getCommentId());
                    dto.setBoardPostId(board.getPostId());
                    dto.setAuthorId(c.getAuthor().getId());
                    dto.setContent(c.getContent());
                    return dto;
                }).toList();
    }
}

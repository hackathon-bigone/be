package hackathon.bigone.sunsak.recipe.comment.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.repository.BoardRepository;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentRequestDto;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentResponseDto;
import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import hackathon.bigone.sunsak.recipe.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public CommentResponseDto addComment(Long boardPostId, CommentRequestDto requestDto, SiteUser author) {
        Board board = boardRepository.findById(boardPostId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + boardPostId));

        Comment comment = new Comment();
        comment.setContent(requestDto.getContent());
        comment.setBoard(board);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);

        return convertToResponseDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long boardPostId) {
        Board board = boardRepository.findById(boardPostId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + boardPostId));

        List<Comment> comments = commentRepository.findByBoard(board);

        return comments.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private CommentResponseDto convertToResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setCommentId(comment.getId());
        dto.setBoardPostId(comment.getBoard().getPostId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreateDate());
        return dto;
    }
}
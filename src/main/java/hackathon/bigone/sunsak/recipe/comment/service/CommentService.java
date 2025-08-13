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

        Comment parentComment = null;
        if (requestDto.getParentId() != null) {
            parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found with id: " + requestDto.getParentId()));
        }

        Comment comment = new Comment();
        comment.setContent(requestDto.getContent());
        comment.setBoard(board);
        comment.setAuthor(author);
        comment.setParent(parentComment);

        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long boardPostId) {
        List<Comment> allComments = commentRepository.findByBoard_PostId(boardPostId);

        List<CommentResponseDto> parentComments = allComments.stream()
                .filter(comment -> comment.getParent() == null)
                .map(comment -> new CommentResponseDto(comment))
                .collect(Collectors.toList());

        return parentComments;
    }

    @Transactional
    public void deleteComment(Long boardPostId, Long commentId, SiteUser currentUser) {
        boardRepository.findById(boardPostId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        if (!comment.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}
package hackathon.bigone.sunsak.recipe.comment.dto;

import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CommentResponseDto {

    private Long commentId;
    private Long boardPostId;
    private Long authorId;
    private String content;
    private String createdAt;
    private List<CommentResponseDto> children;

    public CommentResponseDto() {
    }

    // Comment 엔티티를 DTO로 변환하는 생성자
    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.boardPostId = comment.getBoard().getPostId();
        this.authorId = comment.getAuthor().getId();
        this.content = comment.getContent();
        this.createdAt = DisplayDateUtil.toDisplay(comment.getCreateDate());
        // 자식 댓글이 있다면, 재귀적으로 DTO로 변환하여 children 필드에 저장
        this.children = comment.getChildren().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }
}
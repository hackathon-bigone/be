package hackathon.bigone.sunsak.recipe.comment.dto;

import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import lombok.Getter;
import java.time.LocalDateTime; // joda.time 대신 java.time 사용

@Getter
public class MypageDto {
    private Long commentId;
    private String content;
    private Long boardId;
    private String boardTitle;
    private LocalDateTime createdAt;

    public MypageDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.boardId = comment.getBoard().getPostId();
        this.boardTitle = comment.getBoard().getTitle();
        this.createdAt = comment.getCreateDate();
    }
}
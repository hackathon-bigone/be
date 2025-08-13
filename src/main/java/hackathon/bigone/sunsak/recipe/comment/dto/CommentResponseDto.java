package hackathon.bigone.sunsak.recipe.comment.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseDto {
    private Long commentId;
    private Long boardPostId;
    private Long authorId;
    private String content;
    private LocalDateTime createdAt; // 응답에 생성 시각을 포함하는 것이 좋습니다.
}
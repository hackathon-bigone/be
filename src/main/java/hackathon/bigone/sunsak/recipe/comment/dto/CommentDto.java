package hackathon.bigone.sunsak.recipe.comment.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentDto {
    private Long commentId;
    private Long boardPostId;
    private Long authorId;
    private String content;
}

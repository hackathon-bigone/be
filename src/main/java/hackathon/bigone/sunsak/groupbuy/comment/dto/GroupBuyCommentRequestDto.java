package hackathon.bigone.sunsak.groupbuy.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupBuyCommentRequestDto {
    private String content;
    private Long parentId;
}
package hackathon.bigone.sunsak.groupbuy.comment.dto;

import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyGroupBuyCommentDto {

    private Long commentId;
    private String content;
    private Long groupbuyBoardId;
    private String groupbuyBoardTitle;
    private LocalDateTime createdAt;

    public MyGroupBuyCommentDto(GroupBuyComment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.groupbuyBoardId = comment.getGroupbuy().getGroupbuyId();
        this.groupbuyBoardTitle = comment.getGroupbuy().getGroupbuyTitle(); // <-- 이 부분을 수정했습니다.
        this.createdAt = comment.getCreateDate();
    }
}
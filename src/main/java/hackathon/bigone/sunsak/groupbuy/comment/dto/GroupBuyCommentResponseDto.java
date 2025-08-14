package hackathon.bigone.sunsak.groupbuy.comment.dto;

import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GroupBuyCommentResponseDto {

    private Long commentId;
    private Long groupbuyId;
    private String authorName;
    private String content;
    private String createdAt;
    private Long parentId;
    private List<GroupBuyCommentResponseDto> children;

    public GroupBuyCommentResponseDto() {}

    public GroupBuyCommentResponseDto(GroupBuyComment comment) {
        this.commentId = comment.getId();
        this.groupbuyId = (comment.getGroupbuy() != null) ? comment.getGroupbuy().getGroupbuyId() : null;
        this.authorName = (comment.getAuthor() != null) ? comment.getAuthor().getNickname() : null;
        this.content = comment.getContent();
        this.createdAt = DisplayDateUtil.toDisplay(comment.getCreateDate());
        this.parentId = (comment.getParent() != null) ? comment.getParent().getId() : null;
        this.children = comment.getChildren().stream()
                .map(GroupBuyCommentResponseDto::new)
                .collect(Collectors.toList());
    }
}

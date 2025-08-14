package hackathon.bigone.sunsak.groupbuy.comment.dto;

import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class GroupBuyCommentResponseDto {
    private Long id;
    private String content;
    private String authorName;
    private LocalDateTime createDate;
    private Long parentId;

    private List<GroupBuyCommentResponseDto> children;

    // 엔티티를 DTO로 변환하는 생성자
    public GroupBuyCommentResponseDto(GroupBuyComment groupBuyComment) {
        this.id = groupBuyComment.getId();
        this.content = groupBuyComment.getContent();
        this.authorName = groupBuyComment.getAuthor().getNickname();
        this.createDate = groupBuyComment.getCreateDate();
        if (groupBuyComment.getParent() != null) {
            this.parentId = groupBuyComment.getParent().getId();
        }

        // 자식 댓글(대댓글) 리스트를 DTO로 변환
        this.children = groupBuyComment.getChildren().stream()
                .map(GroupBuyCommentResponseDto::new)
                .collect(Collectors.toList());
    }
}
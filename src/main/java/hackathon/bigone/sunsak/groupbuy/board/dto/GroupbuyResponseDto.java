package hackathon.bigone.sunsak.groupbuy.board.dto;

import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyLink;
import hackathon.bigone.sunsak.groupbuy.board.enums.GroupBuyStatus;
import hackathon.bigone.sunsak.groupbuy.comment.dto.GroupBuyCommentResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class GroupbuyResponseDto {
    private Long groupbuyId;
    private String groupbuyTitle;
    private String groupbuyDescription;
    private String mainImageUrl;
    private int groupbuyCount;
    private GroupBuyStatus status;
    private LocalDateTime createDate;
    private String authorName;

    private List<String> groupbuyLinkUrls;

    private int scrapCount;
    private int commentCount;
    private List<GroupBuyCommentResponseDto> comments;

    public GroupbuyResponseDto(Groupbuy groupbuy) {
        this.groupbuyId = groupbuy.getGroupbuyId();
        this.groupbuyTitle = groupbuy.getGroupbuyTitle();
        this.groupbuyDescription = groupbuy.getGroupbuyDescription();
        this.mainImageUrl = groupbuy.getMainImageUrl();
        this.groupbuyCount = groupbuy.getGroupbuyCount();
        this.status = groupbuy.getStatus();
        this.createDate = groupbuy.getCreateDate();

        this.authorName = (groupbuy.getAuthor() != null) ? groupbuy.getAuthor().getNickname() : null;

        this.groupbuyLinkUrls = (groupbuy.getBuyLinks() != null) ?
                groupbuy.getBuyLinks().stream()
                        .map(GroupBuyLink::getGroupbuylinkUrl)
                        .collect(Collectors.toList()) :
                List.of();
        this.scrapCount = (groupbuy.getScraps() != null) ? groupbuy.getScraps().size() : 0;

        this.commentCount = (groupbuy.getGroupBuyComments() != null) ? groupbuy.getGroupBuyComments().size() : 0;
        this.comments = (groupbuy.getGroupBuyComments() != null) ?
                groupbuy.getGroupBuyComments().stream()
                        .map(GroupBuyCommentResponseDto::new)
                        .collect(Collectors.toList()) :
                List.of();
    }

    public GroupbuyResponseDto(Groupbuy groupbuy, List<GroupBuyCommentResponseDto> comments) {
        this(groupbuy);
        this.comments = comments;
    }
}
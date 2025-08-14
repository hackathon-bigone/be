package hackathon.bigone.sunsak.groupbuy.board.dto;

import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyLink;
import hackathon.bigone.sunsak.groupbuy.board.enums.GroupBuyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class GroupbuyResponseDto {
    private Long groupbuyId;
    private String groupbuyTitle;
    private String groupbuyDescription;
    private String mainImageUrl;
    private int groupbuyCount;
    private GroupBuyStatus status;
    private LocalDateTime createDate;
    private String authorName; // 작성자 이름

    private List<String> groupbuyLinkUrls;

    private int scrapCount;
    private int commentCount;

    public GroupbuyResponseDto(Groupbuy groupbuy) {
        this.groupbuyId = groupbuy.getGroupbuyId();
        this.groupbuyTitle = groupbuy.getGroupbuyTitle();
        this.groupbuyDescription = groupbuy.getGroupbuyDescription();
        this.mainImageUrl = groupbuy.getMainImageUrl();
        this.groupbuyCount = groupbuy.getGroupbuyCount();
        this.status = groupbuy.getStatus();
        this.createDate = groupbuy.getCreateDate();

        this.authorName = groupbuy.getAuthor().getUsername();

        this.groupbuyLinkUrls = groupbuy.getBuyLinks().stream()
                .map(GroupBuyLink::getGroupbuylinkUrl)
                .collect(Collectors.toList());


        this.scrapCount = groupbuy.getScraps().size();
        this.commentCount = groupbuy.getGroupBuyComments().size();
    }
}
package hackathon.bigone.sunsak.groupbuy.board.dto;

import hackathon.bigone.sunsak.groupbuy.board.enums.GroupBuyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GroupbuyRequestDto {
    private String groupbuyTitle;
    private String groupbuyDescription;
    private String mainImageUrl;
    private Integer groupbuyCount;
    private List<String> buyLinks;
    private GroupBuyStatus status;
}
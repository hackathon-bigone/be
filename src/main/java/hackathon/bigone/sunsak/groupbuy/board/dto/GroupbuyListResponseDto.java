package hackathon.bigone.sunsak.groupbuy.board.dto;

import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GroupbuyListResponseDto {
    private List<GroupbuyResponseDto> groupbuys;
    private long totalCount;
}
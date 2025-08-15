// BoardListResponseDto.java
package hackathon.bigone.sunsak.recipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor // 생성자 자동 생성
public class BoardListResponseDto {
    private List<BoardResponseDto> boards;
    private long totalCount;
}
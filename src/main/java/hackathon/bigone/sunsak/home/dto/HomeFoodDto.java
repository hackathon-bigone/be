package hackathon.bigone.sunsak.home.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeFoodDto {
    private String today;
    private String summary; //유통기한 임박 음식들
    private String message;
    private String dLabel;
}

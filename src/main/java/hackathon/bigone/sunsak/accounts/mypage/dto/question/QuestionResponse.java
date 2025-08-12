package hackathon.bigone.sunsak.accounts.mypage.dto.question;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@JsonPropertyOrder({ "count", "items"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionResponse {
    private long count;
    private List<QuestionListResponse> items;
    private String message;
}

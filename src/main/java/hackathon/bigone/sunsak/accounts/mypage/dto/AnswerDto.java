package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hackathon.bigone.sunsak.accounts.mypage.entity.Answer;
import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerDto {
    @JsonProperty("answer_id")
    private Long answerId;
    private String title;
    private String body;

    @JsonIgnore
    private LocalDateTime createDate;
    private String displayDate;

    public static AnswerDto from(Answer a) {
        if (a == null) return null;

        LocalDateTime date = a.getCreateDate();

        return AnswerDto.builder()
                .answerId(a.getId())
                .title(a.getTitle())
                .body(a.getBody())
                .createDate(date)
                .displayDate(DisplayDateUtil.toDisplay(date))
                .build();
    }
}

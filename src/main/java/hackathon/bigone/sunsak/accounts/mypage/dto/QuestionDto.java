package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionDto {
    @JsonProperty("question_id")
    private Long qId;
    private String title;
    private String body;

    @JsonIgnore
    private LocalDateTime createDate;

    private List<String> imageUrls;

    private String displayDate;
    private String answerStatus;

    public static QuestionDto from(Question q) {
        LocalDateTime date = q.getCreateDate();
        String status = (q.getAnswers() != null)
                ? "답변완료"
                : "답변중";

        return QuestionDto.builder()
                .qId(q.getId())
                .title(q.getTitle())
                .body(q.getBody())
                .imageUrls(q.getImageUrls())
                .createDate(date)
                .displayDate(DisplayDateUtil.toDisplay(date))
                .answerStatus(status)
                .build();
    }
}

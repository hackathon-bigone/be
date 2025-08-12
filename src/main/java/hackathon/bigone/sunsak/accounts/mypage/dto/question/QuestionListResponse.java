package hackathon.bigone.sunsak.accounts.mypage.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import lombok.*;

@JsonPropertyOrder({ "question_id", "title", "body", "displayDate", "answerStatus"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionListResponse { //전체 조회용 dto
    @JsonProperty("question_id")
    private Long qId;
    private String title;
    private String body;

    private String displayDate;
    private String answerStatus;

    public static QuestionListResponse from(Question q) {
        if (q == null) throw new IllegalArgumentException("Question is null");
        String status = (q.getAnswer() != null) ? "답변완료" : "답변중";

        return QuestionListResponse.builder()
                .qId(q.getId())
                .title(q.getTitle())
                .body(q.getBody())
                .displayDate(DisplayDateUtil.toDisplay(q.getCreateDate()))
                .answerStatus(status)
                .build();
    }

}

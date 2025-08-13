package hackathon.bigone.sunsak.accounts.mypage.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import hackathon.bigone.sunsak.accounts.mypage.dto.AnswerDto;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import lombok.*;

import java.util.List;

@JsonPropertyOrder({ "question_id", "title", "body", "imageUrls", "displayDate", "answerStatus", "answer" })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionDetailResponse {
    @JsonProperty("question_id")
    private Long qId;
    private String title;
    private String body;

    private List<String> imageUrls;

    private String displayDate;
    private String answerStatus;

    private AnswerDto answer;

    public static QuestionDetailResponse from(Question q) {
        if (q == null) throw new IllegalArgumentException("Question is null");

        String status = (q.getAnswer() != null) ? "답변완료" : "답변중";

        return QuestionDetailResponse.builder()
                .qId(q.getId())
                .title(q.getTitle())
                .body(q.getBody())
                .imageUrls(q.getImageKeys())
                .displayDate(DisplayDateUtil.toDisplay(q.getCreateDate()))
                .answerStatus(status)
                .answer(q.getAnswer() != null ? AnswerDto.from(q.getAnswer()) : null)
                .build();
    }
}

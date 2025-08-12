package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import hackathon.bigone.sunsak.accounts.mypage.entity.Notice;
import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import lombok.*;

import java.time.LocalDateTime;

@JsonPropertyOrder({ "notice_id", "title", "body", "displayDate", "isFixed" })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDto {
    @JsonProperty("notice_id")
    private Long noticeId;
    private String title;
    private String body;

    @JsonIgnore
    private LocalDateTime createDate;

    private String displayDate; //포맷 용
    private boolean isFixed; //게시판 고정

    public static NoticeDto from(Notice notice) {
        LocalDateTime date = notice.getCreateDate();

        return NoticeDto.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .body(notice.getBody())
                .createDate(date)
                .isFixed(notice.isFixed())
                .displayDate(DisplayDateUtil.toDisplay(date))
                .build();
    }

}

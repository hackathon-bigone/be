package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import hackathon.bigone.sunsak.accounts.mypage.entity.Notice;
import lombok.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    private LocalDate createDate;

    private String displayDate; //포맷 용
    private boolean isFixed; //게시판 고정

    public static NoticeDto from(Notice notice) {
        LocalDate date = notice.getCreateDate().toLocalDate();
        int currentYear = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear();

        String display = (date.getYear() == currentYear)
                ? date.format(DateTimeFormatter.ofPattern("M월 d일"))
                : date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));

        return NoticeDto.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .body(notice.getBody())
                .createDate(date)
                .isFixed(notice.isFixed())
                .displayDate(display)
                .build();
    }

}

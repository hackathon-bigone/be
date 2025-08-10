package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import hackathon.bigone.sunsak.accounts.mypage.entity.Notice;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDto {
    private String title;
    private String body;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy년 MM월 dd일")
    private LocalDate createDate;

    private boolean isFixed; //게시판 고정

    public static NoticeDto from(Notice notice) {
        return new NoticeDto(
                notice.getTitle(),
                notice.getBody(),
                notice.getCreateDate(),
                notice.isFixed()
        );
    }
}

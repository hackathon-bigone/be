package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.*;
import hackathon.bigone.sunsak.accounts.mypage.enums.ReportTarget;
import hackathon.bigone.sunsak.accounts.mypage.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@JsonPropertyOrder({ "report_id", "title", "target", "type", "postLink" ,"body", "createDate" })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportDto {
    @JsonProperty("report_id")
    private Long reportId;

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotNull
    @Getter(AccessLevel.NONE) //충돌 방지
    private ReportTarget target;

    @NotNull
    @Getter(AccessLevel.NONE) //충돌 방지
    private ReportType type;

    private String postLink;

    @NotBlank
    private String body;

    private List<String> imageKeys;
    private LocalDateTime createDate;

    //직렬화 - 한글로
    @JsonGetter("target")
    public String getTargetDisplay() {
        return target != null ? target.getDisplayName() : null;
    }

    //요청에서 영어로 받음
    @JsonSetter("target")
    public void setTargetFromJson(ReportTarget target) {
        this.target = target;
    }

    @JsonGetter("type")
    public String getTypeDisplay() {
        return type != null ? type.getDisplayName() : null;
    }

    @JsonSetter("type")
    public void setTypeFromJson(ReportType type) { // 요청에서 "INSULT" 등 enum 이름 받음
        this.type = type;
    }

    @JsonIgnore
    public ReportTarget getTargetEnum() { return target; }

    @JsonIgnore
    public ReportType getTypeEnum() { return type; }
}

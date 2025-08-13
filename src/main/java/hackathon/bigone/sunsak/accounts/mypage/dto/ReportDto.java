package hackathon.bigone.sunsak.accounts.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    private ReportTarget target;

    @NotNull
    private ReportType type;

    private String postLink;

    @NotBlank
    private String body;

    private List<String> imageUrls;
    private LocalDateTime createDate;
}

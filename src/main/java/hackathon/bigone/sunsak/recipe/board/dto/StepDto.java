package hackathon.bigone.sunsak.recipe.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class StepDto {
    private Long stepId;
    private int stepNumber;
    private String stepDescription;

    // DB에 저장되는 URL
    private String stepImageUrl;

    // 업로드용 파일
    private MultipartFile stepImageFile;
}

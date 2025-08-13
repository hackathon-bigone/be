package hackathon.bigone.sunsak.recipe.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class StepRequestDto {
    private int stepNumber;
    private String stepDescription;
    private MultipartFile stepImageFile;
    private String stepImageUrl;
}
package hackathon.bigone.sunsak.recipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StepDto {
    private Long stepId;
    private int stepNumber;
    private String stepDescription;
    private String stepImageUrl;
}

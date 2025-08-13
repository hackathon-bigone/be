package hackathon.bigone.sunsak.recipe.board.dto;

import hackathon.bigone.sunsak.recipe.board.entity.Step;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StepResponseDto {
    private Long stepId;
    private int stepNumber;
    private String stepDescription;
    private String stepImageUrl;

    public StepResponseDto(Step step) {
        this.stepId = step.getStepId();
        this.stepNumber = step.getStepNumber();
        this.stepDescription = step.getStepDescription();
        this.stepImageUrl = step.getStepImageUrl();
    }
}
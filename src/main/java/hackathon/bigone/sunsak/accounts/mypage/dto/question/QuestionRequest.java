package hackathon.bigone.sunsak.accounts.mypage.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {
    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    private String body;
}

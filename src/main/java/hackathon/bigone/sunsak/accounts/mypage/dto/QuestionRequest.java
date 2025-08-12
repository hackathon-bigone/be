package hackathon.bigone.sunsak.accounts.mypage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    @Size(max = 4) // 이미지 최대 4장
    private List<String> imageUrls;
}

package hackathon.bigone.sunsak.global.aws.s3.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PresignUploadRequest {
    @NotBlank
    private String filename;

    @NotBlank
    private String contentType;
}

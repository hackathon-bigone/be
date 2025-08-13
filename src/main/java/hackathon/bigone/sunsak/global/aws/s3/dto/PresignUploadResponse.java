package hackathon.bigone.sunsak.global.aws.s3.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignUploadResponse {
    private String key;
    private String putUrl;
    private String contentType;
    private long expiresInSec;
}

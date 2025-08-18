package hackathon.bigone.sunsak.global.aws.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresignPreviewResponse {
    private String key;
    private String getUrl;
    private long expiresInSec;
}

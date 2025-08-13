package hackathon.bigone.sunsak.global.aws.s3.service;

import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadRequest;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignUploadService {

    private final S3Uploader s3Uploader;

    private String extOf(String filename) { //확장자
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return (i >= 0 && i < filename.length() - 1) ? filename.substring(i + 1) : "";
    }

    private void validateImageMime(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    public List<PresignUploadResponse> issuePresigned(
            String prefix, Long userId,
            List<PresignUploadRequest> reqList,
            Duration ttl
    ) {
        if (reqList == null || reqList.isEmpty()) return List.of();

        if (List.of("qna", "report").contains(prefix) && reqList.size() > 4) {
            throw new IllegalArgumentException(prefix + " 이미지는 최대 4장까지 업로드 가능합니다.");
        }

        List<PresignUploadResponse> result = new ArrayList<>();

        for (PresignUploadRequest r : reqList) {
            validateImageMime(r.getContentType());
            String ext = extOf(r.getFilename());
            if (ext.isBlank()) ext = "bin";

            // prefix + userId + uuid.ext
            String key = prefix + "/" + userId + "/" + UUID.randomUUID() + "." + ext;

            URL putUrl = s3Uploader.presignedPutUrl(key, ttl, r.getContentType());

            result.add(PresignUploadResponse.builder()
                    .key(key)
                    .putUrl(putUrl.toString())
                    .contentType(r.getContentType())
                    .expiresInSec(ttl.toSeconds())
                    .build());
        }
        return result;
    }
}


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

    public String createGetUrl(String key, Duration ttl) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (key.charAt(0) == '/') key = key.substring(1);

        // TTL 기본값 + 7일 클램프
        Duration effective = (ttl == null || ttl.isZero() || ttl.isNegative())
                ? Duration.ofMinutes(360)
                : ttl;
        if (effective.compareTo(Duration.ofDays(7)) > 0) {
            effective = Duration.ofDays(7);
        }

        return s3Uploader.presignedGetUrl(key, effective).toString();
    }

    //수정
    public String createPutUrlForExistingKey(String key, String contentType, Duration ttl) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key는 필수 값입니다.");
        validateImageMime(contentType);

        // TTL 기본값 + 7일 클램프
        Duration effective = (ttl == null || ttl.isZero() || ttl.isNegative())
                ? Duration.ofMinutes(360)
                : ttl;
        if (effective.compareTo(Duration.ofDays(7)) > 0) {
            effective = Duration.ofDays(7);
        }
        return s3Uploader.presignedPutUrl(
                key.charAt(0) == '/' ? key.substring(1) : key,
                effective,
                contentType
        ).toString();
    }
}


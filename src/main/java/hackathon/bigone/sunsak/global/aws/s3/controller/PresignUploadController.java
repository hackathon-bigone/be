package hackathon.bigone.sunsak.global.aws.s3.controller;

import hackathon.bigone.sunsak.global.aws.s3.dto.PresignPreviewResponse;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadRequest;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadResponse;
import hackathon.bigone.sunsak.global.aws.s3.service.PresignUploadService;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class PresignUploadController {

    private final PresignUploadService presignUploadService;
    private static final Set<String> ALLOWED_PREFIX =
            Set.of("qna", "recipe", "groupbuy", "report");
    private static final Set<String> PUBLIC_PREFIX =
            Set.of("recipe", "groupbuy"); //공개 - 썸네일

    //업로드용
    @PostMapping(value = "/{prefix}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PresignUploadResponse>> issuePresigned( //사진 업로드
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable String prefix,
            @Valid @RequestBody List<PresignUploadRequest> reqList
    ) {
        if (userDetail == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (!ALLOWED_PREFIX.contains(prefix)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Duration ttl = Duration.ofMinutes(360); // 만료 시간
        List<PresignUploadResponse> res =
                presignUploadService.issuePresigned(prefix, userDetail.getId(), reqList, ttl);

        return ResponseEntity.ok(res);
    }

    //미리보기용: GET presigned URL 발급
    @PostMapping(value = "/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PresignPreviewResponse>> issuePreviewBatch(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody List<String> keys
    ) {
        if (userDetail == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (keys == null || keys.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // key 검증 (prefix)
        for (String key : keys) {
            String prefix = key.split("/", 2)[0];
            if (!ALLOWED_PREFIX.contains(prefix)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        Duration ttl = Duration.ofMinutes(360);
        List<PresignPreviewResponse> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            String url = presignUploadService.createGetUrl(key, ttl);
            result.add(new PresignPreviewResponse(key, url, ttl.toSeconds()));
        }
        return ResponseEntity.ok(result);
    }

    //미리보기 - 인증해야함
    @GetMapping("/preview")
    public ResponseEntity<PresignPreviewResponse> previewOne(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam String key
    ) {
        if (userDetail == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (key == null || key.isBlank()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        // prefix 검증
        String prefix = key.split("/", 2)[0];
        if (!ALLOWED_PREFIX.contains(prefix)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Duration ttl = Duration.ofMinutes(360);
        String url = presignUploadService.createGetUrl(key, ttl);
        return ResponseEntity.ok(
                PresignPreviewResponse.builder()
                        .key(key)
                        .getUrl(url)
                        .expiresInSec(ttl.toSeconds())
                        .build()
        );
    }

    @GetMapping("/r")
    public ResponseEntity<Void> redirectPublic(@RequestParam("key") String key) {
        if (key == null || key.isBlank()) return ResponseEntity.badRequest().build();

        String p = key.split("/", 2)[0];
        if (!PUBLIC_PREFIX.contains(p)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        var ttl = Duration.ofMinutes(360);
        String url = presignUploadService.createGetUrl(key, ttl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

}


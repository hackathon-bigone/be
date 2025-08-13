package hackathon.bigone.sunsak.global.aws.s3.controller;

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

import java.time.Duration;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class PresignUploadController {

    private final PresignUploadService presignUploadService;

    @PostMapping(value = "/{prefix}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PresignUploadResponse>> issuePresigned( //사진 업로드
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable String prefix,
            @Valid @RequestBody List<PresignUploadRequest> reqList
    ) {
        if (userDetail == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        // prefix 허용 목록 검증
        List<String> allowed = List.of("qna", "recipe", "groupby", "report");
        if (!allowed.contains(prefix)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Duration ttl = Duration.ofMinutes(15); // 만료 시간
        List<PresignUploadResponse> res =
                presignUploadService.issuePresigned(prefix, userDetail.getId(), reqList, ttl);

        return ResponseEntity.ok(res);
    }
}


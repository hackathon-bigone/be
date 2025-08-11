package hackathon.bigone.sunsak.global.aws.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final S3ServiceV1 s3Service;

    @PostMapping("/{type}") //어느파일에 무슨 이미지 넣을지, qna, recipe, groupby(공동구매), report(신고)
    public ResponseEntity<S3ServiceV1.UploadResult> upload(
            @PathVariable String type,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(s3Service.upload(type, file));
    }
}

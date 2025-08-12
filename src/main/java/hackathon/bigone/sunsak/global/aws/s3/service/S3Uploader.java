package hackathon.bigone.sunsak.global.aws.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 s3; // S3Config에서 주입
    private String bucket = "sunsak-bucket-1"; //bucket 이름

    //S3에 저장할 파일 경로를 만드는 것
    //key 생성: {prefix}/{uuid}[.ext]
    //prefix(s3에 지정된 폴더 경로) - qna, recipe, groupby, report
    public String newKey(String prefix, @Nullable String originalFilename) {
        String safePrefix = (prefix == null) ? "" : prefix.replaceAll("^/+", "").replaceAll("/+$", "");
        String ext = ""; //파일 확장자
        if (originalFilename != null) {
            int i = originalFilename.lastIndexOf('.');
            if (i >= 0 && i < originalFilename.length() - 1) ext = originalFilename.substring(i); //확장자 추출 ex) .jpg
        }

        //key 생성
        return (safePrefix.isEmpty() ? "" : safePrefix + "/") + UUID.randomUUID() + ext;
    }

    // 하나씩 업로드 -> HTTP에서 여러개를 요청 받아도 S3 자체에서 객체(사진)을 하나씩 업로드한다
    public String uploadOne(String key, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("empty file");

        ObjectMetadata meta = new ObjectMetadata(); //파일 정보 설정을 위한 것
        meta.setContentType(file.getContentType());
        meta.setContentLength(file.getSize());

        try (var in = file.getInputStream()) {
            s3.putObject(bucket, key, in, meta); //s3에 업로드
        }
        return key; //반환된 key를 DB에 저장
    }

    // service에서 사용하면 될 메소드
    public String uploadOneUnder(String prefix, MultipartFile file) throws IOException {
        String key = newKey(prefix, file != null ? file.getOriginalFilename() : null); //키 생성
        return uploadOne(key, file); // s3 업로드 실행하고 key 반환
    }

    /** 객체 삭제 */
    public void delete(String key) {
        s3.deleteObject(bucket, key);
    }

    /** prefix 하위 전부 삭제 (필요 시 사용) */
    public void deleteAllUnder(String prefix) {
        String safePrefix = (prefix == null) ? "" : prefix.replaceAll("^/+", "");
        String contToken = null;
        do {
            ListObjectsV2Result r = s3.listObjectsV2(new ListObjectsV2Request()
                    .withBucketName(bucket)
                    .withPrefix(safePrefix)
                    .withContinuationToken(contToken));
            r.getObjectSummaries().forEach(s -> s3.deleteObject(bucket, s.getKey()));
            contToken = r.getNextContinuationToken();
        } while (contToken != null);
    }

    // 조회용 프리사인 URL (GET)  S3 객체 읽기용
    public URL presignedGetUrl(String key, Duration ttl) {
        Date exp = new Date(System.currentTimeMillis() + ttl.toMillis());
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(exp);
        return s3.generatePresignedUrl(req);
    }

    /** 업로드용 프리사인 URL (PUT) — 클라이언트 직접 업로드 시 사용 */
    public URL presignedPutUrl(String key, Duration ttl, @Nullable String contentType) {
        Date exp = new Date(System.currentTimeMillis() + ttl.toMillis());
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(exp);
        if (contentType != null) {
            // PUT 프리사인 사용 시, 클라이언트가 같은 Content-Type 헤더로 업로드해야 함
            req.addRequestParameter("Content-Type", contentType);
        }
        return s3.generatePresignedUrl(req);
    }
}

package hackathon.bigone.sunsak.global.aws.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceV1 {

    private final AmazonS3 s3;
    private final String bucket = "sunsak-bucket-1";
    private final Set<String> allowed = Set.of("qna","recipe","groupbuy","report");

    public UploadResult upload(String type, MultipartFile file) {
        if (!allowed.contains(type)) throw new IllegalArgumentException("허용되지 않은 경로");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("빈 파일");

        String orig = file.getOriginalFilename();
        String ext = (orig != null && orig.contains(".")) ? orig.substring(orig.lastIndexOf('.')) : "";
        String key = type + "/" + UUID.randomUUID() + ext;

        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(file.getContentType());
            meta.setContentLength(file.getSize());

            // 업로드 (ACL 필요 없으면 빼도 됨. 버킷은 퍼블릭 차단 권장)
            s3.putObject(bucket, key, file.getInputStream(), meta);
            // s3.setObjectAcl(bucket, key, CannedAccessControlList.Private); // 기본이 Private

            // 미리보기용 프리사인 URL (15분)
            Date expire = new Date(System.currentTimeMillis() + 15 * 60 * 1000);
            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(bucket, key)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expire);
            URL url = s3.generatePresignedUrl(req);

            return new UploadResult(key, file.getContentType(), file.getSize(), url.toString());
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
    }

    public record UploadResult(String key, String contentType, long size, String previewUrl) {}
}


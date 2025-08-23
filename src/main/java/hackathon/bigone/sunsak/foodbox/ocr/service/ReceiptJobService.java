package hackathon.bigone.sunsak.foodbox.ocr.service;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodItemResponse;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import hackathon.bigone.sunsak.foodbox.ocr.support.InMemoryMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptJobService {

    private final OcrService ocrService;
    private final OcrReceiptService ocrReceiptService;
    private final StringRedisTemplate redis;

    private static final String KEY = "receiptjob:";                 // receiptjob:{jobId}:meta, :data
    private static final Duration TTL = Duration.ofMinutes(15);

    public void createJob(String jobId) {
        JSONObject meta = new JSONObject()
                .put("status", "PENDING")
                .put("progress", 0);
        redis.opsForValue().set(KEY + jobId + ":meta", meta.toString(), TTL);
    }

    public Map<String, String> readMeta(String jobId) {
        String raw = redis.opsForValue().get(KEY + jobId + ":meta");
        if (raw == null) return null;
        JSONObject o = new JSONObject(raw);
        Map<String, String> m = new HashMap<>();
        for (String k : o.keySet()) m.put(k, String.valueOf(o.get(k)));
        return m;
    }

    public List<FoodItemResponse> readData(String jobId) {
        String raw = redis.opsForValue().get(KEY + jobId + ":data");
        if (raw == null) return Collections.emptyList();
        JSONArray arr = new JSONArray(raw);
        List<FoodItemResponse> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject it = arr.getJSONObject(i);
            out.add(new FoodItemResponse(it.getString("name"), it.getInt("quantity")));
        }
        return out;
    }

    @Async("ocrExecutor") // 아래 AsyncConfig에 정의
    public void processAsync(String jobId, byte[] bytes, String originalFilename, String contentType) {
        try {
            update(jobId, "RUNNING", 10, null);

            MultipartFile safeFile = new InMemoryMultipartFile(
                    "file",
                    originalFilename != null ? originalFilename : "receipt.jpg",
                    contentType != null ? contentType : "application/octet-stream",
                    bytes
            );

            // OCR 호출 (느린 구간)
            List<OcrExtractedItem> rawItems = ocrService.extractItemNamesFromImage(safeFile);
            update(jobId, "RUNNING", 60, null);

            // 집계/정규화 (저장 X, 화면용 출력만)
            Map<String, Integer> aggregated = ocrReceiptService.showOnlyOutput(rawItems);

            // JSON 저장 (name, quantity만)
            JSONArray data = new JSONArray(
                    aggregated.entrySet().stream()
                            .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                            .map(e -> new JSONObject()
                                    .put("name", e.getKey().trim())
                                    .put("quantity", e.getValue()))
                            .collect(Collectors.toList())
            );
            redis.opsForValue().set(KEY + jobId + ":data", data.toString(), TTL);

            update(jobId, "DONE", 100, null);
        } catch (Exception e) {
            log.error("OCR job failed: {}", jobId, e);
            update(jobId, "FAILED", 100, e.getMessage());
        }
    }

    private void update(String jobId, String status, int progress, String error) {
        JSONObject meta = new JSONObject()
                .put("status", status)
                .put("progress", progress);
        if (error != null) meta.put("error", error);
        redis.opsForValue().set(KEY + jobId + ":meta", meta.toString(), TTL);
    }
}
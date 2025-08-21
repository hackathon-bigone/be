package hackathon.bigone.sunsak.foodbox.ocr.controller;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodItemResponse;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import hackathon.bigone.sunsak.foodbox.ocr.service.OcrReceiptService;
import hackathon.bigone.sunsak.foodbox.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/foodbox/receipt")
@RequiredArgsConstructor
public class OcrController {
    private final OcrService ocrService;
    private final OcrReceiptService ocrReceiptService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FoodItemResponse>> sendReceiptOcr(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        // OCR 호출
        List<OcrExtractedItem> rawItems = ocrService.extractItemNamesFromImage(file);
        if (rawItems == null || rawItems.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        //저장 x
        Map<String, Integer> aggregated = ocrReceiptService.showOnlyOutput(rawItems);

        List<FoodItemResponse> result = aggregated.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .map(e -> new FoodItemResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return ResponseEntity.ok()
                    .header("X-App-Message", "영수증을 인식하지 못했어요. 다시 촬영해 주세요.")
                    .body(Collections.emptyList());
        }

        log.debug("OCR upload result: {}",
                result.stream()
                        .map(it -> String.format("{name=%s, qty=%d}", it.getName(), it.getQuantity()))
                        .toList());

        return ResponseEntity.ok(result);
    }
}

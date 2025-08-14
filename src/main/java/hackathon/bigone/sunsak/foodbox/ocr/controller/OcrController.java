package hackathon.bigone.sunsak.foodbox.ocr.controller;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodItemResponse;
import hackathon.bigone.sunsak.foodbox.nlp.service.NlpService;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import hackathon.bigone.sunsak.foodbox.ocr.service.OcrNomalizationService;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/foodbox/receipt")
@RequiredArgsConstructor
public class OcrController {
    private final OcrService ocrService;
    private final NlpService nlpService;
    private final OcrNomalizationService ocrNomalizationService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FoodItemResponse>> sendReceiptOcr(@RequestParam("file") MultipartFile file) throws Exception {
        // OCR 호출
        List<OcrExtractedItem> rawItems = ocrService.extractItemNamesFromImage(file);
        if (rawItems == null || rawItems.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Komoran 분석 → user_dict 그룹 / 자유명사 그룹 분리
        NlpService.ClassifiedTokens classified = nlpService.classifyByUserDict(rawItems);
        Map<String, Integer> userDictGroup = classified.getUserDict();   // user_dict로 잡힌 토큰 (그대로 사용)
        Map<String, Integer> freeNounGroup = classified.getFreeNouns();  // user_dict에 없는 명사

        // 자유명사만 Redis keyword 매핑
        Map<String, String> mappedFree = ocrNomalizationService.normalizeFreeNouns(freeNounGroup.keySet());

        // 최종명 기준 수량 합산
        Map<String, Integer> aggregated = new LinkedHashMap<>();

        // user_dict 그룹: 표준명 = 그대로
        for (var e : userDictGroup.entrySet()) {
            aggregated.merge(e.getKey().trim(), Math.max(1, e.getValue()), Integer::sum);
        }

        // 자유명사 그룹: keyword 매핑 성공한 것만 사용
        for (var e : freeNounGroup.entrySet()) {
            String mapped = mappedFree.get(e.getKey());
            if (mapped == null || mapped.isBlank()) continue; // 매핑 실패 → 버림
            aggregated.merge(mapped.trim(), Math.max(1, e.getValue()), Integer::sum);
        }

        // 응답 변환
        List<FoodItemResponse> result = aggregated.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank()) //name 없는 항목 제거
                .map(e -> new FoodItemResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return ResponseEntity.ok()
                    .header("X-App-Message", "영수증을 인식하지 못했어요. 다시 촬영해 주세요.")
                    .body(Collections.emptyList());
        }

        log.debug("OCR upload result: {}", result);
        return ResponseEntity.ok(result);
    }
}

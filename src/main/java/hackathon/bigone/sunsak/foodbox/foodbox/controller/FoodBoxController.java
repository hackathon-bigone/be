package hackathon.bigone.sunsak.foodbox.foodbox.controller;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodItemRequest;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodListResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.update.FoodItemBatchUpdateRequest;
import hackathon.bigone.sunsak.foodbox.foodbox.service.FoodBoxService;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/foodbox")
public class FoodBoxController {
    private final FoodBoxService foodBoxService;

    @PostMapping("/ocr/save") //영수증 인식 입력
    public ResponseEntity<List<FoodBoxResponse>> saveFoodsWithOCR(
            @RequestBody List<OcrExtractedItem> items,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userDetail.getId(); // JWT 인증 기반
        List<FoodBoxResponse> savedFoods = foodBoxService.saveFromOcr(userId, items);
        return ResponseEntity.ok(savedFoods);
    }

    //사용자 직접 입력
    @PostMapping("/save")
    public ResponseEntity<List<FoodBoxResponse>> saveFoods(
            @RequestBody List<FoodItemRequest> items,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if (userDetail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userDetail.getId();

        for (int i = 0; i < items.size(); i++) {
            FoodItemRequest it = items.get(i);
            if (it == null || it.getName() == null || it.getName().isBlank()) {
                return ResponseEntity.badRequest().body(null);
            }
            if (it.getExpiryDate() == null) {
                // 어떤 라인에서 빠졌는지 알려주기
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "expiryDate is required at index " + i);
            }
        }

        return ResponseEntity.ok(foodBoxService.saveFoods(userId, items));
    }

    //all - 모두 , imminent- 임박날짜
    @GetMapping("") //로그인한 사용자의 식품 목록 보여주기
    public ResponseEntity<FoodListResponse> getFoods(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "7") Integer days
    ){
        if (userDetail == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (days < 1) days = 7; // 안전장치

        Long userId = userDetail.getId();

        FoodListResponse resp = foodBoxService.getFoodsByUser(userId, filter, days);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/update") //수정
    public ResponseEntity<List<FoodBoxResponse>> modifyFoods(
            //@RequestParam(defaultValue = "false") boolean dryRun, //미리보기
            @RequestBody FoodItemBatchUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if (userDetail == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Long userId = userDetail.getUser().getId();
        foodBoxService.batchUpdate(userId, req.getItems());
        return ResponseEntity.ok(foodBoxService.getFoodsByUserList(userId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFoods(
            @RequestBody List<Long> foodIds, //근데 id 여러개 삭제도 가능하게 할건데 List<Long> foodId 해야하나
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if (userDetail == null || userDetail.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userDetail.getUser().getId();

        if(foodBoxService.delete(userId, foodIds)){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

}

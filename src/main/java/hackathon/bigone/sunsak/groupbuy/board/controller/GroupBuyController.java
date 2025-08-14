package hackathon.bigone.sunsak.groupbuy.board.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadRequest;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadResponse;
import hackathon.bigone.sunsak.global.aws.s3.service.PresignUploadService;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyRequestDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.service.GroupBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/groupbuys")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final PresignUploadService presignUploadService;

    @PostMapping
    public ResponseEntity<GroupbuyResponseDto> createGroupbuy(
            @RequestBody @Valid GroupbuyRequestDto groupbuyRequestDto,
            @AuthenticationPrincipal SiteUser user) {

        GroupbuyResponseDto createdGroupbuy = groupBuyService.create(groupbuyRequestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupbuy);
    }

    //전체조회
    @GetMapping
    public ResponseEntity<List<GroupbuyResponseDto>> getAllGroupbuys(
            @RequestParam(defaultValue = "recent") String sort) {

        List<GroupbuyResponseDto> groupbuys = groupBuyService.findAllGroupbuys(sort);
        return ResponseEntity.ok(groupbuys);
    }

    //상세조회
    @GetMapping("/{groupbuyId}")
    public ResponseEntity<GroupbuyResponseDto> getGroupbuy(@PathVariable Long groupbuyId) {
        GroupbuyResponseDto groupbuy = groupBuyService.findGroupbuyById(groupbuyId);
        return ResponseEntity.ok(groupbuy);
    }

    //수정
    @PutMapping("/{groupbuyId}")
    public ResponseEntity<GroupbuyResponseDto> updateGroupbuy(
            @PathVariable Long groupbuyId,
            @RequestBody @Valid GroupbuyRequestDto groupbuyRequestDto,
            @AuthenticationPrincipal SiteUser user) {

        GroupbuyResponseDto updatedGroupbuy = groupBuyService.update(groupbuyId, groupbuyRequestDto, user);
        return ResponseEntity.ok(updatedGroupbuy);
    }

    @DeleteMapping("/{groupbuyId}")
    public ResponseEntity<Void> deleteGroupbuy(
            @PathVariable Long groupbuyId,
            @AuthenticationPrincipal SiteUser user) {

        groupBuyService.delete(groupbuyId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupbuyId}/scrap")
    public ResponseEntity<Void> scrapGroupbuy(
            @PathVariable Long groupbuyId,
            @AuthenticationPrincipal SiteUser user) {

        groupBuyService.scrap(groupbuyId, user);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/uploads/{prefix}")
    public ResponseEntity<Object> getPresignedUrls(
            @PathVariable String prefix,
            @RequestBody List<PresignUploadRequest> reqList,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getUser().getId();
        Duration ttl = Duration.ofMinutes(10);

        try {
            List<PresignUploadResponse> response = presignUploadService.issuePresigned(prefix, userId, reqList, ttl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Pre-signed URL 발급 중 오류 발생", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
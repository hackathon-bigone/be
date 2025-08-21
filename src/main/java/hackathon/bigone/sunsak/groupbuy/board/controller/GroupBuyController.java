package hackathon.bigone.sunsak.groupbuy.board.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.global.aws.s3.service.PresignUploadService;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyListResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyRequestDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.service.GroupBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final UserRepository userRepository;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<GroupbuyResponseDto> createGroupbuy(
            @RequestBody @Valid GroupbuyRequestDto groupbuyRequestDto,
            @AuthenticationPrincipal CustomUserDetail userDetail) {

        if (userDetail == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        SiteUser user = userDetail.getUser();

        GroupbuyResponseDto createdGroupbuy = groupBuyService.create(groupbuyRequestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroupbuy);
    }

    //전체조회
    @GetMapping
    public ResponseEntity<GroupbuyListResponseDto> getAllGroupbuys(
            @RequestParam(defaultValue = "recent", required = false) String sort) {

        GroupbuyListResponseDto response = groupBuyService.findAllGroupbuys(sort);
        return ResponseEntity.ok(response);
    }

    // 상세조회
    @GetMapping("/{groupbuyId}")
    public ResponseEntity<GroupbuyResponseDto> getGroupbuy(@PathVariable Long groupbuyId) {
        GroupbuyResponseDto groupbuy = groupBuyService.findGroupbuyById(groupbuyId);
        return ResponseEntity.ok(groupbuy);
    }

    // 수정
    @PutMapping("/{groupbuyId}")
    public ResponseEntity<GroupbuyResponseDto> updateGroupbuy(
            @PathVariable Long groupbuyId,
            @RequestBody @Valid GroupbuyRequestDto groupbuyRequestDto,
            @AuthenticationPrincipal CustomUserDetail userDetail) {

        if (userDetail == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        SiteUser user = userDetail.getUser();
        GroupbuyResponseDto updatedGroupbuy = groupBuyService.update(groupbuyId, groupbuyRequestDto, user);
        return ResponseEntity.ok(updatedGroupbuy);
    }

    // 삭제
    @DeleteMapping("/{groupbuyId}")
    public ResponseEntity<Void> deleteGroupbuy(
            @PathVariable Long groupbuyId,
            @AuthenticationPrincipal CustomUserDetail userDetail) {

        if (userDetail == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        SiteUser user = userDetail.getUser();
        groupBuyService.delete(groupbuyId, user);
        return ResponseEntity.noContent().build();
    }

    // 스크랩
    @PostMapping("/{groupbuyId}/scrap")
    public ResponseEntity<String> toggleScrap(
            @PathVariable Long groupbuyId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();
        groupBuyService.toggleScrap(groupbuyId, currentUser);
        return ResponseEntity.ok("스크랩 상태가 변경되었습니다.");
    }

    // 검색
    @GetMapping("/search")
    public ResponseEntity<GroupbuyListResponseDto> searchGroupbuys(
            @RequestParam String keyword,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {

        GroupbuyListResponseDto groupbuys = groupBuyService.searchGroupbuysByTitle(keyword, pageable);
        return ResponseEntity.ok(groupbuys);
    }
}
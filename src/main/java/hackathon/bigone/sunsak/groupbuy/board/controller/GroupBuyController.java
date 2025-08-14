package hackathon.bigone.sunsak.groupbuy.board.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyRequestDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.service.GroupBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groupbuys")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService groupBuyService;

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
}
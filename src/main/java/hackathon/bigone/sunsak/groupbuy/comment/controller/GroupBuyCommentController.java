package hackathon.bigone.sunsak.groupbuy.comment.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.groupbuy.comment.dto.GroupBuyCommentRequestDto;
import hackathon.bigone.sunsak.groupbuy.comment.dto.GroupBuyCommentResponseDto;
import hackathon.bigone.sunsak.groupbuy.comment.service.GroupBuyCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/groupbuys/{groupbuyId}/comments")
@RestController
public class GroupBuyCommentController {

    private final GroupBuyCommentService groupBuyCommentService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<GroupBuyCommentResponseDto> addComment(
            @PathVariable Long groupbuyId,
            @RequestBody @Valid GroupBuyCommentRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetail userDetail) {

        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        SiteUser user = userDetail.getUser();
        GroupBuyCommentResponseDto newComment = groupBuyCommentService.addComment(groupbuyId, requestDto, user);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    // 댓글 조회
    @GetMapping
    public ResponseEntity<List<GroupBuyCommentResponseDto>> getComments(@PathVariable Long groupbuyId) {
        List<GroupBuyCommentResponseDto> comments = groupBuyCommentService.getComments(groupbuyId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long groupbuyId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetail userDetail) {

        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        SiteUser user = userDetail.getUser();
        groupBuyCommentService.deleteComment(groupbuyId, commentId, user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 댓글 개수 조회
    @GetMapping("/count")
    public ResponseEntity<Long> countComments(@PathVariable Long groupbuyId) {
        long count = groupBuyCommentService.countComments(groupbuyId);
        return ResponseEntity.ok(count);
    }
}

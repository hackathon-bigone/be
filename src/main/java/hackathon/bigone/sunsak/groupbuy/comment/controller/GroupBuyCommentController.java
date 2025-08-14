package hackathon.bigone.sunsak.groupbuy.comment.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
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


@RequestMapping("/groupbuys/{groupbuyId}/comments")
@RequiredArgsConstructor
@RestController
public class GroupBuyCommentController {

    private final GroupBuyCommentService GroupBuyCommentService;

    //댓글 작성
    @PostMapping
    public ResponseEntity<GroupBuyCommentResponseDto> addComment(
            @PathVariable Long groupbuyId,
            @RequestBody @Valid GroupBuyCommentRequestDto requestDto,
            @AuthenticationPrincipal SiteUser user) {

        GroupBuyCommentResponseDto newComment = GroupBuyCommentService.addComment(groupbuyId, requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    //댓글 조회
    @GetMapping
    public ResponseEntity<List<GroupBuyCommentResponseDto>> getComments(
            @PathVariable Long groupbuyId) {

        List<GroupBuyCommentResponseDto> comments = GroupBuyCommentService.getComments(groupbuyId);
        return ResponseEntity.ok(comments);
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long groupbuyId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal SiteUser user) {

        GroupBuyCommentService.deleteComment(groupbuyId, commentId, user);
        return ResponseEntity.noContent().build();
    }
}
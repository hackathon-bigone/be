package hackathon.bigone.sunsak.recipe.comment.controller;

import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentRequestDto;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentResponseDto;
import hackathon.bigone.sunsak.recipe.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardPostId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long boardPostId,
            @RequestBody CommentRequestDto requestDto, // 👈 DTO로 변경
            @AuthenticationPrincipal CustomUserDetail userDetail // 👈 올바른 인증 방식
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CommentResponseDto response = commentService.addComment(boardPostId, requestDto, userDetail.getUser());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CommentResponseDto> getComments(@PathVariable Long boardPostId){
        // 서비스 메서드에서 반환하는 DTO도 CommentResponseDto로 일관성 있게 변경하는 것이 좋습니다.
        return commentService.getComments(boardPostId);
    }
}
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


@RequiredArgsConstructor
@RequestMapping("/recipe/{boardPostId}/comments")
@RestController("recipeCommentController")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long boardPostId,
            @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CommentResponseDto response = commentService.addComment(boardPostId, requestDto, userDetail.getUser());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<CommentResponseDto> getComments(@PathVariable Long boardPostId){
        return commentService.getComments(boardPostId);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long boardPostId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        commentService.deleteComment(boardPostId, commentId, userDetail.getUser());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
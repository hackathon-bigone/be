package hackathon.bigone.sunsak.recipe.comment.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentDto;
import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import hackathon.bigone.sunsak.recipe.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardPostId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommentDto addComment(@PathVariable Long boardPostId,
                                 @RequestBody String content,
                                 @RequestAttribute SiteUser currentUser) {
        Comment comment = commentService.addComment(boardPostId, content, currentUser);

        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setBoardPostId(boardPostId);
        dto.setAuthorId(currentUser.getId());
        dto.setContent(comment.getContent());
        return dto;
    }

    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long boardPostId){
        return commentService.getComments(boardPostId);
    }
}

package hackathon.bigone.sunsak.groupbuy.comment.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import hackathon.bigone.sunsak.groupbuy.board.repository.GroupBuyRepository;
import hackathon.bigone.sunsak.groupbuy.comment.dto.GroupBuyCommentRequestDto;
import hackathon.bigone.sunsak.groupbuy.comment.dto.GroupBuyCommentResponseDto;
import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import hackathon.bigone.sunsak.groupbuy.comment.repository.GroupBuyCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupBuyCommentService {
    private final GroupBuyCommentRepository groupbuyCommentRepository;
    private final GroupBuyRepository groupBuyRepository;

    @Transactional
    public GroupBuyCommentResponseDto addComment(Long groupbuyId, GroupBuyCommentRequestDto requestDto, SiteUser author) {
        Groupbuy groupbuy = groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("Groupbuy not found with id: " + groupbuyId));

        GroupBuyComment parentGroupBuyComment = null;
        if (requestDto.getParentId() != null) {
            parentGroupBuyComment = groupbuyCommentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found with id: " + requestDto.getParentId()));
        }

        GroupBuyComment groupBuyComment = new GroupBuyComment();
        groupBuyComment.setContent(requestDto.getContent());
        groupBuyComment.setGroupbuy(groupbuy);
        groupBuyComment.setAuthor(author);
        groupBuyComment.setParent(parentGroupBuyComment);

        GroupBuyComment savedGroupBuyComment = groupbuyCommentRepository.save(groupBuyComment);
        return new GroupBuyCommentResponseDto(savedGroupBuyComment);
    }

    @Transactional(readOnly = true)
    public List<GroupBuyCommentResponseDto> getComments(Long groupbuyId) {
        List<GroupBuyComment> allGroupBuyComments = groupbuyCommentRepository.findByGroupbuy_GroupbuyId(groupbuyId);

        return allGroupBuyComments.stream()
                .filter(comment -> comment.getParent() == null)
                .map(GroupBuyCommentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long groupbuyId, Long commentId, SiteUser currentUser) {
        groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        GroupBuyComment groupBuyComment = groupbuyCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        if (!groupBuyComment.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 댓글을 삭제할 권한이 없습니다.");
        }
        groupbuyCommentRepository.delete(groupBuyComment);
    }
}
package hackathon.bigone.sunsak.groupbuy.comment.repository;

import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyCommentRepository extends JpaRepository<GroupBuyComment, Long> {

    // 공동구매 게시글 ID로 댓글 목록을 찾는 메서드
    List<GroupBuyComment> findByGroupbuy_GroupbuyId(Long groupbuyId);
    long countByGroupbuy_GroupbuyId(Long groupbuyId);
    List<GroupBuyComment> findByAuthor_Id(Long authorId);
    long countByAuthor_Id(Long authorId);
}
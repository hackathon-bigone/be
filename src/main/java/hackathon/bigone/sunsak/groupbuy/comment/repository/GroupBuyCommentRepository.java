package hackathon.bigone.sunsak.groupbuy.comment.repository;

import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyCommentRepository extends JpaRepository<GroupBuyComment, Long> {
    List<GroupBuyComment> findByGroupbuy_GroupbuyId(Long groupbuyId);
}
package hackathon.bigone.sunsak.groupbuy.comment.repository;

import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyCommentRepository extends JpaRepository<GroupBuyComment, Long> {
    @Query("SELECT c FROM GroupBuyComment c JOIN FETCH c.author WHERE c.groupbuy.groupbuyId = :groupbuyId")
    List<GroupBuyComment> findByGroupbuy_GroupbuyId(@Param("groupbuyId") Long groupbuyId);

}
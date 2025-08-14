package hackathon.bigone.sunsak.groupbuy.board.repository;

import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupBuyRepository extends JpaRepository<Groupbuy, Long> {
}
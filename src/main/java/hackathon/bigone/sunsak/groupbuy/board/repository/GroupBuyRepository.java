package hackathon.bigone.sunsak.groupbuy.board.repository;

import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyRepository extends JpaRepository<Groupbuy, Long> {
    List<Groupbuy> findByGroupbuyTitleContaining(String keyword, Pageable pageable);
    List<Groupbuy> findByAuthor_Id(Long author);
    long countByAuthor_Id(Long author);
    long countByGroupbuyTitleContaining(String keyword);
}

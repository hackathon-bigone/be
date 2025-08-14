package hackathon.bigone.sunsak.groupbuy.board.repository;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupBuyScrapRepository extends JpaRepository<GroupBuyScrap, Long> {
    boolean existsByUserAndGroupbuy(SiteUser user, Groupbuy groupbuy);
    Optional<GroupBuyScrap> findByUserAndGroupbuy(SiteUser user, Groupbuy groupbuy);

}
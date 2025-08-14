package hackathon.bigone.sunsak.groupbuy.board.entity;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="group_buy_scraps")
public class GroupBuyScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scrapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupbuy_id")
    private Groupbuy groupbuy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SiteUser user;
}

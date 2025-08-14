package hackathon.bigone.sunsak.groupbuy.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "group_buy_links")
public class GroupBuyLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupbuyLinkId;

    @Column(nullable = false, length = 500)
    private String groupbuylinkUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_buy_id")
    private Groupbuy groupbuy;
}

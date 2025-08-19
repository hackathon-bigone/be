package hackathon.bigone.sunsak.groupbuy.board.entity;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.groupbuy.board.enums.GroupBuyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import hackathon.bigone.sunsak.groupbuy.comment.entity.GroupBuyComment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "GroupBuys")
@EntityListeners(AuditingEntityListener.class)
public class Groupbuy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupbuyId;

    @Column(nullable = false, length = 100)
    private String groupbuyTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String groupbuyDescription;

    @Column(nullable = true, length = 500)
    private String mainImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupBuyStatus status;

    // 공구 참여 인원
    private int groupbuyCount;

    // 공동구매 링크
    @OneToMany(mappedBy = "groupbuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyLink> buyLinks = new ArrayList<>();

    // 공동구매 스크랩 리스트
    @OneToMany(mappedBy = "groupbuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyScrap> scraps = new ArrayList<>();

    // 작성자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser author;

    private String authorUsername;

    //댓글
    @OneToMany(mappedBy = "groupbuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyComment> groupBuyComments = new ArrayList<>();


    @CreatedDate
    private LocalDateTime createDate;

}
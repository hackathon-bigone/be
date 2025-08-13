package hackathon.bigone.sunsak.recipe.comment.entity;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_post_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SiteUser author;

    @Column(nullable = false, length = 500)
    private String content;

}

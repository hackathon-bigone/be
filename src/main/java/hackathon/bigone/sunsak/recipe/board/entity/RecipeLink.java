package hackathon.bigone.sunsak.recipe.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RecipeLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recipelinkId;

    @Column(nullable = false, length = 500)
    private String recipelinkUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_postId")
    private Board board;
}

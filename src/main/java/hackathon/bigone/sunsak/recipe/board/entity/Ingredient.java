package hackathon.bigone.sunsak.recipe.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredientId;

    @Column(nullable = false, length = 100)
    private String ingredientName;

    @Column(nullable = true, length = 50)
    private String ingredientAmount; // "100g", "1개" 등 수량 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_postId")
    private Board board;
}

package hackathon.bigone.sunsak.recipe.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stepId;

    @Column(nullable= false)
    private int stepNumber;

    @Column(nullable =false)
    private String stepImageUrl;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String stepDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_post_id")
    private Board board;
}

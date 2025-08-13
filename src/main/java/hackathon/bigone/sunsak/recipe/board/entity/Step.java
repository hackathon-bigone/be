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

    // 단계 순서
    @Column(nullable = false)
    private int stepNumber;

    // 프론트에서 업로드 후 전달되는 이미지 URL
    @Column(nullable = false, length = 500)
    private String stepImageUrl;

    // 단계 설명
    @Column(columnDefinition = "TEXT", nullable = false)
    private String stepDescription;

    // 해당 단계가 속한 Board
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_post_id")
    private Board board;
}

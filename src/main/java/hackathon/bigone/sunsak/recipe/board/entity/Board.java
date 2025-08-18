package hackathon.bigone.sunsak.recipe.board.entity;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.enums.RecipeCategory;
import hackathon.bigone.sunsak.recipe.comment.entity.Comment;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "RecipeBoards")
@EntityListeners(AuditingEntityListener.class)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    // 제목
    @Column(nullable=false, length = 100)
    private String title;

    // 조리시간
    @Column(nullable=false, length = 100)
    private int cookingTime;

    // 대표사진 URL
    @Column(nullable = true, length = 500)
    private String mainImageUrl;

    // 재료
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    // 레시피 링크
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeLink> recipeLink = new ArrayList<>();

    // 레시피 설명
    @Column(columnDefinition = "TEXT", nullable = false)
    private String recipeDescription;

    // 작성자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private SiteUser author;

    // 단계
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(max = 15, message = "레시피 단계는 최대 15개까지 가능합니다.")
    private List<Step> steps = new ArrayList<>();

    // 카테고리
    @ElementCollection(targetClass = RecipeCategory.class)
    @CollectionTable(name = "board_categories", joinColumns = @JoinColumn(name = "board_post_id"))
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private List<RecipeCategory> categories = new ArrayList<>();

    // 좋아요 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeLike> likes = new ArrayList<>();

    // 댓글 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 스크랩 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeScrap> scraps = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createDate;
}
package hackathon.bigone.sunsak.recipe.board.entity;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import hackathon.bigone.sunsak.recipe.board.enums.RecipeCategory;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.Size;

@Entity
@Getter
@Setter
@Table(name = "RecipeBoards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    //제목
    @Column(nullable=false, length = 100)
    private String title;

    //조리시간
    @Column(nullable=false, length = 100)
    private String cookingTime;

    //대표사진
    @Column(nullable = true, length = 500)
    private String mainimageUrl;

    //재료
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredient> ingredients = new ArrayList<>();

    //레시피링크
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeLink> RecipeLink = new ArrayList<>();

    //레시피설명
    @Column(columnDefinition = "TEXT", nullable = false)
    private String recipeDescription;

    //작성자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private SiteUser author;

    //단계
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(max = 15,  message = "레시피 단계는 최대 15개까지 가능합니다.")
    private List<Step> steps = new ArrayList<>();

    //카테고리
    @ElementCollection(targetClass = RecipeCategory.class)
    @CollectionTable(name = "board_categories", joinColumns = @JoinColumn(name = "board_post_id"))
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private List<RecipeCategory> categories = new ArrayList<>();


}

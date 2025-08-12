package hackathon.bigone.sunsak.accounts.mypage.entity;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.base.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Question extends BaseTime {
    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private SiteUser author;

    @ElementCollection
    //별도의 gna_images 테이블로 자동생성
    @CollectionTable(name = "qna_images", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "image_url")
    private List<String> imageUrls  = new ArrayList<>();

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Answer answer;

    @Transient //상태 저장 안함
    public String getAnswerStatus() {
        return (answer != null) ? "답변완료" : "답변중";
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
        if (answer != null && answer.getQuestion() != this) {
            answer.setQuestion(this);
        }
    }
}

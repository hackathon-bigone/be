package hackathon.bigone.sunsak.accounts.mypage.entity;

import hackathon.bigone.sunsak.global.base.entity.BaseTime;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Qna extends BaseTime {
    private String title;
    private String body;
}

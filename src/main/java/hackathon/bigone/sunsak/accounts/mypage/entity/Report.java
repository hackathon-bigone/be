package hackathon.bigone.sunsak.accounts.mypage.entity;

import hackathon.bigone.sunsak.accounts.mypage.enums.ReportTarget;
import hackathon.bigone.sunsak.accounts.mypage.enums.ReportType;
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
public class Report extends BaseTime {
    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTarget target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    private String postLink;

    @ElementCollection
    //별도의 gna_images 테이블로 자동생성
    @CollectionTable(name = "report_images", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "image_key")
    @Builder.Default
    private List<String> imageKeys = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

}

package hackathon.bigone.sunsak.global.initData.mypage.noitce;


import hackathon.bigone.sunsak.accounts.mypage.entity.Notice;
import hackathon.bigone.sunsak.accounts.mypage.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class NoticeInit {
    private final NoticeRepository noticeRepository;

    @Bean
    @Order(6) //초기 데이터 실행 순서
    public ApplicationRunner initNotices(){
        return args -> {
            if (noticeRepository.count() == 0) {
                insertDefaultNotices();
            }
        };
    }

    private void insertDefaultNotices() {
        Notice notice1 = new Notice(
                "개인정보 처리 방침 개정",
                """
                안녕하세요, 순삭입니다.
                
                더 안전하고 투명한 서비스 제공을 위해 개인정보 처리 방침이 다음과 같이 개정될 예정입니다.
                - 시행 일자: 2024년 12월 1일
                - 주요 변경 사항: 수집 항목 일부 조정, 보관 기간 명확화, 제3자 제공 관련 내용 보완
                개정된 개인정보 처리 방침 전문은 시행일 이전에 서비스 내 공지사항과 홈페이지를 통해 확인하실 수 있습니다. 
                회원 여러분께서는 변경 사항을 꼭 확인하시어 서비스 이용에 참고하시기 바랍니다.
                
                감사합니다.
                """,
                false
        );
        notice1.setCreatedDateManually(LocalDateTime.of(2024, 12, 1, 0, 0));
        noticeRepository.save(notice1);

        Notice notice2 = new Notice(
                "v.1.2.1 버전 업데이트 안내",
                """
                안녕하세요, 순삭입니다.
        
                더욱 쾌적하고 안정적인 서비스 지원을 위해 다음과 같이 서비스의 시스템 업데이트를 진행할 예정입니다.
                업데이트 일시: 8월 5일
                업데이트 내용: 레시피 및 공동구매 서비스의 댓글에 대한
                답글 기능
        
                순삭 서비스를 이용해 주시는 모든 분들께 감사드리며, 더 나은 서비스로 보답할 있도록 늘 노력하겠습니다.
        
                감사합니다.
                """,
                true
        );

        notice2.setCreatedDateManually(LocalDateTime.of(2025,8,1,0,0,0));
        noticeRepository.save(notice2);
    }
}

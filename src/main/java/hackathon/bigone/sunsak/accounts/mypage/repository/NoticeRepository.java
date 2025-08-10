package hackathon.bigone.sunsak.accounts.mypage.repository;

import hackathon.bigone.sunsak.accounts.mypage.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice,Long> {
    List<Notice> findAllByOrderByIsFixedDescCreateDateDesc();
}

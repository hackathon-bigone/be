package hackathon.bigone.sunsak.accounts.mypage.service;

import hackathon.bigone.sunsak.accounts.mypage.dto.NoticeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.PasswordChangeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.ReportDto;
import hackathon.bigone.sunsak.accounts.mypage.entity.Report;
import hackathon.bigone.sunsak.accounts.mypage.repository.NoticeRepository;
import hackathon.bigone.sunsak.accounts.mypage.repository.ReportRepository;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.global.aws.s3.service.S3Uploader;
import hackathon.bigone.sunsak.global.validate.accounts.SignupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupValidator signupValidator;
    private final NoticeRepository noticeRepository;
    private final ReportRepository reportRepository;
    private final S3Uploader s3Uploader;

    //닉네임 수정
    @Transactional
    public void updateNickname(Long userId, String newNickname) {
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getNickname().equals(newNickname)) {
            throw new IllegalArgumentException("현재 사용 중인 닉네임과 동일합니다.");
        }

        user.setNickname(newNickname);
    }

    //비밀번호 변경
    @Transactional
    public void updatePassword(Long userId, PasswordChangeDto dto){
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        signupValidator.passwordValidate(dto.getNewPassword(), dto.getRepeatPw());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    //모든 공지사항 확인
    @Transactional
    public List<NoticeDto> getAllNotices() {
        return noticeRepository.findAllByOrderByIsFixedDescCreateDateDesc()
                .stream()
                .map(NoticeDto::from) // from()에서 이미 displayDate까지 세팅
                .toList();
    }

    //공지사항 상세 확인
    @Transactional
    public  Optional<NoticeDto> getNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .map(NoticeDto::from);
    }

    //신고하기 - 작성
    @Transactional
    public ReportDto createReport(Long userId, ReportDto dto) {
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

//        if (dto.getTarget() == ReportTarget.POST &&
//                (dto.getPostLink() == null || dto.getPostLink().isBlank())) {
//            throw new IllegalArgumentException("게시물 신고일 경우 링크는 필수입니다.");
//        }

        List<String> keys = (dto.getImageKeys() == null) ? List.of() : dto.getImageKeys();

        Report toSave = Report.builder()
                .title(dto.getTitle())
                .target(dto.getTarget())
                .type(dto.getType())
                .postLink(dto.getPostLink())
                .body(dto.getBody())
                .imageKeys(keys)
                .build();

        Report saved = reportRepository.save(toSave);

        List<String> viewUrls = keys.stream()
                .map(k -> s3Uploader.presignedGetUrl(k, Duration.ofMinutes(15)).toString())
                .collect(Collectors.toList());

        return ReportDto.builder()
                .reportId(saved.getId())
                .title(saved.getTitle())
                .target(saved.getTarget())
                .type(saved.getType())
                .postLink(saved.getPostLink())
                .body(saved.getBody())
                .imageKeys(viewUrls)  // presigned URL을 응답에 넣음
                .createDate(saved.getCreateDate())
                .build();
    }


}

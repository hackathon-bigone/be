package hackathon.bigone.sunsak.groupbuy.board.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyListResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyRequestDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyLink;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyScrap;
import hackathon.bigone.sunsak.groupbuy.board.enums.GroupBuyStatus;
import hackathon.bigone.sunsak.groupbuy.board.repository.GroupBuyRepository;
import hackathon.bigone.sunsak.groupbuy.board.repository.GroupBuyScrapRepository;
import hackathon.bigone.sunsak.groupbuy.comment.dto.GroupBuyCommentResponseDto;
import hackathon.bigone.sunsak.groupbuy.comment.repository.GroupBuyCommentRepository;
import hackathon.bigone.sunsak.groupbuy.comment.service.GroupBuyCommentService;
import hackathon.bigone.sunsak.recipe.board.repository.ScrapRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyService {
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyScrapRepository groupBuyScrapRepository;
    private final GroupBuyCommentService groupBuyCommentService;
    private final GroupBuyCommentRepository groupBuyCommentRepository;
    private ScrapRepository scrapRepository;


    //공동구매 생성 기능
    @Transactional
    public GroupbuyResponseDto create(GroupbuyRequestDto groupdto, SiteUser author) {
        List<GroupBuyLink> links = (groupdto.getGroupbuyLinkUrls() != null) ?
                groupdto.getGroupbuyLinkUrls().stream()
                        .map(url -> {
                            GroupBuyLink link = new GroupBuyLink();
                            link.setGroupbuylinkUrl(url);
                            return link;
                        })
                        .collect(Collectors.toList()) : new ArrayList<>();

        Groupbuy newGroupbuy = new Groupbuy();
        newGroupbuy.setGroupbuyTitle(groupdto.getGroupbuyTitle());
        newGroupbuy.setGroupbuyDescription(groupdto.getGroupbuyDescription());
        newGroupbuy.setMainImageUrl(groupdto.getMainImageUrl());
        newGroupbuy.setGroupbuyCount(groupdto.getGroupbuyCount());
        newGroupbuy.setStatus(GroupBuyStatus.RECRUITING); // 초기 상태는 '모집중'으로 설정
        newGroupbuy.setAuthor(author);

        newGroupbuy.setBuyLinks(links);
        links.forEach(link -> link.setGroupbuy(newGroupbuy));

        Groupbuy savedGroupbuy = groupBuyRepository.save(newGroupbuy);

        return new GroupbuyResponseDto(savedGroupbuy);
    }

    //공동구매 수정 기능
    @Transactional
    public GroupbuyResponseDto update(Long groupbuyId, GroupbuyRequestDto groupdto, SiteUser author) {
        Groupbuy groupbuy = groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("공동구매 게시글을 찾을 수 없습니다."));

        if (groupbuy.getAuthor() == null || !groupbuy.getAuthor().equals(author)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        groupbuy.setGroupbuyTitle(groupdto.getGroupbuyTitle());
        groupbuy.setGroupbuyDescription(groupdto.getGroupbuyDescription());
        groupbuy.setGroupbuyCount(groupdto.getGroupbuyCount());
        groupbuy.setMainImageUrl(groupdto.getMainImageUrl());

        if (groupdto.getStatus() != null) {
            groupbuy.setStatus(groupdto.getStatus());
        }

        groupbuy.getBuyLinks().clear();
        groupdto.getGroupbuyLinkUrls().stream()
                .map(url -> {
                    GroupBuyLink newLink = new GroupBuyLink();
                    newLink.setGroupbuylinkUrl(url);
                    newLink.setGroupbuy(groupbuy);
                    return newLink;
                })
                .forEach(link -> groupbuy.getBuyLinks().add(link));

        Groupbuy updatedGroupbuy = groupBuyRepository.save(groupbuy);
        return new GroupbuyResponseDto(updatedGroupbuy);
    }

    public List<GroupbuyResponseDto> getMyGroupbuys(Long userId){
        List<Groupbuy> myGroupbuys = groupBuyRepository.findByAuthor_Id(userId);
        return myGroupbuys.stream().map(GroupbuyResponseDto::new).collect(Collectors.toList());
    }

    //공동구매 삭제 기능
    @Transactional
    public void delete(Long groupbuyId, SiteUser author) {
        Groupbuy groupbuy = groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("공동구매 게시글을 찾을 수 없습니다."));

        if (!groupbuy.getAuthor().equals(author)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        groupBuyRepository.delete(groupbuy);
    }

    // 특정 게시글 상세 조회
    public GroupbuyResponseDto findGroupbuyById(Long groupbuyId) {
        Groupbuy groupbuy = groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        Long authorId= groupbuy.getAuthor().getId();
        long authorPostCount = groupBuyRepository.countByAuthor_Id(authorId);

        // 첫 번째 변수 선언을 제거하고, 기존 코드와 합쳐서 올바르게 수정합니다.
        List<GroupBuyCommentResponseDto> comments = groupBuyCommentRepository.findByGroupbuy_GroupbuyId(groupbuyId).stream()
                .filter(comment -> comment.getParent() == null)
                .map(GroupBuyCommentResponseDto::new)
                .collect(Collectors.toList());

        return new GroupbuyResponseDto(groupbuy, comments, (int) authorPostCount);
    }

    //공동구매 스크랩 기능
    @Transactional
    public void toggleScrap(Long groupbuyId, SiteUser user) {
        Groupbuy groupbuy = groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("공동구매 게시글을 찾을 수 없습니다."));
        Optional<GroupBuyScrap> existingScrap = groupBuyScrapRepository.findByUserAndGroupbuy(user, groupbuy);

        if (existingScrap.isPresent()) {
            groupBuyScrapRepository.delete(existingScrap.get());
        } else {
            GroupBuyScrap newScrap = new GroupBuyScrap();
            newScrap.setUser(user);
            newScrap.setGroupbuy(groupbuy);
            groupBuyScrapRepository.save(newScrap);
        }
    }

    //마이페이지 스크랩 연동
    @Transactional(readOnly=true)
    public List<GroupbuyResponseDto> getScrapGroupbuysByUser(SiteUser user) {
        return groupBuyScrapRepository.findByUser(user).stream()
                .map(scrap -> {
                    Groupbuy groupbuy = scrap.getGroupbuy();
                    // Groupbuy 엔티티만 받는 생성자 호출
                    return new GroupbuyResponseDto(groupbuy);
                })
                .collect(Collectors.toList());
    }

    //검색
    public List<GroupbuyResponseDto> searchGroupbuysByTitle(String keyword, Pageable pageable) {
        List<Groupbuy> groupbuys = groupBuyRepository.findByGroupbuyTitleContaining(keyword, pageable);
        return groupbuys.stream()
                .map(GroupbuyResponseDto::new)
                .collect(Collectors.toList());
    }

    //모든 게시믈 조회
    @Transactional(readOnly = true)
    public GroupbuyListResponseDto findAllGroupbuys(String sort) {
        Sort sortBy = Sort.by(Sort.Direction.DESC, "createDate");

        List<Groupbuy> groupbuys = groupBuyRepository.findAll(sortBy);

        List<GroupbuyResponseDto> groupbuyDtos = groupbuys.stream()
                .map(groupbuy -> {
                    return new GroupbuyResponseDto(groupbuy, new ArrayList<>(), 0);
                })
                .collect(Collectors.toList());
        long totalCount = groupBuyRepository.count();
        return new GroupbuyListResponseDto(groupbuyDtos, totalCount);
    }

    //작성한 게시글 수 세기
    public long countMyGroupbuys(Long userId){
        return groupBuyRepository.countByAuthor_Id(userId);
    }

    //스크랩한 게시글 수 세기
    public long countGroupbuyScrap(Long userId){
        return groupBuyScrapRepository.countByUser_Id(userId);
    }
}
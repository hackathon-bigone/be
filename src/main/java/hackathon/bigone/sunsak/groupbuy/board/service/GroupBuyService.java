package hackathon.bigone.sunsak.groupbuy.board.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyRequestDto;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.entity.Groupbuy;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyLink;
import hackathon.bigone.sunsak.groupbuy.board.entity.GroupBuyScrap;
import hackathon.bigone.sunsak.groupbuy.board.enums.GroupBuyStatus;
import hackathon.bigone.sunsak.groupbuy.board.repository.GroupBuyRepository;
import hackathon.bigone.sunsak.groupbuy.board.repository.GroupBuyScrapRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyService {
    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyScrapRepository GroupBuyScrapRepository;


    //공동구매 생성 기능
    @Transactional
    public GroupbuyResponseDto create(GroupbuyRequestDto groupdto, SiteUser author) {
        List<GroupBuyLink> links = groupdto.getGroupbuyLinkUrls().stream()
                .map(url -> {
                    GroupBuyLink link = new GroupBuyLink();
                    link.setGroupbuylinkUrl(url);
                    return link;
                })
                .collect(Collectors.toList());

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
        groupbuy.getBuyLinks().clear();
        groupdto.getGroupbuyLinkUrls().stream()
                .map(url -> {
                    GroupBuyLink newLink = new GroupBuyLink();
                    newLink.setGroupbuylinkUrl(url);
                    newLink.setGroupbuy(groupbuy);
                    return newLink;
                })
                .forEach(link -> groupbuy.getBuyLinks().add(link));


        return new GroupbuyResponseDto(groupbuy);
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
        return new GroupbuyResponseDto(groupbuy);
    }

    //전체 조회 (정렬)
    public List<GroupbuyResponseDto> findAllGroupbuys(String sort) {
        // TODO: popular 정렬 로직 필요시 추가
        Sort sortBy = Sort.by(Sort.Direction.DESC, "createDate");

        List<Groupbuy> groupbuys = groupBuyRepository.findAll(sortBy);
        return groupbuys.stream()
                .map(GroupbuyResponseDto::new)
                .collect(Collectors.toList());
    }

    //공동구매 스크랩 기능
    @Transactional
    public void scrap(Long groupbuyId, SiteUser user) {
        Groupbuy groupbuy = groupBuyRepository.findById(groupbuyId)
                .orElseThrow(() -> new EntityNotFoundException("공동구매 게시글을 찾을 수 없습니다."));

        boolean isScrapped = GroupBuyScrapRepository.existsByUserAndGroupbuy(user, groupbuy);

        if (isScrapped) {
            GroupBuyScrap scrap = GroupBuyScrapRepository.findByUserAndGroupbuy(user, groupbuy)
                    .orElseThrow(() -> new EntityNotFoundException("스크랩 정보를 찾을 수 없습니다."));
            GroupBuyScrapRepository.delete(scrap);
        } else {
            GroupBuyScrap newScrap = new GroupBuyScrap();
            newScrap.setUser(user);
            newScrap.setGroupbuy(groupbuy);
            GroupBuyScrapRepository.save(newScrap);
        }
    }
}
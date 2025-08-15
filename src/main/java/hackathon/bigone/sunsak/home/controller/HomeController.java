package hackathon.bigone.sunsak.home.controller;

import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.home.dto.HomeFoodDto;
import hackathon.bigone.sunsak.home.service.HomeService;
import hackathon.bigone.sunsak.recipe.board.dto.BoardListResponseDto;
import hackathon.bigone.sunsak.recipe.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {
    private final HomeService homeService;
    private final BoardService boardService;

    @GetMapping("/foodbox")
    public ResponseEntity<HomeFoodDto> getImminentFoods(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if(userDetail == null){
            return ResponseEntity.ok(homeService.getGuestFoodBox());
        }
        return ResponseEntity.ok(homeService.getImminentFoods(userDetail.getId()));
    }

    @GetMapping("/top5-popular-boards")
    public ResponseEntity<BoardListResponseDto> getTop5PopularBoards() {
        BoardListResponseDto response = boardService.getTop5PopularBoards();
        return ResponseEntity.ok(response);
    }
}

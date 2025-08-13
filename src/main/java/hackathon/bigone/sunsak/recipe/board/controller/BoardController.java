package hackathon.bigone.sunsak.recipe.board.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.global.aws.s3.service.S3Uploader;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.recipe.board.dto.BoardDto;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor

public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @GetMapping
    public ResponseEntity<List<BoardDto>> getAllBoards() {
        List<BoardDto> boards = boardService.findAllBoards();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<BoardDto> getBoardById(@PathVariable Long postId){
        BoardDto board = boardService.findBoardById(postId);
        return ResponseEntity.ok(board);
    }

    //UserDetial을 CustomUserDetail로 변경
    //CustomUserDetail 안에 SiteUser 엔티티가 직접 있어서 DB 재조회 불필요
    @PostMapping
    public ResponseEntity<String> createBoard(
            @RequestBody BoardDto boardDto,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if(userDetail == null){
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser author = userDetail.getUser();

        try {
            boardService.createBoard(boardDto, author);
            return new ResponseEntity<>("게시글이 성공적으로 생성되었습니다.", HttpStatus.CREATED);
        } catch(IOException e){
            log.error("Failed to create board due to image upload error", e);
            return new ResponseEntity<>("이미지 업로드 중 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/boards/{postId}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long postId, @RequestBody BoardDto boardDto,
                                             @AuthenticationPrincipal CustomUserDetail userDetail){
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();
        Board updatedBoard = boardService.updateBoard(postId, boardDto, currentUser);

        return ResponseEntity.ok(updatedBoard);
    }

    //좋아요
    @PostMapping("/{postId}/like") //경로 '/' 없어서 추가
    public ResponseEntity<String> toggleLike(
            @PathVariable Long postId, @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        SiteUser currentUser = userDetail.getUser();
        boardService.toggleLike(postId, currentUser); //서비스 호출

        return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
    }

    @PostMapping("/{postId}/scrap")
    public ResponseEntity<String> toggleScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        SiteUser currentUser = userDetail.getUser();
        boardService.toggleScrap(postId, currentUser); //서비스 호출

        return ResponseEntity.ok("스크랩 상태가 변경되었습니다.");
    }


}



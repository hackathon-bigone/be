package hackathon.bigone.sunsak.recipe.board.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadRequest;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadResponse;
import hackathon.bigone.sunsak.global.aws.s3.service.PresignUploadService;
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

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final PresignUploadService presignUploadService;

    // 기존 API (변경 없음)
    @GetMapping
    public ResponseEntity<List<BoardDto>> getAllBoards() {
        List<BoardDto> boards = boardService.findAllBoards();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<BoardDto> getBoardById(@PathVariable Long postId) {
        BoardDto board = boardService.findBoardById(postId);
        return ResponseEntity.ok(board);
    }

    //------------------- 새로운 업로드 방식 적용 -------------------

    /**
     * 클라이언트로부터 파일 업로드에 필요한 Pre-signed URL을 발급하는 API
     * @param prefix S3 버킷 내의 파일 경로 접두사 (예: "recipe")
     */
    @PostMapping("/uploads/{prefix}")
    public ResponseEntity<List<PresignUploadResponse>> getPresignedUrls(
            @PathVariable String prefix, // URL에서 prefix를 받음
            @RequestBody List<PresignUploadRequest> reqList,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getUser().getId();
        Duration ttl = Duration.ofMinutes(10); // URL 유효 시간: 10분

        try {
            List<PresignUploadResponse> response = presignUploadService.issuePresigned(prefix, userId, reqList, ttl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 게시글 생성 API (이미지 키를 DTO에 담아 JSON으로 받음)
     */
    @PostMapping
    public ResponseEntity<String> createBoard(
            @RequestBody BoardDto boardDto, // @RequestBody로 변경
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser author = userDetail.getUser();

        try {
            boardService.create(boardDto, author);
            return new ResponseEntity<>("게시글이 성공적으로 생성되었습니다.", HttpStatus.CREATED);
        } catch (IOException e) {
            log.error("게시글 생성 중 오류 발생", e);
            return new ResponseEntity<>("게시글 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 게시글 수정 API (이미지 키를 DTO에 담아 JSON으로 받음)
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<Board> updateBoard(
            @PathVariable Long postId,
            @RequestBody BoardDto boardDto, // @RequestBody 유지
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();

        try {
            Board updatedBoard = boardService.updateBoard(postId, boardDto, currentUser);
            return ResponseEntity.ok(updatedBoard);
        } catch (IOException e) {
            log.error("게시글 수정 중 오류 발생", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 기존 API (변경 없음)
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();
        boardService.toggleLike(postId, currentUser);
        return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
    }

    // 기존 API (변경 없음)
    @PostMapping("/{postId}/scrap")
    public ResponseEntity<String> toggleScrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();
        boardService.toggleScrap(postId, currentUser);
        return ResponseEntity.ok("스크랩 상태가 변경되었습니다.");
    }
}
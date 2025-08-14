package hackathon.bigone.sunsak.recipe.board.controller;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadRequest;
import hackathon.bigone.sunsak.global.aws.s3.dto.PresignUploadResponse;
import hackathon.bigone.sunsak.global.aws.s3.service.PresignUploadService;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.recipe.board.dto.BoardRequestDto;
import hackathon.bigone.sunsak.recipe.board.dto.BoardResponseDto;
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

    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getAllBoards() {
        List<BoardResponseDto> boards = boardService.findAllBoards();
        return ResponseEntity.ok(boards);
    }

    // 특정 게시글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<BoardResponseDto> getBoardById(@PathVariable Long postId) {
        BoardResponseDto board = boardService.findBoardById(postId);
        return ResponseEntity.ok(board);
    }

    @PostMapping("/uploads/{prefix}")
    public ResponseEntity<Object> getPresignedUrls(
            @PathVariable String prefix,
            @RequestBody List<PresignUploadRequest> reqList,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getUser().getId();
        Duration ttl = Duration.ofMinutes(10);

        try {
            List<PresignUploadResponse> response = presignUploadService.issuePresigned(prefix, userId, reqList, ttl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Pre-signed URL 발급 중 오류 발생", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<String> createBoard(
            @RequestBody BoardRequestDto boardDto,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser author = userDetail.getUser();

        boardService.create(boardDto, author);
        return new ResponseEntity<>("게시글이 성공적으로 생성되었습니다.", HttpStatus.CREATED);
    }

    // 게시글 수정 API
    @PatchMapping("/{postId}")
    public ResponseEntity<BoardResponseDto> updateBoard(
            @PathVariable Long postId,
            @RequestBody BoardRequestDto boardDto,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();

        BoardResponseDto updatedBoard = boardService.updateBoard(postId, boardDto, currentUser);
        return ResponseEntity.ok(updatedBoard);
    }

    // 게시글 삭제 API
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deleteBoard(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        if (userDetail == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();
        try {
            boardService.deleteBoard(postId, currentUser);
            return new ResponseEntity<>("게시글이 성공적으로 삭제되었습니다.", HttpStatus.NO_CONTENT); // 204 No Content
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // 403 Forbidden
        }
    }

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

    //검색
    @GetMapping("/search")
    public ResponseEntity<List<BoardResponseDto>> searchBoards(@RequestParam String keywords){
        List<BoardResponseDto> results = boardService.findBoardByKeywords(keywords);
        return ResponseEntity.ok(results);
    }
}
package hackathon.bigone.sunsak.recipe.board.controller;

import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.dto.BoardDto;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.service.BoardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor

public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createBoard(@RequestBody BoardDto boardDto,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if(userDetails == null){
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        SiteUser author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        boardService.create(boardDto, author);
        return new ResponseEntity<>("게시글이 성공적으로 생성되었습니다.", HttpStatus.CREATED);}

    @PatchMapping("/boards/{postId}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long postId, @RequestBody BoardDto boardDto,
                                             @AuthenticationPrincipal UserDetails userDetails){
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(("사용자를 찾을 수 없습니다.")));
        Board updatedBoard = boardService.updateBoard(postId, boardDto, currentUser);

        return ResponseEntity.ok(updatedBoard);
        }
    }



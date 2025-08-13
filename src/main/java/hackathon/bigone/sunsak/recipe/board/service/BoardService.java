package hackathon.bigone.sunsak.recipe.board.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.aws.s3.service.S3Uploader;
import hackathon.bigone.sunsak.recipe.board.dto.BoardRequestDto;
import hackathon.bigone.sunsak.recipe.board.dto.BoardResponseDto;
import hackathon.bigone.sunsak.recipe.board.entity.*;
import hackathon.bigone.sunsak.recipe.board.repository.BoardRepository;
import hackathon.bigone.sunsak.recipe.board.repository.LikeRepository;
import hackathon.bigone.sunsak.recipe.board.repository.ScrapRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;


    @Transactional
    public BoardResponseDto create(BoardRequestDto boardDto, SiteUser author) {
        Board newBoard = new Board();
        newBoard.setTitle(boardDto.getTitle());
        newBoard.setCookingTime(boardDto.getCookingTime());
        newBoard.setRecipeDescription(boardDto.getRecipeDescription());
        newBoard.setAuthor(author);
        newBoard.setMainImageUrl(boardDto.getMainImageUrl());

        if (boardDto.getIngredients() != null) {
            boardDto.getIngredients().forEach(ingredientDto -> {
                Ingredient newIngredient = new Ingredient();
                newIngredient.setIngredientName(ingredientDto.getIngredientName());
                newIngredient.setIngredientAmount(ingredientDto.getIngredientAmount());
                newIngredient.setBoard(newBoard);
                newBoard.getIngredients().add(newIngredient);
            });
        }

        if (boardDto.getSteps() != null) {
            boardDto.getSteps().forEach(stepDto -> {
                Step newStep = new Step();
                newStep.setStepNumber(stepDto.getStepNumber());
                newStep.setStepDescription(stepDto.getStepDescription());
                newStep.setBoard(newBoard);
                // 👈 stepDto가 StepRequestDto 타입이므로 stepImageUrl 필드를 추가해야 함
                newStep.setStepImageUrl(stepDto.getStepImageUrl());
                newBoard.getSteps().add(newStep);
            });
        }

        if (boardDto.getRecipeLinks() != null) {
            boardDto.getRecipeLinks().forEach(recipeLinkDto -> {
                RecipeLink newLink = new RecipeLink();
                newLink.setRecipelinkUrl(recipeLinkDto.getRecipelinkUrl());
                newLink.setBoard(newBoard);
                newBoard.getRecipeLink().add(newLink);
            });
        }

        if (boardDto.getCategories() != null) {
            newBoard.getCategories().addAll(boardDto.getCategories());
        }

        return new BoardResponseDto(boardRepository.save(newBoard));
    }

    @Transactional
    public BoardResponseDto updateBoard(Long postId, BoardRequestDto boardDto, SiteUser currentUser) {
        Board existingBoard = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + postId));

        if (!existingBoard.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 게시글을 수정할 권한이 없습니다.");
        }

        existingBoard.setTitle(boardDto.getTitle());
        existingBoard.setRecipeDescription(boardDto.getRecipeDescription());
        existingBoard.setCookingTime(boardDto.getCookingTime());
        existingBoard.setMainImageUrl(boardDto.getMainImageUrl());

        existingBoard.getSteps().clear();
        if (boardDto.getSteps() != null) {
            boardDto.getSteps().forEach(stepDto -> {
                Step newStep = new Step();
                newStep.setStepNumber(stepDto.getStepNumber());
                newStep.setStepDescription(stepDto.getStepDescription());
                newStep.setBoard(existingBoard);
                newStep.setStepImageUrl(stepDto.getStepImageUrl());
                existingBoard.getSteps().add(newStep);
            });
        }

        existingBoard.getIngredients().clear();
        if (boardDto.getIngredients() != null) {
            boardDto.getIngredients().forEach(ingredientDto -> {
                Ingredient newIngredient = new Ingredient();
                newIngredient.setIngredientName(ingredientDto.getIngredientName());
                newIngredient.setIngredientAmount(ingredientDto.getIngredientAmount());
                newIngredient.setBoard(existingBoard);
                existingBoard.getIngredients().add(newIngredient);
            });
        }

        existingBoard.getRecipeLink().clear();
        if (boardDto.getRecipeLinks() != null) {
            boardDto.getRecipeLinks().forEach(recipeLinkDto -> {
                RecipeLink newLink = new RecipeLink();
                newLink.setRecipelinkUrl(recipeLinkDto.getRecipelinkUrl());
                newLink.setBoard(existingBoard);
                existingBoard.getRecipeLink().add(newLink);
            });
        }

        existingBoard.getCategories().clear();
        if (boardDto.getCategories() != null) {
            existingBoard.getCategories().addAll(boardDto.getCategories());
        }

        return new BoardResponseDto(boardRepository.save(existingBoard));
    }

    @Transactional
    public void deleteBoard(Long postId, SiteUser currentUser) {
        Board existingBoard = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + postId));

        if (!existingBoard.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 게시글을 삭제할 권한이 없습니다.");
        }
        boardRepository.delete(existingBoard);
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> findAllBoards() {
        return boardRepository.findAll().stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardResponseDto findBoardById(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return new BoardResponseDto(board);
    }

    @Transactional
    public void toggleLike(Long postId, SiteUser user) {
        Board board = boardRepository.findById(postId).orElseThrow();
        Optional<RecipeLike> existingLike = likeRepository.findByBoardAndUser(board, user);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            RecipeLike newLike = new RecipeLike();
            newLike.setBoard(board);
            newLike.setUser(user);
            likeRepository.save(newLike);
        }
    }

    @Transactional
    public void toggleScrap(Long postId, SiteUser user) {
        Board board = boardRepository.findById(postId).orElseThrow();
        Optional<RecipeScrap> existingScrap = scrapRepository.findByBoardAndUser(board, user);

        if (existingScrap.isPresent()) {
            scrapRepository.delete(existingScrap.get());
        } else {
            RecipeScrap newScrap = new RecipeScrap();
            newScrap.setBoard(board);
            newScrap.setUser(user);
            scrapRepository.save(newScrap);
        }
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> getLikedBoardsByUser(SiteUser user) {
        return likeRepository.findByUser(user).stream()
                .map(RecipeLike::getBoard)
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> getScrapBoardsByUser(SiteUser user) {
        return scrapRepository.findByUser(user).stream()
                .map(RecipeScrap::getBoard)
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }
}
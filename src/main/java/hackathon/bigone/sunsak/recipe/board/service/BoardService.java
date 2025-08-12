package hackathon.bigone.sunsak.recipe.board.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.aws.s3.service.S3Uploader;
import hackathon.bigone.sunsak.recipe.board.dto.BoardDto;
import hackathon.bigone.sunsak.recipe.board.dto.IngredientDto;
import hackathon.bigone.sunsak.recipe.board.dto.RecipeLinkDto;
import hackathon.bigone.sunsak.recipe.board.dto.StepDto;
import hackathon.bigone.sunsak.recipe.board.entity.*;
import hackathon.bigone.sunsak.recipe.board.repository.BoardRepository;
import hackathon.bigone.sunsak.recipe.board.repository.LikeRepository;
import hackathon.bigone.sunsak.recipe.board.repository.ScrapRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final LikeRepository LikeRepository;
    private final ScrapRepository scrapRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public Board createBoard(BoardDto boardDto, SiteUser author) throws IOException { // IOException 예외를 던짐
        Board newBoard = new Board();
        newBoard.setTitle(boardDto.getTitle());
        newBoard.setCookingTime(boardDto.getCookingTime());
        newBoard.setRecipeDescription(boardDto.getRecipeDescription());
        newBoard.setAuthor(author);

        if(boardDto.getMainImageFile() != null && !boardDto.getMainImageFile().isEmpty()) {
            String imageUrl = s3Uploader.uploadOne("recipe", boardDto.getMainImageFile());
            newBoard.setMainImageUrl(imageUrl);
        }


        // 재료
        if (boardDto.getIngredients() != null) {
            boardDto.getIngredients().forEach(ingredientDto -> {
                Ingredient newIngredient = new Ingredient();
                newIngredient.setIngredientName(ingredientDto.getIngredientName());
                newIngredient.setIngredientAmount(ingredientDto.getIngredientAmount());
                newIngredient.setBoard(newBoard);
                newBoard.getIngredients().add(newIngredient);
            });
        }

        // 단계
        if (boardDto.getSteps() != null) {
            boardDto.getSteps().forEach(stepDto -> {
                Step newStep = new Step();
                newStep.setStepNumber(stepDto.getStepNumber());
                newStep.setStepDescription(stepDto.getStepDescription());
                newStep.setBoard(newBoard);
                newBoard.getSteps().add(newStep);
            });
        }

        // 링크
        if (boardDto.getRecipeLinks() != null) {
            boardDto.getRecipeLinks().forEach(recipeLinkDto -> {
                RecipeLink newLink = new RecipeLink();
                newLink.setRecipelinkUrl(recipeLinkDto.getRecipelinkUrl());
                newLink.setBoard(newBoard);
                newBoard.getRecipeLink().add(newLink);
            });
        }

        //카테고리
        if (boardDto.getCategories() != null) {
            newBoard.getCategories().addAll(boardDto.getCategories());
        }

        return boardRepository.save(newBoard);

    }
        @Transactional
        public Board updateBoard(Long postId, BoardDto boardDto, SiteUser currentUser) {
            Board existingBoard = boardRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + postId));

            if (!existingBoard.getAuthor().equals(currentUser)) {
                throw new IllegalStateException("이 게시글을 수정할 권한이 없습니다.");
            }

        existingBoard.setTitle(boardDto.getTitle());
        existingBoard.setRecipeDescription(boardDto.getRecipeDescription());
        existingBoard.setCookingTime(boardDto.getCookingTime());
        existingBoard.setMainImageUrl(boardDto.getMainImageUrl());

        // 단계(Steps) 업데이트
        existingBoard.getSteps().clear();
        if (boardDto.getSteps() != null) {
            boardDto.getSteps().forEach(stepDto -> {
                Step newStep = new Step();
                newStep.setStepNumber(stepDto.getStepNumber());
                newStep.setStepDescription(stepDto.getStepDescription());
                newStep.setBoard(existingBoard);
                existingBoard.getSteps().add(newStep);
            });
        }

        // 재료(Ingredients) 업데이트
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

        // 링크(Links) 업데이트
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

        return existingBoard;
    }


    // 모든 게시글(레시피) 조회
    public List<BoardDto> findAllBoards() {
        List<Board> boards = boardRepository.findAll();
        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 게시글(레시피) 조회
    public BoardDto findBoardById(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return convertToDto(board);
    }

    private BoardDto convertToDto(Board board) {
        BoardDto boardDto = new BoardDto();

        boardDto.setPostId(board.getPostId());
        boardDto.setTitle(board.getTitle());
        boardDto.setCookingTime(board.getCookingTime());
        boardDto.setRecipeDescription(board.getRecipeDescription());

        String s3Key = board.getMainImageUrl();
        if (s3Key != null && !s3Key.isEmpty()) {
            String imageUrl = s3Uploader.presignedGetUrl(s3Key, Duration.ofMinutes(10)).toString();
            boardDto.setMainImageUrl(imageUrl);
        }

        List<IngredientDto> ingredientDtos = board.getIngredients().stream()
                .map(this::convertIngredientToDto)
                .collect(Collectors.toList());
        boardDto.setIngredients(ingredientDtos);

        List<StepDto> stepDtos = board.getSteps().stream()
                .map(this::convertStepToDto)
                .collect(Collectors.toList());
        boardDto.setSteps(stepDtos);

        List<RecipeLinkDto> recipeLinkDtos = board.getRecipeLink().stream()
                .map(this::convertRecipeLinkToDto)
                .collect(Collectors.toList());
        boardDto.setRecipeLinks(recipeLinkDtos);

        boardDto.setCategories(board.getCategories());

        return boardDto;
    }
    private IngredientDto convertIngredientToDto(Ingredient ingredient) {
        IngredientDto ingredientDto = new IngredientDto();
        ingredientDto.setIngredientName(ingredient.getIngredientName());
        ingredientDto.setIngredientAmount(ingredient.getIngredientAmount());
        return ingredientDto;
    }
    private StepDto convertStepToDto(Step step) {
        StepDto stepDto = new StepDto();
        stepDto.setStepNumber(step.getStepNumber());
        stepDto.setStepDescription(step.getStepDescription());
        return stepDto;
    }

    private  RecipeLinkDto convertRecipeLinkToDto(RecipeLink recipeLink) {
        RecipeLinkDto recipeLinkDto = new RecipeLinkDto();
        recipeLinkDto.setRecipelinkUrl(recipeLink.getRecipelinkUrl());
        return recipeLinkDto;
    }

    private final LikeRepository likeRepository;
    public void toggleLike(Long postId, SiteUser user){
        Board board = boardRepository.findById(postId).orElseThrow();
        Optional<RecipeLike> existingLike = likeRepository.findByBoardAndUser(board, user);

        if(existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        }
        else {
            RecipeLike newLike = new RecipeLike();
            newLike.setBoard(board);
            newLike.setUser(user);
            likeRepository.save(newLike);
        }
    }

    public void toggleScrap(Long postId, SiteUser user){
        Board board = boardRepository.findById(postId).orElseThrow();
        Optional<RecipeScrap> existingScrap = scrapRepository.findByBoardAndUser(board, user);

        if (existingScrap.isPresent()) {
            scrapRepository.delete(existingScrap.get());
        }

        else{
            RecipeScrap newScrap = new RecipeScrap();
            newScrap.setBoard(board);
            newScrap.setUser(user);
            scrapRepository.save(newScrap);
        }
    }
    @Transactional(readOnly = true)
    public List<BoardDto> getLikedBoardsByUser(SiteUser user) {
        List<RecipeLike> likes = likeRepository.findByUser(user);
        List<Board> boards = likes.stream()
                .map(RecipeLike::getBoard)
                .toList();

        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<BoardDto> getScrapBoardsByUser(SiteUser user) {
        List<RecipeScrap> scraps = scrapRepository.findByUser(user);
        List<Board> boards = scraps.stream()
                .map(RecipeScrap::getBoard)
                .toList();

        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
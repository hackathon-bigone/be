package hackathon.bigone.sunsak.recipe.board.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.aws.s3.service.S3Uploader;
import hackathon.bigone.sunsak.recipe.board.dto.BoardListResponseDto;
import hackathon.bigone.sunsak.recipe.board.dto.BoardRequestDto;
import hackathon.bigone.sunsak.recipe.board.dto.BoardResponseDto;
import hackathon.bigone.sunsak.recipe.board.entity.*;
import hackathon.bigone.sunsak.recipe.board.enums.RecipeCategory;
import hackathon.bigone.sunsak.recipe.board.repository.BoardRepository;
import hackathon.bigone.sunsak.recipe.board.repository.LikeRepository;
import hackathon.bigone.sunsak.recipe.board.repository.ScrapRepository;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentResponseDto;
import hackathon.bigone.sunsak.recipe.comment.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;
    private final CommentService commentService;
    private final S3Uploader s3Uploader;

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
        Board savedBoard = boardRepository.save(newBoard);
        return new BoardResponseDto(savedBoard, new ArrayList<>());
    }

    @Transactional
    public BoardResponseDto updateBoard(Long postId, BoardRequestDto boardDto, SiteUser currentUser) {
        Board existingBoard = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + postId));

        if (!existingBoard.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 게시글을 수정할 권한이 없습니다.");
        }

        s3Uploader.delete(existingBoard.getMainImageUrl());

        for (Step step : existingBoard.getSteps()) {
            s3Uploader.delete(step.getStepImageUrl());
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

        Board savedBoard = boardRepository.save(existingBoard);

        List<CommentResponseDto> comments = commentService.getComments(postId);

        return new BoardResponseDto(savedBoard, comments);
    }

    @Transactional
    public void deleteBoard(Long postId, SiteUser currentUser) {
        Board existingBoard = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + postId));

        if (!existingBoard.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 게시글을 삭제할 권한이 없습니다.");
        }

        s3Uploader.delete(existingBoard.getMainImageUrl());

        for (Step step : existingBoard.getSteps()){
            s3Uploader.delete(step.getStepImageUrl());
        }
        boardRepository.delete(existingBoard);
    }

    // 통합된 메서드
    @Transactional(readOnly = true)
    public BoardListResponseDto findAllRecipes(String category, String sort) {
        List<Board> boards;
        long totalCount;

        Optional<RecipeCategory> recipeCategoryOpt = findCategoryByName(category);

        // 카테고리 필터링 로직
        if (recipeCategoryOpt.isPresent()) {
            RecipeCategory recipeCategory = recipeCategoryOpt.get();
            boards = boardRepository.findByCategoriesContaining(recipeCategory);
            totalCount = boardRepository.countByCategoriesContaining(recipeCategory);
        } else {
            // 필터링이 없거나 잘못된 카테고리인 경우 전체 조회
            boards = boardRepository.findAll(Sort.by(Sort.Direction.DESC, "createDate"));
            totalCount = boardRepository.count();
        }

        // 정렬 로직
        if ("popular".equalsIgnoreCase(sort)) {
            boards.sort(Comparator.comparingInt(board -> board.getLikes().size()));
            Collections.reverse(boards);
        }

        List<BoardResponseDto> boardDtos = boards.stream()
                .map(board -> {
                    List<CommentResponseDto> comments = commentService.getComments(board.getPostId());
                    return new BoardResponseDto(board, comments);
                })
                .collect(Collectors.toList());

        return new BoardListResponseDto(boardDtos, totalCount);
    }

    @Transactional(readOnly = true)
    public BoardResponseDto findBoardById(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        List<CommentResponseDto> parentComments = commentService.getComments(postId);
        BoardResponseDto responseDto = new BoardResponseDto(board, parentComments);

        responseDto.setLikeCount(board.getLikes().size());
        responseDto.setCommentCount(board.getComments().size());

        return responseDto;
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
                .map(board -> new BoardResponseDto(board, commentService.getComments(board.getPostId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> getScrapBoardsByUser(SiteUser user) {
        return scrapRepository.findByUser(user).stream()
                .map(RecipeScrap::getBoard)
                .map(board -> new BoardResponseDto(board, new ArrayList<>()))
                .collect(Collectors.toList());
    }

    //검색
    @Transactional(readOnly = true)
    public BoardListResponseDto findBoardByKeywords(String keywords) {
        List<Board> searchResults;

        if (keywords.contains(",")) {
            List<String> keywordList = Arrays.stream(keywords.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            Set<Board> uniqueBoards = new HashSet<>();
            for (String keyword : keywordList) {
                uniqueBoards.addAll(boardRepository.findBySingleKeyword(keyword));
            }
            searchResults = new ArrayList<>(uniqueBoards);
        } else {
            searchResults = boardRepository.findBySingleKeyword(keywords.trim());
        }
        List<BoardResponseDto> boardDtos = searchResults.stream()
                .map(board -> new BoardResponseDto(board, commentService.getComments(board.getPostId())))
                .collect(Collectors.toList());
        long totalCount = (long) searchResults.size();
        return new BoardListResponseDto(boardDtos, totalCount);
    }

    @Transactional(readOnly = true)
    public BoardListResponseDto getTop5PopularBoards() {
        List<Board> boards = boardRepository.findTop5ByOrderByLikesDesc();

        List<BoardResponseDto> boardDtos = boards.stream()
                .map(board -> {
                    return new BoardResponseDto(board, new ArrayList<>());
                })
                .collect(Collectors.toList());
        return new BoardListResponseDto(boardDtos, 5L);
    }

    //한글 카테고리명을 Enum으로 변환하는 헬퍼 메서드
    private Optional<RecipeCategory> findCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return Optional.empty();
        }

        switch (categoryName.toUpperCase()) {
            case "왕초보":
                return Optional.of(RecipeCategory.BEGINNER);
            case "전자레인지/에어프라이어":
                return Optional.of(RecipeCategory.MICROWAVE_AIRFRYER);
            case "디저트":
                return Optional.of(RecipeCategory.DESSERT);
            case "비건":
                return Optional.of(RecipeCategory.VEGAN);
            default:
                return Optional.empty();
        }
    }

    public List<BoardResponseDto> getMyBoards(Long userId) {
        List<Board> myBoards = boardRepository.findByAuthor_Id(userId);
        return myBoards.stream().map(BoardResponseDto::new).collect(Collectors.toList());
    }

    public long countMyBoards(Long userId){
        return boardRepository.countByAuthor_Id(userId);
    }

    public long countRecipeScrap(Long userId){
        return scrapRepository.countByUser_Id(userId);
    }
}
package hackathon.bigone.sunsak.recipe.board.service;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.recipe.board.dto.BoardDto;
import hackathon.bigone.sunsak.recipe.board.dto.IngredientDto;
import hackathon.bigone.sunsak.recipe.board.dto.RecipeLinkDto;
import hackathon.bigone.sunsak.recipe.board.dto.StepDto;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.entity.Ingredient;
import hackathon.bigone.sunsak.recipe.board.entity.RecipeLink;
import hackathon.bigone.sunsak.recipe.board.entity.Step;
import hackathon.bigone.sunsak.recipe.board.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    public Board create(BoardDto boardDto, SiteUser author) {
        Board newBoard = new Board();
        newBoard.setTitle(boardDto.getTitle());
        newBoard.setServings(boardDto.getServings());
        newBoard.setCookingTime(boardDto.getCookingTime());
        newBoard.setImageUrl(boardDto.getImageUrl());
        newBoard.setCategory(boardDto.getCategory());
        newBoard.setDescription(boardDto.getDescription());
        newBoard.setAuthor(author);

        // 재료
        if (boardDto.getIngredients() != null) {
            boardDto.getIngredients().forEach(ingredientDto -> {
                Ingredient newIngredient = new Ingredient();
                newIngredient.setName(ingredientDto.getName());
                newIngredient.setAmount(ingredientDto.getAmount());
                newIngredient.setBoard(newBoard);
                newBoard.getIngredients().add(newIngredient);
            });
        }

        // 단계
        if (boardDto.getSteps() != null) {
            boardDto.getSteps().forEach(stepDto -> {
                Step newStep = new Step();
                newStep.setStepNumber(stepDto.getStepNumber());
                newStep.setDescription(stepDto.getDescription());
                newStep.setBoard(newBoard);
                newBoard.getSteps().add(newStep);
            });
        }

        // 링크
        if (boardDto.getRecipeLinks() != null) {
            boardDto.getRecipeLinks().forEach(recipeLinkDto -> {
                RecipeLink newLink = new RecipeLink();
                newLink.setUrl(recipeLinkDto.getUrl());
                newLink.setBoard(newBoard);
                newBoard.getRecipeLink().add(newLink);
            });
        }

        return boardRepository.save(newBoard);

    }

    @Transactional
    public Board updateBoard(Long postId, BoardDto boardDto, SiteUser currentUser) {
        Board existingBoard = boardRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + postId));

        // 게시글 작성자와 현재 로그인한 사용자가 같은지 확인
        if (!existingBoard.getAuthor().equals(currentUser)) {
            throw new IllegalStateException("이 게시글을 수정할 권한이 없습니다.");
        }

        existingBoard.setTitle(boardDto.getTitle());
        existingBoard.setDescription(boardDto.getDescription());
        existingBoard.setServings(boardDto.getServings());
        existingBoard.setCookingTime(boardDto.getCookingTime());
        existingBoard.setImageUrl(boardDto.getImageUrl());
        existingBoard.setCategory(boardDto.getCategory());

        // 단계(Steps) 업데이트
        existingBoard.getSteps().clear();
        if (boardDto.getSteps() != null) {
            boardDto.getSteps().forEach(stepDto -> {
                Step newStep = new Step();
                newStep.setStepNumber(stepDto.getStepNumber());
                newStep.setDescription(stepDto.getDescription());
                newStep.setBoard(existingBoard);
                existingBoard.getSteps().add(newStep);
            });
        }

        // 재료(Ingredients) 업데이트
        existingBoard.getIngredients().clear();
        if (boardDto.getIngredients() != null) {
            boardDto.getIngredients().forEach(ingredientDto -> {
                Ingredient newIngredient = new Ingredient();
                newIngredient.setName(ingredientDto.getName());
                newIngredient.setAmount(ingredientDto.getAmount());
                newIngredient.setBoard(existingBoard);
                existingBoard.getIngredients().add(newIngredient);
            });
        }

        // 링크(Links) 업데이트
        existingBoard.getRecipeLink().clear();
        if (boardDto.getRecipeLinks() != null) {
            boardDto.getRecipeLinks().forEach(recipeLinkDto -> {
                RecipeLink newLink = new RecipeLink();
                newLink.setUrl(recipeLinkDto.getUrl());
                newLink.setBoard(existingBoard);
                existingBoard.getRecipeLink().add(newLink);
            });
        }

        return existingBoard;
    }
}
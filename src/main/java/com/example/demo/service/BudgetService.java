package com.example.demo.service;

import com.example.demo.dto.BudgetDTO;
import com.example.demo.entity.Budget;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.exception.BudgetExceededException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BudgetRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AlertService alertService;

    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUser_Id(userId);
    }

    public List<Budget> getBudgetsByUserAndMonth(Long userId, String monthYear) {
        return budgetRepository.findByUser_IdAndMonthYear(userId, monthYear);
    }

    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found."));
    }

    public Budget createBudget(BudgetDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + dto.getUserId() + " not found."));
        Category category = categoryRepository.findByName(dto.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category '" + dto.getCategory() + "' not found."));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setMonthlyLimit(dto.getMonthlyLimit());
        budget.setSpentAmount(0.0);
        budget.setMonthYear(dto.getMonthYear());
        budget.setAlertThreshold(dto.getAlertThreshold());
        return budgetRepository.save(budget);
    }

    public Budget updateBudget(Long id, BudgetDTO dto) {
        Budget budget = getBudgetById(id);
        Category category = categoryRepository.findByName(dto.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category '" + dto.getCategory() + "' not found."));
        budget.setCategory(category);
        budget.setMonthlyLimit(dto.getMonthlyLimit());
        budget.setMonthYear(dto.getMonthYear());
        budget.setAlertThreshold(dto.getAlertThreshold());
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id) {
        Budget budget = getBudgetById(id);
        budgetRepository.delete(budget);
    }

    public void updateSpentAmount(Long userId, String category, String monthYear, Double newTotal) {
        Budget budget = budgetRepository.findByUser_IdAndCategory_NameAndMonthYear(userId, category, monthYear)
                .orElse(null);
        if (budget == null) {
            return;
        }
        budget.setSpentAmount(newTotal);
        budgetRepository.save(budget);

        if (budget.getMonthlyLimit() != null && budget.getMonthlyLimit() > 0) {
            double percentUsed = (newTotal / budget.getMonthlyLimit()) * 100.0;
            int threshold = budget.getAlertThreshold() != null ? budget.getAlertThreshold() : 80;
            if (percentUsed >= threshold) {
                alertService.triggerBudgetAlert(userId, category, percentUsed);
            }
        }
    }

    /**
     * Called during expense creation: adds the new amount to the running spent total
     * for the user/category/month, updates the budget, and rejects the expense
     * with a BudgetExceededException if it would breach the monthly limit.
     */
    public void checkAndApplyExpense(Long userId, String category, String monthYear, Double amount) {
        Budget budget = budgetRepository.findByUser_IdAndCategory_NameAndMonthYear(userId, category, monthYear)
                .orElse(null);

        if (budget == null) {
            // No budget configured for this category/month — nothing to enforce.
            return;
        }

        double currentSpent = budget.getSpentAmount() != null ? budget.getSpentAmount() : 0.0;
        double newTotal = currentSpent + amount;

        if (budget.getMonthlyLimit() != null && newTotal > budget.getMonthlyLimit()) {
            throw new BudgetExceededException("Budget limit exceeded for " + category + ".");
        }

        updateSpentAmount(userId, category, monthYear, newTotal);
    }
}

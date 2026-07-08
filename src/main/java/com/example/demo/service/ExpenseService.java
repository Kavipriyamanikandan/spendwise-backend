package com.example.demo.service;

import com.example.demo.dto.ExpenseDTO;
import com.example.demo.entity.Expense;
import com.example.demo.entity.User;
import com.example.demo.exception.ExpenseNotFoundException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private CategoryClassifier categoryClassifier;

    public ExpenseService() {
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense with ID " + id + " not found."));
    }

    public List<Expense> getExpensesByUser(Long userId) {
        return expenseRepository.findByUser_Id(userId);
    }

    public List<Expense> getExpensesByMonth(Long userId, String month) {
        return expenseRepository.findByUser_IdAndMonth(userId, month);
    }

    public Expense createExpense(ExpenseDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + dto.getUserId() + " not found."));

        // Predict category (simple keyword-based classifier)
        String predicted = categoryClassifier.predict(dto.getTitle(), dto.getCategory());

        Expense expense = new Expense();
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDate(dto.getDate());
        expense.setTransactionRefNo(dto.getTransactionRefNo());
        expense.setNotes(dto.getNotes());
        expense.setPredictedCategory(predicted);
        expense.setUser(user);

        // Determine monthYear from date (expects "yyyy-MM-dd" or "yyyy-MM")
        String monthYear = extractMonthYear(dto.getDate());

        // Check budget threshold before persisting
        budgetService.checkAndApplyExpense(dto.getUserId(), dto.getCategory(), monthYear, dto.getAmount());

        return expenseRepository.save(expense);
    }

    public Expense updateExpense(Long id, ExpenseDTO dto) {
        Expense expense = getExpenseById(id);
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setDate(dto.getDate());
        expense.setTransactionRefNo(dto.getTransactionRefNo());
        expense.setNotes(dto.getNotes());
        expense.setPredictedCategory(categoryClassifier.predict(dto.getTitle(), dto.getCategory()));
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }

    private String extractMonthYear(String date) {
        if (date == null || date.length() < 7) {
            return date;
        }
        return date.substring(0, 7); // "yyyy-MM"
    }
}

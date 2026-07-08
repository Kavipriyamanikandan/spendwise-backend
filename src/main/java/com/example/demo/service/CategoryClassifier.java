package com.example.demo.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight keyword-based predictive classifier for expense categories.
 * Scans the expense title for known keywords and returns a predicted category tag.
 * Falls back to the user-supplied category when no keyword match is found.
 */
@Component
public class CategoryClassifier {

    private static final Map<String, String> KEYWORD_MAP = new LinkedHashMap<>();

    static {
        KEYWORD_MAP.put("uber", "Transport");
        KEYWORD_MAP.put("ola", "Transport");
        KEYWORD_MAP.put("fuel", "Transport");
        KEYWORD_MAP.put("petrol", "Transport");
        KEYWORD_MAP.put("taxi", "Transport");
        KEYWORD_MAP.put("flight", "Travel");
        KEYWORD_MAP.put("hotel", "Travel");
        KEYWORD_MAP.put("restaurant", "Food & Dining");
        KEYWORD_MAP.put("cafe", "Food & Dining");
        KEYWORD_MAP.put("coffee", "Food & Dining");
        KEYWORD_MAP.put("swiggy", "Food & Dining");
        KEYWORD_MAP.put("zomato", "Food & Dining");
        KEYWORD_MAP.put("grocery", "Groceries");
        KEYWORD_MAP.put("supermarket", "Groceries");
        KEYWORD_MAP.put("rent", "Housing");
        KEYWORD_MAP.put("electricity", "Utilities");
        KEYWORD_MAP.put("water bill", "Utilities");
        KEYWORD_MAP.put("internet", "Utilities");
        KEYWORD_MAP.put("mobile recharge", "Utilities");
        KEYWORD_MAP.put("movie", "Entertainment");
        KEYWORD_MAP.put("netflix", "Entertainment");
        KEYWORD_MAP.put("spotify", "Entertainment");
        KEYWORD_MAP.put("pharmacy", "Healthcare");
        KEYWORD_MAP.put("hospital", "Healthcare");
        KEYWORD_MAP.put("doctor", "Healthcare");
        KEYWORD_MAP.put("medicine", "Healthcare");
        KEYWORD_MAP.put("tuition", "Education");
        KEYWORD_MAP.put("course", "Education");
        KEYWORD_MAP.put("book", "Education");
        KEYWORD_MAP.put("shopping", "Shopping");
        KEYWORD_MAP.put("amazon", "Shopping");
        KEYWORD_MAP.put("flipkart", "Shopping");
    }

    public String predict(String title, String fallbackCategory) {
        if (title != null) {
            String lower = title.toLowerCase();
            for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return fallbackCategory;
    }
}

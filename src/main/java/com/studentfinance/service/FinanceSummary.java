package com.studentfinance.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public record FinanceSummary(long totalIncome, long totalExpenses, long balance,
                             long monthlyIncome, long monthlyExpenses, YearMonth month,
                             Map<String, Long> categoryExpenses, Map<LocalDate, Long> dailyExpenses,
                             Map<YearMonth, Long> monthlyHistory) { }

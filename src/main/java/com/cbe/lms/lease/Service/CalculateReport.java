package com.cbe.lms.lease.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CalculateReport {

    public static String calculateReportM(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment, double advancePayment,
            double discountRate, double leaseLiability, LocalDate contractRegisteredDate, String installmentDetails,
            long BranchId, String contractType, LocalDate contractEndDates, LocalDate contractStartDates,
            String branchName) {
        double constDepreciationPerM;
        int monthBetweens = (int) ReportUtility.monthBetween(contractRegisteredDate, contractEndDates);
        double advancePayments = advancePayment;

        if (installmentDetails != null) {
            Entry<Map<LocalDate, BigDecimal>, Integer> result = parseInstallmentDetails(installmentDetails);
            Map<LocalDate, BigDecimal> installmentMap = result.getKey();

            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();
                if (installmentDate.isBefore(contractRegisteredDate)
                        && ReportUtility.getNextTerm(contractRegisteredDate, "yearly")
                                .isEqual(ReportUtility.getNextTerm(installmentDate, "yearly"))) {

                    advancePayments += futureLeasePayments;

                } else {
                }
            }

            if (totalPayment == advancePayments) {
                constDepreciationPerM = totalPayment / ReportUtility.monthBetween(startDate, endDate);
            } else {
                constDepreciationPerM = depreciationPerMonth;
            }

        } else {
            if (totalPayment == advancePayment) {
                constDepreciationPerM = totalPayment / ReportUtility.monthBetween(startDate, endDate);
            } else {
                constDepreciationPerM = depreciationPerMonth;
            }
        }

        double[] depreciation = calculateDepreciation(totalPayment, advancePayment, depreciationPerMonth, startDate,
                endDate);
        // double constDepreciationPerM = depreciation[0];
        double constDepreciationPerY = depreciation[1];

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();
        JSONArray combinedArray = new JSONArray();

        JSONObject detail = contractDetail(Id, startDate, endDate, rightOfUse, totalPayment, advancePayment,
                leaseLiability, contractRegisteredDate, BranchId, contractType, branchName, constDepreciationPerM,
                constDepreciationPerY);
        detailArray.put(detail);

        double depreciationF;
        double depreciationFirst;

        if (ReportUtility.getNextTerm(startDate, "monthly")
                .equals(ReportUtility.getNextTerm(contractRegisteredDate, "monthly"))) {
            depreciationF = ReportUtility.monthBetween(startDate,
                    ReportUtility.getNextTerm(contractRegisteredDate, "yearly"))
                    - (int) ReportUtility.monthBetween(startDate,
                            ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));
        } else {
            depreciationF = ReportUtility.monthBetween(startDate,
                    ReportUtility.getNextTerm(contractRegisteredDate, "monthly"));
        }

        depreciationFirst = depreciationF * constDepreciationPerM;

        Entry<Map<LocalDate, BigDecimal>, Integer> result = parseInstallmentDetails(installmentDetails);
        Map<LocalDate, BigDecimal> installmentMap = result.getKey();
        int numberOfInstallments = result.getValue();

        String contractTerm = "monthly";
        calculateInterestExpense(installmentMap, leaseLiability,
                discountRate, ammortizationArray,
                combinedArray, startDate,
                endDate, numberOfInstallments, contractRegisteredDate, contractTerm);

        monthBetweens += 2;
        double monthlyBlance = 0;

        for (int i = 0; i <= monthBetweens; i++) {
            if (i == 0) {
                JSONObject initial = new JSONObject();
                initial.put("year", contractStartDates.toString());
                initial.put("deprecationExp", 0);
                reportArray.put(initial);

            } else if (i == 1) {
                JSONObject FirstYear = new JSONObject();
                FirstYear.put("year", ReportUtility.getNextTerm(contractRegisteredDate,
                        "monthly").toString());
                FirstYear.put("deprecationExp", depreciationFirst);
                reportArray.put(FirstYear);
                startDate = ReportUtility.getNextTerm(contractRegisteredDate, "monthly");
                monthlyBlance += depreciationFirst;
            } else if (i == monthBetweens) {

                double depreciationLast = rightOfUse - monthlyBlance;

                JSONObject finalYear = new JSONObject();
                finalYear.put("year", ReportUtility.getNextTerm(startDate, "monthly").toString());
                finalYear.put("deprecationExp", depreciationLast);
                reportArray.put(finalYear);
                break;
            } else {
                if ((monthlyBlance + constDepreciationPerM > rightOfUse)) {
                    continue;
                } else {
                    JSONObject reportEntry = new JSONObject();
                    reportEntry.put("year", ReportUtility.getNextTerm(startDate, "monthly").toString());
                    reportEntry.put("deprecationExp",
                            constDepreciationPerM);
                    reportArray.put(reportEntry);
                    startDate = ReportUtility.getNextTerm(startDate, "monthly");
                    monthlyBlance += constDepreciationPerM;
                }
            }

        }

        // Loop through the years in ammortizationArray
        for (int i = 0; i < ammortizationArray.length(); i++) {
            JSONObject ammortizationEntry = ammortizationArray.getJSONObject(i);

            String year = ammortizationEntry.get("year").toString();
            // System.out.println("year: " + ammortizationEntry.get("year").toString());

            // Convert to LocalDate
            // if (ammortizationEntry.has("year")) {
            // year = ammortizationEntry.get("year").toString();
            // // Proceed with your logic
            // } else {
            // System.out.println("detailArray Array: " + detailArray.toString());
            // // Handle missing year case, e.g., skip the entry or assign a default value
            // continue;
            // }
            boolean matched = false;

            // Find corresponding entry in reportArray
            for (int j = 0; j < reportArray.length(); j++) {
                JSONObject reportEntry = reportArray.getJSONObject(j);
                // LocalDate reportYear = LocalDate.parse(reportEntry.getString("year")); //
                // Convert to LocalDate
                if (reportEntry.getString("year").equals(year)) {
                    // Combine the information
                    JSONObject combinedEntry = new JSONObject();
                    combinedEntry.put("year", year); // Convert back to string
                    combinedEntry.put("deprecationExp", reportEntry.getDouble("deprecationExp"));
                    combinedEntry.put("interestExpence", ammortizationEntry.get("interestExpence"));

                    // if ((double) ammortizationEntry.get("interestExpence") < 0) {
                    // System.out.println(detailArray);
                    // }
                    combinedArray.put(combinedEntry);
                    matched = true;
                    break;
                }
            }

            // If no match was found, add the entry from ammortizationArray
            if (!matched) {
                JSONObject combinedEntry = new JSONObject();
                combinedEntry.put("year", year); // Convert back to string
                combinedEntry.put("deprecationExp", 0); // Set default value for deprecationExp
                combinedEntry.put("interestExpence", ammortizationEntry.get("interestExpence"));

                // Add additional fields if needed
                // combinedEntry.put("additionalField", "your_additional_value_here");

                combinedArray.put(combinedEntry);
            }
        }

        // Loop through the years in reportArray
        for (int i = 0; i < reportArray.length(); i++) {
            JSONObject reportEntry = reportArray.getJSONObject(i);
            LocalDate year = LocalDate.parse(reportEntry.getString("year")); // Convert to LocalDate

            boolean matched = false;

            // Check if the year already exists in combinedArray
            for (int j = 0; j < combinedArray.length(); j++) {
                JSONObject combinedEntry = combinedArray.getJSONObject(j);
                LocalDate combinedYear;
                if (combinedEntry.has("year")) {
                    combinedYear = LocalDate.parse(combinedEntry.getString("year")); // Convert to LocalDate
                    // Proceed with your logic
                } else {
                    // System.out.println("Missing 'year' key in entry: " +
                    // combinedEntry.toString());
                    // Handle missing year case, e.g., skip the entry or assign a default value
                    continue;
                }

                if (combinedYear.equals(year)) {
                    matched = true;
                    break;
                }
            }

            // If no match was found, add the entry from reportArray
            if (!matched) {
                JSONObject combinedEntry = new JSONObject();
                combinedEntry.put("year", year.toString()); // Convert back to string
                combinedEntry.put("deprecationExp", reportEntry.getDouble("deprecationExp"));
                combinedEntry.put("interestExpence", 0); // Set default value for interestExpence

                // Add additional fields if needed
                // combinedEntry.put("additionalField", "your_additional_value_here");

                combinedArray.put(combinedEntry);
            }
        }

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("combinedArray", combinedArray);
        return reportObject.toString();

    }

    private static void calculateInterestExpense(Map<LocalDate, BigDecimal> installmentMap, double leaseLiability,
            double discountRate, JSONArray amortizationArray, JSONArray summaryArray, LocalDate startDate,
            LocalDate endDate, int numberOfInstallment, LocalDate contractRegisteredDate, String reportType) {

        double balance = leaseLiability;
        double leasePayment = 0;
        double interestExpense;
        int count = 1;
        LocalDate contractStartDates = startDate;
        // int timeBetween = 0;

        // Determine the period (yearly or monthly) based on reportType
        String period = reportType.equalsIgnoreCase("yearly") ? "yearly" : "monthly";
        double depreciationF = ReportUtility.monthBetween(startDate,
                ReportUtility.getNextTerm(contractRegisteredDate, period));
        double depreciationFY = ReportUtility.monthBetween(startDate,
                ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));

        JSONObject expenseEntry = new JSONObject();

        for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
            LocalDate installmentDate = entry.getKey();
            BigDecimal installmentAmount = entry.getValue();
            double futureLeasePayments = installmentAmount.doubleValue();

            int timeBetween = ReportUtility.yearsBetween(
                    contractRegisteredDate,
                    ReportUtility.getNextTerm(installmentDate, "yearly"));
            double initialLeaseLiability = leaseLiability;
            boolean isFinalInstallment = count + 1 == numberOfInstallment + 1;

            // both
            for (int i = 0; i <= timeBetween; i++) {
                // Handle first-year entry
                if (i == 0 && count == 1) {
                    balance = leaseLiability;
                    JSONObject finalYear = new JSONObject();
                    finalYear.put("year", contractRegisteredDate);
                    finalYear.put("interestExpence", "-");
                    finalYear.put("leasePayment", leasePayment);
                    finalYear.put("balance", balance);
                    amortizationArray.put(finalYear);

                }

                // Handle final year entry for the last installment
                else if (i == timeBetween && isFinalInstallment) {
                    startDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    leasePayment = futureLeasePayments;

                    interestExpense = futureLeasePayments - balance;
                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / 12;
                        for (int month = 1; month <= 12; month++) {
                            startDate = ReportUtility.getNextTerm(startDate, "monthly");
                            expenseEntry = createReportEntry(startDate.toString(), leasePayment, 0,
                                    interestExpense);
                            amortizationArray.put(expenseEntry);
                            summaryArray.put(expenseEntry);

                        }

                    } else {
                        expenseEntry = createReportEntry(startDate.toString(),
                                leasePayment, 0, interestExpense);
                        amortizationArray.put(expenseEntry);
                        summaryArray.put(expenseEntry);

                    }

                    break;
                } else if (i == 1 && count == 1 && ReportUtility.getNextTerm(contractRegisteredDate, "yearly")
                        .isEqual(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
                    continue;
                }
                // Calculate for the rest of the installments (excluding the last one)
                else if (i == timeBetween && !isFinalInstallment) {
                    startDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
                    leasePayment = futureLeasePayments;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            numberOfMonths);
                    balance = balance + interestExpense - futureLeasePayments;

                    // if (period.equals("monthly")) {
                    // interestExpense = interestExpense / 12;
                    // for (int month = 1; month <= 12; month++) {
                    // startDate = ReportUtility.getNextTerm(startDate, "monthly");
                    // expenseEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                    // interestExpense);
                    // amortizationArray.put(expenseEntry);
                    // summaryArray.put(expenseEntry);

                    // }
                    // endDate = ReportUtility.getNextTerm(endDate, "monthly");
                    // } else {
                    // expenseEntry = createReportEntry(startDate.toString(),
                    // leasePayment, balance, interestExpense);
                    // amortizationArray.put(expenseEntry);
                    // summaryArray.put(expenseEntry);
                    // endDate = ReportUtility.getNextTerm(endDate, "yearly");
                    // }
                    expenseEntry = createReportEntry(startDate.toString(),
                            leasePayment, balance, interestExpense);
                    amortizationArray.put(expenseEntry);
                    summaryArray.put(expenseEntry);
                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    endDate = ReportUtility.getNextTerm(endDate, "yearly");
                    System.out.println(i);

                }

                //
                // Handle the calculation for the first installment period
                else if (i == 1 && count == 1) {
                    startDate = ReportUtility.getNextTerm(contractRegisteredDate, "monthly");
                    interestExpense = ReportUtility.calculateInterestExpense(initialLeaseLiability, discountRate,
                            leasePayment, depreciationFY);
                    double numberOfmonths = ReportUtility.monthBetween(contractStartDates,
                            ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));

                    balance += interestExpense;
                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / depreciationFY;
                        for (int month = 1; month <= depreciationFY + 1; month++) {
                            expenseEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                    interestExpense);
                            amortizationArray.put(expenseEntry);
                            summaryArray.put(expenseEntry);
                            startDate = ReportUtility.getNextTerm(startDate, "monthly");

                        }
                        // endDate = ReportUtility.getNextTerm(startDate, "monthly");
                    } else {
                        expenseEntry = createReportEntry(startDate.toString(),
                                leasePayment, balance, interestExpense);
                        amortizationArray.put(expenseEntry);
                        summaryArray.put(expenseEntry);
                        // endDate = startDate;
                    }
                    // expenseEntry = createReportEntry(startDate.toString(),
                    // leasePayment, balance, interestExpense);
                    // amortizationArray.put(expenseEntry);
                    // summaryArray.put(expenseEntry);
                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);

                } else if (leasePayment == 0 && count == 1 && !ReportUtility.getNextTerm(endDate, "yearly")
                        .isEqual(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
                    startDate = endDate;
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    leasePayment = 0;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            ReportUtility.monthBetween(startDate, endDate));
                    balance += interestExpense;
                    // interestExpensepermonth = interestExpense / 12;

                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / depreciationFY;
                        for (int month = 1; month <= depreciationFY + 1; month++) {
                            expenseEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                    interestExpense);
                            amortizationArray.put(expenseEntry);
                            summaryArray.put(expenseEntry);
                            startDate = ReportUtility.getNextTerm(startDate, "monthly");

                        }
                        // endDate = ReportUtility.getNextTerm(startDate, "monthly");
                    }

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                }
                // Process final interest calculations if no future payments remain
                else if (isFinalInstallment
                        && !endDate.equals(ReportUtility.getNextTerm(installmentDate, "yearly"))) {

                    startDate = endDate;
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
                    leasePayment = 0;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            numberOfMonths);
                    balance += interestExpense;
                    expenseEntry = createReportEntry(startDate.toString(),
                            leasePayment, balance, interestExpense);
                    amortizationArray.put(expenseEntry);
                    summaryArray.put(expenseEntry);
                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);

                }
                // Handle intermediate years' interest calculations
                else if (i > 1 && count > 0 && !isFinalInstallment
                        && !endDate.equals(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
                    startDate = endDate;
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            numberOfMonths);
                    balance += interestExpense;
                    // if (period.equals("monthly")) {
                    // interestExpense = interestExpense / 12;
                    // for (int month = 1; month <= 12; month++) {
                    // startDate = ReportUtility.getNextTerm(startDate, "monthly");
                    // expenseEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                    // interestExpense);
                    // amortizationArray.put(expenseEntry);
                    // summaryArray.put(expenseEntry);

                    // }

                    // } else {
                    // expenseEntry = createReportEntry(startDate.toString(),
                    // leasePayment, balance, interestExpense);
                    // amortizationArray.put(expenseEntry);
                    // summaryArray.put(expenseEntry);

                    // }

                    expenseEntry = createReportEntry(startDate.toString(),
                            leasePayment, balance, interestExpense);
                    amortizationArray.put(expenseEntry);
                    summaryArray.put(expenseEntry);

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                }
            }

            count += 1;
            if (isFinalInstallment) {
                break;
            }
        }

    }

    private static JSONObject createReportEntry(String date, double leasePayment, double balance,
            double interestExpense) {
        JSONObject entry = new JSONObject();
        entry.put("year", date);
        entry.put("leasePayment", leasePayment);
        entry.put("balance", balance);
        entry.put("interestExpence", interestExpense);

        return entry;
    }

    private static JSONObject contractDetail(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double totalPayment, double advancePayment, double leaseLiability, LocalDate contractRegisteredDate,
            long BranchId, String contractType, String branchName, Double constDepreciationPerM,
            Double constDepreciationPerY) {
        JSONObject detail = new JSONObject();
        detail.put("contractMonth", ReportUtility.monthBetween(startDate, endDate));
        detail.put("contractYear", ReportUtility.yearsBetween(startDate, endDate));
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("branchName", branchName);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", constDepreciationPerM);
        detail.put("depreciationPerMonth", constDepreciationPerY);
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detail.put("contractRegisteredDate", contractRegisteredDate);
        detail.put("startDate", startDate);
        detail.put("endDate", endDate);

        return detail;
    }

    public static double[] contractDuration(LocalDate contractStartDates, LocalDate contractEndDates,
            LocalDate contractRegisteredDate) {
        double totalnumberofmonths = ReportUtility.monthBetween(contractStartDates, contractEndDates);
        double yearbetween = ReportUtility.yearsBetween(contractRegisteredDate, contractEndDates);
        double yearbetweens = 0;
        if (contractRegisteredDate.isEqual(ReportUtility.getNextTerm(contractRegisteredDate, "yearly").minusYears(1))) {
            yearbetween -= 1;
        }
        if (totalnumberofmonths <= 12) {
            yearbetween += 2;
        } else if (ReportUtility.getNextTerm(contractStartDates, "yearly").isEqual(contractRegisteredDate)) {
            yearbetweens = 2;
        } else if (ReportUtility.getNextTerm(contractStartDates, "yearly")
                .isBefore(ReportUtility.getNextTerm(contractRegisteredDate, "yearly"))) {

            yearbetweens = 1;

        } else if (ReportUtility.getNextTerm(contractStartDates, "yearly")
                .isEqual(ReportUtility.getNextTerm(contractRegisteredDate, "yearly"))) {
            yearbetweens = 2;
        }

        return new double[] { yearbetween + yearbetweens };
    }

    private static void generateDepreciationReport(LocalDate startDate, LocalDate endDate, double rightOfUse,
            double constDepreciationPerY, double constDepreciationPerM, JSONArray reportArray,
            JSONArray summaryArray, LocalDate contractRegisteredDate) {
        double balance = rightOfUse;

        double depreciationF = ReportUtility.monthBetween(startDate,
                ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));
        double depreciationBeforeLast = 0;
        double depreciationFirstPeriod = depreciationF * constDepreciationPerM;
        double firstRemainingMonth = ReportUtility.monthBetween(startDate,
                ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));
        double yearbetween = contractDuration(startDate, endDate, contractRegisteredDate)[0] + 1;
        double numberofmonths = 0;
        double totalnumberofmonths = ReportUtility.monthBetween(startDate, endDate);

        for (int i = 0; i < yearbetween; i++) {
            if (i == 0) {
                balance = rightOfUse;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", startDate.toString());
                finalYear.put("balance", balance);
                finalYear.put("deprecationExp", 0);
                finalYear.put("months", "-");
                reportArray.put(finalYear);
            } else if (i == 1) {
                balance = rightOfUse - depreciationFirstPeriod;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", ReportUtility.getNextTerm(contractRegisteredDate, "yearly").toString());
                finalYear.put("balance", balance);
                finalYear.put("deprecationExp", depreciationFirstPeriod);
                finalYear.put("months", firstRemainingMonth);
                numberofmonths += firstRemainingMonth;
                reportArray.put(finalYear);
                summaryArray.put(finalYear);
                startDate = ReportUtility.getNextTerm(contractRegisteredDate, "yearly");
            } else if (yearbetween == 1) {
                depreciationBeforeLast = balance;
                double lastmonth = totalnumberofmonths - numberofmonths;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", ReportUtility.getNextTerm(startDate, "yearly").toString());
                finalYear.put("balance", 0);
                finalYear.put("deprecationExp", depreciationBeforeLast);
                finalYear.put("months", lastmonth);
                summaryArray.put(finalYear);
                reportArray.put(finalYear);

                break;
            } else if (i == (yearbetween - 1) || (startDate == ReportUtility.getNextTerm(endDate, "yearly"))) {
                depreciationBeforeLast = balance;
                double lastmonth = totalnumberofmonths - numberofmonths;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", ReportUtility.getNextTerm(startDate, "yearly").toString());
                finalYear.put("balance", 0);
                finalYear.put("deprecationExp", depreciationBeforeLast);
                finalYear.put("months", lastmonth);
                summaryArray.put(finalYear);
                reportArray.put(finalYear);
                break;
            } else {
                if (numberofmonths + 12 > totalnumberofmonths) {
                    continue;
                } else {
                    balance -= constDepreciationPerY;
                    JSONObject reportEntry = new JSONObject();
                    reportEntry.put("year", ReportUtility.getNextTerm(startDate, "yearly").toString());
                    reportEntry.put("balance", balance);

                    reportEntry.put("deprecationExp", constDepreciationPerY);
                    reportEntry.put("months", "12");
                    reportArray.put(reportEntry);
                    summaryArray.put(reportEntry);
                    startDate = ReportUtility.getNextTerm(startDate, "yearly");
                    numberofmonths += 12;
                }

            }

        }
    }

    private static double[] calculateDepreciation(double totalPayment, double advancePayment,
            double depreciationPerMonth,
            LocalDate startDate, LocalDate endDate) {
        double constDepreciationPerM;
        double constDepreciationPerY;
        double totalMonths = ReportUtility.monthBetween(startDate, endDate);

        if (totalPayment == advancePayment) {
            constDepreciationPerM = totalPayment / totalMonths;
        } else {
            constDepreciationPerM = depreciationPerMonth;
        }
        constDepreciationPerY = constDepreciationPerM * 12;

        return new double[] { constDepreciationPerM, constDepreciationPerY };
    }

    private static Entry<Map<LocalDate, BigDecimal>, Integer> parseInstallmentDetails(String installmentDetails) {

        Map<LocalDate, BigDecimal> installmentMap = new TreeMap<>();
        int numberOfInstallments = 0;
        if (installmentDetails != null) {
            JSONObject jsonObject = new JSONObject(installmentDetails);
            for (String key : jsonObject.keySet()) {
                BigDecimal value = jsonObject.getBigDecimal(key);
                LocalDate date = LocalDate.parse(key);
                installmentMap.put(date, value);
                numberOfInstallments++;
            }
        }

        return new AbstractMap.SimpleEntry<>(installmentMap, numberOfInstallments);
    }

    public static String calculateReportY(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment,
            double advancePayment, double discountRate, double leaseLiability,
            LocalDate contractRegisteredDate, String installmentDetails,
            long BranchId, String contractType, LocalDate contractStartDates,
            LocalDate contractEndDates, String branchName) {

        String contractTerm = "yearly";
        Entry<Map<LocalDate, BigDecimal>, Integer> result = parseInstallmentDetails(installmentDetails);
        Map<LocalDate, BigDecimal> installmentMap = result.getKey();
        int numberOfInstallments = result.getValue();

        double[] depreciation = calculateDepreciation(totalPayment, advancePayment, depreciationPerMonth, startDate,
                endDate);
        double constDepreciationPerM = depreciation[0];
        double constDepreciationPerY = depreciation[1];

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray summaryArray = new JSONArray();
        JSONArray detailArray = new JSONArray();
        JSONObject detail = contractDetail(Id, startDate, endDate, rightOfUse, totalPayment, advancePayment,
                leaseLiability, contractRegisteredDate, BranchId, contractType, branchName, constDepreciationPerM,
                constDepreciationPerY);
        detailArray.put(detail);

        generateDepreciationReport(startDate, endDate, rightOfUse, constDepreciationPerY, constDepreciationPerM,
                reportArray, summaryArray, contractRegisteredDate);
        calculateInterestExpense(installmentMap, leaseLiability, discountRate, ammortizationArray, summaryArray,
                contractStartDates, contractEndDates, numberOfInstallments, contractRegisteredDate, contractTerm);

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("summaryArray", summaryArray);
        return reportObject.toString();

    }

}

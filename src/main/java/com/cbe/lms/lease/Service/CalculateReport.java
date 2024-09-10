package com.cbe.lms.lease.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CalculateReport {


    public static Map<LocalDate, BigDecimal> createInstallmentMap(JSONObject jsonObject) {
        Map<LocalDate, BigDecimal> installmentMap = new TreeMap<>();
        for (String key : jsonObject.keySet()) {
            BigDecimal value = jsonObject.getBigDecimal(key);
            LocalDate date = LocalDate.parse(key);
            installmentMap.put(date, value);
        }
        return installmentMap;
    }
    
    public static String calculateReportM(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment, double advancePayment,
            double discountRate, double leaseLiability, LocalDate contractRegisteredDate, String installmentDetails,
            long BranchId, String contractType, LocalDate contractEndDates, LocalDate contractStartDates,
            String branchName) {
        double constDepreciationPerM = 0;
        int monthBetweens = (int) monthBetween(contractRegisteredDate, contractEndDates);
        double advancePayments = advancePayment;

        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);

            Map<LocalDate, BigDecimal> installmentMap = createInstallmentMap(jsonObject);

            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();
                if (installmentDate.isBefore(contractRegisteredDate) && getNextTerm(contractRegisteredDate, "yearly")
                        .isEqual(getNextTerm(installmentDate, "yearly"))) {

                    advancePayments += futureLeasePayments;

                } else {
                }

            }

            if (totalPayment == advancePayments) {
                constDepreciationPerM = totalPayment / monthBetween(startDate, endDate);
            } else {
                constDepreciationPerM = depreciationPerMonth;
            }

        } else {
            if (totalPayment == advancePayment) {
                constDepreciationPerM = totalPayment / monthBetween(startDate, endDate);
            } else {
                constDepreciationPerM = depreciationPerMonth;
            }
        }

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();

        JSONObject detail = new JSONObject();
        detail.put("contractMonth", monthBetween(startDate, endDate));
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("branchName", branchName);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", constDepreciationPerM);
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detailArray.put(detail);
        double depreciationF = 0;
        double depreciationFirst = 0;
        double depreciationFirst1 = 0;
        double balance = 0;

        if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            // Handle the case where lease starts at the end of a month
            depreciationFirst = constDepreciationPerM;
        } else {
            // Handle the case where lease starts at the beginning of a month
            depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, "monthly"));

            depreciationFirst1 = depreciationF *
                    constDepreciationPerM;

            depreciationFirst = depreciationFirst1;
        }

        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);

            // Create a map to store installment dates and amounts
            Map<LocalDate, BigDecimal> installmentMap = new TreeMap<>();

            // Iterate through the JSON object and populate the map
            int numberofinstallment = 0;
            for (String key : jsonObject.keySet()) {
                BigDecimal value = jsonObject.getBigDecimal(key);
                LocalDate date = LocalDate.parse(key); // Parse the date string into a LocalDate
                installmentMap.put(date, value);
                numberofinstallment += 1;

            }

            // Now you have a map where keys are LocalDate objects and values are
            // installment amounts
            // You can access them as needed
            double leasePayment = 0;
            double interestExpense = 0;
            int count = 1;
            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();

                int yearsBetween = yearsBetween(contractStartDates, getNextTerm(installmentDate, "yearly"));
                double initialLeaseLiability = leaseLiability;
                double interestExpensepermonth = 0;
                double firstMonthInterestExpence = 0;
                double numberOfmonths = 0;

                for (int i = 0; i <= yearsBetween; i++) {

                    if (i == 0 && count == 1) {

                        balance = leaseLiability;
                        JSONObject finalYear = new JSONObject();
                        finalYear.put("year", contractRegisteredDate);
                        finalYear.put("interestExpence", "-");
                        finalYear.put("leasePayment", leasePayment);
                        finalYear.put("balance", balance);
                        ammortizationArray.put(finalYear);
                        // endDate = getNextTerm(contractRegisteredDate, "monthly");

                    } else if (i == yearsBetween && count == numberofinstallment) {
                        startDate = getNextTerm(installmentDate, "yearly").minusYears(1);
                        endDate = getNextTerm(startDate, "yearly");
                        leasePayment = futureLeasePayments;
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        interestExpense = futureLeasePayments - balance;

                        interestExpensepermonth = interestExpense / 12;
                        for (int month = 1; month <= 12; month++) {
                            startDate = getNextTerm(startDate, "monthly");

                            JSONObject reportEntry = new JSONObject();
                            reportEntry.put("year", startDate);
                            reportEntry.put("leasePayment", leasePayment);
                            reportEntry.put("balance", "-");
                            reportEntry.put("interestExpence",
                                    interestExpensepermonth);
                            ammortizationArray.put(reportEntry);
                        }
                        break;
                    } else if (i == 1 && count == 1 && (getNextTerm(contractRegisteredDate, "yearly")
                            .isEqual(getNextTerm(installmentDate, "yearly")))) {

                        continue;

                    } else if (i == yearsBetween && count != numberofinstallment) {
                        LocalDate constarDate = getNextTerm(installmentDate, "yearly").minusYears(1).plusMonths(1);

                        startDate = constarDate;
                        endDate = getNextTerm(startDate, "yearly");
                        numberOfmonths = monthBetween(constarDate, endDate) + 1;
                        leasePayment = futureLeasePayments;
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, numberOfmonths);
                        balance = balance + interestExpense - leasePayment;
                        interestExpensepermonth = interestExpense
                                / 12;
                        for (int month = 0; month < 12; month++) {
                            startDate = getNextTerm(startDate, "monthly");

                            JSONObject reportEntry = new JSONObject();
                            reportEntry.put("year", startDate);
                            reportEntry.put("leasePayment", leasePayment);
                            reportEntry.put("balance", balance);
                            reportEntry.put("interestExpence",
                                    interestExpensepermonth);

                            ammortizationArray.put(reportEntry);
                        }
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        endDate = getNextTerm(startDate, "monthly");

                    } else if (i == 1 && count == 1) {
                        startDate = getNextTerm(contractRegisteredDate, "monthly");
                        numberOfmonths = monthBetween(contractStartDates,
                                getNextTerm(contractRegisteredDate, "yearly"));

                        double expenceMonths = monthBetween(contractRegisteredDate,
                                getNextTerm(contractRegisteredDate, "yearly"));
                        interestExpense = calculateInterestExpense(initialLeaseLiability,
                                discountRate, leasePayment, numberOfmonths);

                        balance += interestExpense;
                        if (numberOfmonths < 1) {
                            interestExpensepermonth = interestExpense;
                        } else {
                            interestExpensepermonth = interestExpense
                                    / numberOfmonths;

                
                        }

                        for (int month = 0; month < expenceMonths; month++) {
                            JSONObject reportEntry = new JSONObject();

                            if (month == 0) {
                                firstMonthInterestExpence = interestExpensepermonth * ((int) depreciationF + 1);
                                reportEntry.put("year", startDate);
                                reportEntry.put("balance", balance);
                                reportEntry.put("interestExpence",
                                        firstMonthInterestExpence);
                                ammortizationArray.put(reportEntry);
                                startDate = getNextTerm(startDate, "monthly");
                            } else {
                                reportEntry.put("year", startDate);
                                reportEntry.put("balance", balance);
                                reportEntry.put("interestExpence",
                                        interestExpensepermonth);
                                ammortizationArray.put(reportEntry);
                                startDate = getNextTerm(startDate, "monthly");
                            }

                        }

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        endDate = startDate;

                    }

                    else if ((count == numberofinstallment)
                            && !getNextTerm(endDate, "yearly").equals(getNextTerm(installmentDate, "yearly"))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        numberOfmonths = monthBetween(startDate, endDate);
                        leasePayment = 0;

                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, numberOfmonths);
                        balance += interestExpense;
                        interestExpensepermonth = interestExpense
                                / numberOfmonths;
                        for (int month = 1; month <= 12; month++) {

                            JSONObject reportEntry = new JSONObject();
                            reportEntry.put("year", startDate);
                            reportEntry.put("balance", balance);
                            reportEntry.put("interestExpence",
                                    interestExpensepermonth);
                            ammortizationArray.put(reportEntry);
                            startDate = getNextTerm(startDate, "monthly");

                        }

                        endDate = startDate;

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                    }
                    
                    else if (i > 1 && count > 1 && count != numberofinstallment
                            && !endDate.equals(getNextTerm(installmentDate, "yearly"))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        // numberOfmonths = monthBetween(startDate, endDate);
                        leasePayment = futureLeasePayments;
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, monthBetween(startDate, endDate));
                        balance += interestExpense;
                        interestExpensepermonth = interestExpense
                                / 12;

                        for (int month = 1; month <= 12; month++) {

                            JSONObject reportEntry = new JSONObject();
                            reportEntry.put("year", startDate);
                            reportEntry.put("leasePayment", leasePayment);

                            reportEntry.put("balance", balance);
                            reportEntry.put("interestExpence",
                                    interestExpensepermonth);
                            ammortizationArray.put(reportEntry);
                            startDate = getNextTerm(startDate, "monthly");

                        }
                        i = i + yearsBetween - 1;
                        endDate = startDate;

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                    } else if (leasePayment == 0 && count == 1
                            && !(getNextTerm(endDate, "yearly").isEqual(getNextTerm(installmentDate, "yearly")))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        // numberOfmonths = monthBetween(startDate, endDate);
                        leasePayment = 0;
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, monthBetween(startDate, endDate));
                        balance += interestExpense;
                        interestExpensepermonth = interestExpense
                                / 12;

                        for (int month = 1; month <= 12; month++) {

                            JSONObject reportEntry = new JSONObject();
                            reportEntry.put("year", startDate);
                            reportEntry.put("balance", balance);
                            reportEntry.put("interestExpence",
                                    interestExpensepermonth);
                            ammortizationArray.put(reportEntry);
                            startDate = getNextTerm(startDate, "monthly");

                        }
                        endDate = startDate;

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                    }

                }
                count += 1;

            }

        }
       
        int currentYear = contractStartDates.getYear();
        LocalDate nextJune30 = LocalDate.of(currentYear, 6, 30);

        monthBetweens += 2;

        double monthlyBlance = 0;

        for (int i = 0; i <= monthBetweens; i++) {
            if (i == 0) {
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", contractStartDates.toString());
                finalYear.put("deprecationExp", 0);
                reportArray.put(finalYear);

            } else if (i == 1) {
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(contractRegisteredDate,
                        "monthly").toString());
                finalYear.put("deprecationExp", depreciationFirst);
                reportArray.put(finalYear);
                startDate = getNextTerm(contractRegisteredDate, "monthly");
                monthlyBlance += depreciationFirst;
            } else if (i == monthBetweens) {

                double depreciationLast = rightOfUse - monthlyBlance;

                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "monthly").toString());
                finalYear.put("deprecationExp", depreciationLast);
                reportArray.put(finalYear);
                break;
            } else {
                if ((monthlyBlance + constDepreciationPerM > rightOfUse)) {
                    continue;
                } else {
                    JSONObject reportEntry = new JSONObject();
                    reportEntry.put("year", getNextTerm(startDate, "monthly").toString());
                    reportEntry.put("deprecationExp",
                            constDepreciationPerM);
                    reportArray.put(reportEntry);
                    startDate = getNextTerm(startDate, "monthly");
                    monthlyBlance += constDepreciationPerM;
                }
            }

        }

        JSONArray combinedArray = new JSONArray();

        // Loop through the years in ammortizationArray
        for (int i = 0; i < ammortizationArray.length(); i++) {
            JSONObject ammortizationEntry = ammortizationArray.getJSONObject(i);
            // Convert to LocalDate
            String year = ammortizationEntry.get("year").toString();
            boolean matched = false;

            // Find corresponding entry in reportArray
            for (int j = 0; j < reportArray.length(); j++) {
                JSONObject reportEntry = reportArray.getJSONObject(j);
                LocalDate reportYear = LocalDate.parse(reportEntry.getString("year")); // Convert to LocalDate
                if (reportEntry.getString("year").equals(year)) {
                    // Combine the information
                    JSONObject combinedEntry = new JSONObject();
                    combinedEntry.put("year", year.toString()); // Convert back to string
                    combinedEntry.put("deprecationExp", reportEntry.getDouble("deprecationExp"));
                    combinedEntry.put("interestExpence", ammortizationEntry.get("interestExpence"));

                    combinedArray.put(combinedEntry);
                    matched = true;
                    break;
                }
            }

            // If no match was found, add the entry from ammortizationArray
            if (!matched) {
                JSONObject combinedEntry = new JSONObject();
                combinedEntry.put("year", year.toString()); // Convert back to string
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
                LocalDate combinedYear = LocalDate.parse(combinedEntry.getString("year")); // Convert to LocalDate
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

        // Now combinedArray contains entries from both arrays for all years

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("combinedArray", combinedArray);
        return reportObject.toString();

    }

    //////////////////////////////////////////////////////////////////////////////////
    public static int calculateContractMonths(LocalDate startDate, LocalDate endDate) {
        long monthsBetween = ChronoUnit.MONTHS.between(startDate, endDate);
        return (int) (monthsBetween + 1); // Adding 1 to include both start and end months
    }

    public static double calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static String calculateReportY(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment, double advancePayment,
            double discountRate, double leaseLiability, LocalDate contractRegisteredDate, String installmentDetails,
            long BranchId, String contractType, LocalDate contractStartDates, LocalDate contractEndDates,
            String branchName) {

        double constDepreciationPerY = 0;
        double constDepreciationPerM = 0;
        double advancePayments = advancePayment;
        double yearbetween = yearsBetween(contractRegisteredDate, contractEndDates);
        double yearbetweens = 0;
        double firstRemainingMonth = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));

        double totalnumberofmonths = monthBetween(startDate, endDate);
        if (contractRegisteredDate.isEqual(getNextTerm(contractRegisteredDate, "yearly").minusYears(1))) {
            yearbetween -= 1;
        }
        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);

            Map<LocalDate, BigDecimal> installmentMap = createInstallmentMap(jsonObject);

            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();
                if (installmentDate.isBefore(contractRegisteredDate) ||
                        getNextTerm(contractRegisteredDate, "yearly")
                                .isEqual(getNextTerm(installmentDate, "yearly"))) {
                    yearbetweens += 1;

                    // advancePayments += futureLeasePayments;

                }

            }
            if (totalPayment == advancePayments) {
                constDepreciationPerY = (totalPayment / monthBetween(startDate, endDate)) * 12;
                constDepreciationPerM = (totalPayment / monthBetween(startDate, endDate));
            } else {
                constDepreciationPerY = depreciationPerMonth * 12;
                constDepreciationPerM = constDepreciationPerY / 12;

            }
        } else

        {
            if (totalPayment == advancePayments) {
                constDepreciationPerY = (totalPayment / monthBetween(startDate, endDate)) * 12;
                constDepreciationPerM = (totalPayment / monthBetween(startDate, endDate));

            } else {

                constDepreciationPerY = depreciationPerMonth * 12;
                constDepreciationPerM = constDepreciationPerY / 12;

            }
        }
        if (totalnumberofmonths <= 12) {
            yearbetween += 2;
        } else if (getNextTerm(contractStartDates, "yearly").isEqual(contractRegisteredDate)) {
            yearbetweens = 2;
        } else if (getNextTerm(contractStartDates, "yearly").isBefore(getNextTerm(contractRegisteredDate, "yearly"))) {

            yearbetweens = 1;

        } else if (getNextTerm(contractStartDates, "yearly").isEqual(getNextTerm(contractRegisteredDate, "yearly"))) {
            yearbetweens = 2;
        }

        yearbetween += yearbetweens;
        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();
        JSONArray summaryArray = new JSONArray();
        JSONObject detail = new JSONObject();
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("branchName", branchName);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", constDepreciationPerY);
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detail.put("contractYear", yearsBetween(startDate, endDate));

        detailArray.put(detail);
        double depreciationF = 0;
        double depreciationBeforeLast = 0;
        double depreciationFirstPeriod = 0;
        double balance = 0;
        double numberofmonths = 0;

        if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            // Handle the case where lease starts at the end of a month
            depreciationFirstPeriod = constDepreciationPerY;
        }

        else {
            // Handle the case where lease starts at the beginning of a month
            LocalDate currentDate = startDate;
            if (currentDate.isBefore(contractRegisteredDate) ||
                    currentDate.isEqual(contractRegisteredDate)) {

                depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));
                depreciationFirstPeriod = depreciationF * constDepreciationPerM;

            }
        }
        yearbetween = yearbetween + 1;
        for (int i = 0; i < yearbetween; i++) {
            if (i == 0) {
                balance = rightOfUse;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", contractStartDates.toString());
                finalYear.put("balance", balance);
                finalYear.put("deprecationExp", 0);
                finalYear.put("months", "-");
                reportArray.put(finalYear);
            } else if (i == 1) {
                balance = rightOfUse - depreciationFirstPeriod;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(contractRegisteredDate, "yearly").toString());
                finalYear.put("balance", balance);
                finalYear.put("deprecationExp", depreciationFirstPeriod);
                finalYear.put("months", firstRemainingMonth);
                numberofmonths += firstRemainingMonth;
                reportArray.put(finalYear);
                summaryArray.put(finalYear);
                startDate = getNextTerm(contractRegisteredDate, "yearly");
            }

            else if (yearbetween == 1) {
                depreciationBeforeLast = balance;
                double lastmonth = totalnumberofmonths - numberofmonths;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "yearly").toString());
                finalYear.put("balance", 0);
                finalYear.put("deprecationExp", depreciationBeforeLast);
                finalYear.put("months", lastmonth);
                summaryArray.put(finalYear);
                reportArray.put(finalYear);

                break;
            } else if (i == (yearbetween - 1) || (startDate == getNextTerm(endDate, "yearly"))) {
                depreciationBeforeLast = balance;
                double lastmonth = totalnumberofmonths - numberofmonths;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "yearly").toString());
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
                    reportEntry.put("year", getNextTerm(startDate, "yearly").toString());
                    reportEntry.put("balance", balance);
                    
                    reportEntry.put("deprecationExp", constDepreciationPerY);
                    reportEntry.put("months", "12");
                    reportArray.put(reportEntry);
                    summaryArray.put(reportEntry);
                    startDate = getNextTerm(startDate, "yearly");
                    numberofmonths += 12;
                }

            }
        }
        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);

            // Create a map to store installment dates and amounts
            Map<LocalDate, BigDecimal> installmentMap = new TreeMap<>();

            // Iterate through the JSON object and populate the map
            int numberofinstallment = 0;
            for (String key : jsonObject.keySet()) {
                BigDecimal value = jsonObject.getBigDecimal(key);
                LocalDate date = LocalDate.parse(key); // Parse the date string into a LocalDate
                installmentMap.put(date, value);
                numberofinstallment += 1;

            }

            // Now you have a map where keys are LocalDate objects and values are
            // installment amounts
            // You can access them as needed
            double leasePayment = 0;
            double interestExpense = 0;
            int count = 1;
            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();
                int yearsBetween = yearsBetween(contractStartDates, getNextTerm(installmentDate, "yearly"));
                double initialLeaseLiability = leaseLiability;

                

                for (int i = 0; i <= yearsBetween; i++) {

                    if (i == 0 && count == 1) {
                        balance = leaseLiability;
                        JSONObject finalYear = new JSONObject();
                        finalYear.put("year", contractRegisteredDate);
                        finalYear.put("interestExpence", "-");
                        finalYear.put("leasePayment", leasePayment);
                        finalYear.put("balance", balance);
                        ammortizationArray.put(finalYear);
                        endDate = getNextTerm(contractRegisteredDate, "yearly");
                    }

                    // the calculation for the last installment date and amount
                    else if (i == yearsBetween && count == numberofinstallment) {
                        startDate = getNextTerm(installmentDate, "yearly").minusYears(1);
                        endDate = getNextTerm(startDate, "yearly");
                        leasePayment = futureLeasePayments;
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        interestExpense = futureLeasePayments - balance;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", endDate);
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("balance", "-");
                        reportEntry.put("interestExpence",
                                interestExpense);
                        ammortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);
                        break;
                    }

                    // this skips if by checking the date of contract registerd date and intallment
                    // date for first iteration
                    else if (i == 1 && count == 1 && (getNextTerm(contractRegisteredDate, "yearly")
                            .isEqual(getNextTerm(installmentDate, "yearly")))) {
                        continue;
                    }

                    // this calculate for the rest instalment details and doesnt include the last
                    // instalment date and amount
                    else if (i == yearsBetween && count != numberofinstallment) {
                        startDate = getNextTerm(installmentDate, "yearly").minusYears(1);
                        endDate = getNextTerm(startDate, "yearly");
                        double numberOfmonths = monthBetween(startDate, endDate);
                        leasePayment = futureLeasePayments;
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, numberOfmonths);
                        balance = balance + interestExpense - futureLeasePayments;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", endDate);
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence",
                                interestExpense);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        ammortizationArray.put(reportEntry);
                        endDate = getNextTerm(endDate, "yearly");
                        summaryArray.put(reportEntry);

                    }

                    // this calculate the instal
                    else if (i == 1 && count == 1) {
                        startDate = endDate;
                        interestExpense = calculateInterestExpense(initialLeaseLiability,
                                discountRate, leasePayment, depreciationF);
                        balance += interestExpense;
                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", startDate);
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence",
                                interestExpense);
                        ammortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                        endDate = getNextTerm(startDate, "yearly");

                    }

                    else if ((count == numberofinstallment)
                            && !endDate.equals(getNextTerm(installmentDate, "yearly"))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        double number_of_month = monthBetween(startDate, endDate);
                        leasePayment = 0;

                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, number_of_month);
                        balance += interestExpense;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", startDate);
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence",
                                interestExpense);
                        ammortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                        // endDate = getNextTerm(startDate, "yearly");

                    }

                    //
                    else if (i > 1 && count > 0 && count != numberofinstallment
                            && !endDate.equals(getNextTerm(installmentDate, "yearly"))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        double number_of_month = monthBetween(startDate, endDate);
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, number_of_month);
                        balance += interestExpense;
                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", startDate);
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence",
                                interestExpense);
                        ammortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                        // endDate = getNextTerm(startDate, "yearly");

                    }
                }
                count += 1;

            }

        }

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("summaryArray", summaryArray);
        return reportObject.toString();
    }

    private static int yearsBetween(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        int yearbetween = 0;
        if (monthBetween(startDate, endDate) <= 12) {
            yearbetween = period.getYears();

        } else {
            yearbetween = period.getYears() + 1;
        }
        return yearbetween;

    }

    static double monthBetween(LocalDate startDate, LocalDate endDate) {
        double averageDaysInMonth = 365.000000000000001 / 12; // Approximate average number of

        // return ChronoUnit.DAYS.between(startDate, endDate) + 1 / 365;
        return (ChronoUnit.DAYS.between(startDate, endDate)) / averageDaysInMonth;
    }

    static int isBefore(LocalDate startDate, LocalDate targetDate) {
        // Compare the current date with the target date
        if (startDate.isBefore(targetDate)) {
            return -1;
        } else if (startDate.isEqual(targetDate)) {
            return 0;
        } else {
            return 1;
        }
    }

    static LocalDate getNextTerm(LocalDate date, String type) {
        if (type.equals("yearly")) {
            int currentYear = date.getYear();
            LocalDate nextJune30 = LocalDate.of(currentYear, 6, 30);

            // If the current date is on or after June 30, calculate for the next year
            if (date.isAfter(nextJune30) || date.isEqual(nextJune30)) {
                nextJune30 = nextJune30.plusYears(1);
            }
            return nextJune30;
        } else if (type.equals("monthly")) {
            if (date.getDayOfMonth() == date.lengthOfMonth()) {
                LocalDate nextMonth = date.plusMonths(1);
                YearMonth yearMonth = YearMonth.from(nextMonth);
                return yearMonth.atEndOfMonth();
            } else {
                LocalDate nextMonth = date.plusMonths(0);
                YearMonth yearMonth = YearMonth.from(nextMonth);
                return yearMonth.atEndOfMonth();
            }
        } else if (type.equals("weekly")) {
            LocalDate nextMonth = date.plusMonths(1);
            YearMonth yearMonth = YearMonth.from(nextMonth);
            return yearMonth.atEndOfMonth();
        } else {
            // Handle unknown or unsupported types
            throw new IllegalArgumentException("Unsupported type: " + type);
            // or return a default value instead of throwing an exception
            // return someDefaultValue;
        }
    }
    /*
     * -------------------------------START OF RIGHT OF USE AND DEPRECIATION PER
     * MONTH CALCULATION---------------------------------------------
     */

    public double calculateLeaseLiability(Double totalContractPrice, double advancePayment, double discountRate,
            LocalDate contractStartDate, LocalDate contractEndDate, String installmentDetails,
            LocalDate contractRegisteredDate) {

        double futureLeasePayments = totalContractPrice - advancePayment;

        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);

            Map<LocalDate, BigDecimal> installmentMap = createInstallmentMap(jsonObject);

            // Now you have a map where keys are LocalDate objects and values are
            // installment amounts
            // You can access them as needed
            BigDecimal leaseLiability = BigDecimal.ZERO;
            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                
                double leaseTermTillPaid = (double) ChronoUnit.DAYS.between(contractStartDate, installmentDate)
                        / 365.0;

                BigDecimal totalLeaseLiability = installmentAmount.divide(
                        BigDecimal.valueOf(Math.pow(1 + discountRate, leaseTermTillPaid)),
                        MathContext.DECIMAL128);
                leaseLiability = leaseLiability.add(totalLeaseLiability);
            }

            return leaseLiability.doubleValue();
        }

        else

        {
            int leaseTermTillPaid = (int) ChronoUnit.DAYS.between(contractStartDate,
                    contractStartDate);

            double leaseLiability = futureLeasePayments / Math.pow((1 + discountRate),
                    (leaseTermTillPaid / 365));
            return leaseLiability;

        }

    }

    public double calculateRightOfUseAsset(double advancePayments, double leaseLiability,
            double leaseIncentives, double initialDirectCosts, String installmentDetails,
            LocalDate contractRegisteredDate) {

        double rightOfUseAsset = 0;
        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);

            // Create a map to store installment dates and amounts
            Map<LocalDate, BigDecimal> installmentMap = createInstallmentMap(jsonObject);

            // Now you have a map where keys are LocalDate objects and values are
            // installment amounts
            // You can access them as needed
            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                
                rightOfUseAsset = (advancePayments + leaseLiability + initialDirectCosts) -
                        leaseIncentives;

            }

        } else {
            rightOfUseAsset = advancePayments;
        }
        return rightOfUseAsset;

    }

    public double calculateDepreciationPerMonth(double rightOfUseAsset, double period) {

        double depreciationPerMonth = rightOfUseAsset / period;

        return depreciationPerMonth;
    }

    private static double calculateInterestExpense(double outstandingLeaseLiability, double discountRate,
            double leasePayment, double monthbetween) {
        return (outstandingLeaseLiability - leasePayment) * discountRate
                * monthbetween / 12;
    }
    private static double updateLeaseLiability(double outstandingLeaseLiability, double interestExpense,
            double leasePayment) {

        return outstandingLeaseLiability - leasePayment + interestExpense;
    }

}

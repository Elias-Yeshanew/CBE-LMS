package com.example.demo.lease.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;
import java.util.Locale;
import java.math.RoundingMode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CalculateReport {
    public static String calculateReportGM(Long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment, double advancePayment,
            double discountRate, double leaseLiability, int selectedYear, int selectedMonth,
            LocalDate contractRegisteredDate, long BranchId, String contractType) {
        double constDepreciationPerM = totalPayment / calculateContractMonths(startDate, endDate);
        /////////////// CONDITIONAL////////////////
        int amountOfDepreciation;
        if (contractRegisteredDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            amountOfDepreciation = 1;
        } else {
            amountOfDepreciation = 2;
        }
        amountOfDepreciation += calculateContractMonths(contractRegisteredDate, endDate);

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();
        JSONArray finalReportArray = new JSONArray();
        JSONObject detail = new JSONObject();
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", constDepreciationPerM);
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detailArray.put(detail);
        // reportArray.put(initialBalance);
        double depreciationF = 0;
        double depreciationFirst = 0;

        if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            depreciationFirst = constDepreciationPerM;

        } else {
            // depreciationF = (calculateDaysBetween(startDate, getNextTerm(startDate,
            // "monthly"))) / 30;
            depreciationF = ChronoUnit.MONTHS.between(startDate, contractRegisteredDate);

            depreciationFirst = formatToTwoDecimalPlaces(depreciationF *
                    constDepreciationPerM);
        }

        for (int i = 0; i < amountOfDepreciation; i++) {
            if (i == 0) {
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", startDate.toString());
                finalYear.put("deprecationExp", " - ");
                reportArray.put(finalYear);
            } else if (i == 1) {
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "monthly").toString());
                finalYear.put("deprecationExp", formatToTwoDecimalPlaces(depreciationFirst));
                reportArray.put(finalYear);
                startDate = getNextTerm(startDate, "monthly");
            } else if (i == (amountOfDepreciation - 1)) {
                double depreciationLast = constDepreciationPerM - depreciationFirst;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "monthly").toString());
                finalYear.put("deprecationExp", formatToTwoDecimalPlaces(depreciationLast));
                reportArray.put(finalYear);
                break;
            } else {
                JSONObject reportEntry = new JSONObject();
                reportEntry.put("year", getNextTerm(startDate, "monthly").toString());
                reportEntry.put("deprecationExp", formatToTwoDecimalPlaces(constDepreciationPerM));
                reportArray.put(reportEntry);
                startDate = getNextTerm(startDate, "monthly");
            }
        }

        for (int j = 1; j < reportArray.length(); j++) {
            JSONObject entry = reportArray.getJSONObject(j);
            LocalDate entryDate = LocalDate.parse(entry.getString("year"));
            if (entryDate.getYear() == selectedYear && entryDate.getMonthValue() == selectedMonth) {
                String monthName = entryDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                LocalDate lastDayOfMonth = entryDate.withDayOfMonth(entryDate.lengthOfMonth());
                String yearMonth = lastDayOfMonth.getDayOfMonth() + " " + monthName + ", " + entryDate.getYear();
                JSONObject newEntry = new JSONObject();
                newEntry.put("year", yearMonth);
                newEntry.put("deprecationExp", entry.get("deprecationExp"));
                finalReportArray.put(newEntry);
            }
        }

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", finalReportArray);
        reportObject.put("ammortization", ammortizationArray);
        return reportObject.toString();
    }

    public static void main(String[] args) {

    }

    public static String calculateReportM(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment, double advancePayment,
            double discountRate, double leaseLiability, LocalDate contractRegisteredDate, String installmentDetails,
            long BranchId, String contractType) {
        double constDepreciationPerM;
        if (totalPayment == advancePayment) {
            constDepreciationPerM = totalPayment / calculateContractMonths(startDate, endDate);
        } else {
            constDepreciationPerM = depreciationPerMonth;
        }

        /////////////// CONDITIONAL////////////////
        int amountOfDepreciation;
        if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            amountOfDepreciation = 1;
        } else {
            amountOfDepreciation = 2;
        }
        amountOfDepreciation += calculateContractMonths(contractRegisteredDate, endDate);

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();


        JSONObject detail = new JSONObject();
        detail.put("contractMonth", Math.round(monthBetween(startDate, endDate)));
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", formatToTwoDecimalPlaces(constDepreciationPerM));
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detailArray.put(detail);
        // reportArray.put(initialBalance);
        double depreciationF = 0;
        double depreciationFirst = 0;
        double depreciationFirst1 = 0;
        double balance = 0;

        if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            // Handle the case where lease starts at the end of a month
            depreciationFirst = constDepreciationPerM;
        } else {
            // Handle the case where lease starts at the beginning of a month
            depreciationF = (calculateDaysBetween(startDate, getNextTerm(startDate,
                    "monthly"))) / 30;
            depreciationFirst1 = formatToTwoDecimalPlaces(depreciationF *
                    constDepreciationPerM);
            depreciationFirst = depreciationFirst1;

            LocalDate currentDate = getNextTerm(startDate, "monthly");
            while (currentDate.isBefore(contractRegisteredDate) ||
                    currentDate.isEqual(contractRegisteredDate)) {

                double currentDepreciation = formatToTwoDecimalPlaces(constDepreciationPerM);
                depreciationFirst += currentDepreciation;
                currentDate = getNextTerm(currentDate, "monthly");
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
                Period period = Period.between(startDate, getNextTerm(installmentDate, "yearly"));
                double futureLeasePayments = installmentAmount.doubleValue();
                int yearsBetween = period.getYears() + 1;
                double initialLeaseLiability = leaseLiability;
                double interestExpensepermonth = 0;

                if (numberofinstallment == 1
                        && getNextTerm(contractRegisteredDate, "yearly")
                                .equals(getNextTerm(installmentDate, "yearly"))) {
                    break;
                } else {
                    for (int i = 0; i <= yearsBetween; i++) {

                        // double principalRepayment = futureLeasePayments - interestExpense;
                        // leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                        // principalRepayment, leasePayment);
                        if (i == 0 && count == 1) {
                            balance = leaseLiability;
                            JSONObject finalYear = new JSONObject();
                            finalYear.put("year", "-");
                            finalYear.put("interestExpence", "-");
                            finalYear.put("balance", formatToTwoDecimalPlaces(balance));
                            ammortizationArray.put(finalYear);
                            endDate = getNextTerm(contractRegisteredDate, "yearly");
                        } else if (i == yearsBetween && count == numberofinstallment) {
                            startDate = endDate;
                            endDate = getNextTerm(startDate, "yearly");
                            leasePayment = futureLeasePayments;
                            leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                    leasePayment);
                            interestExpense = futureLeasePayments - balance;
                            interestExpensepermonth = interestExpense
                                    / Math.round(monthBetween(startDate, endDate));

                            for (int month = 1; month <= 12; month++) {
                                JSONObject reportEntry = new JSONObject();
                                reportEntry.put("year", getNextTerm(startDate, "monthly"));
                                reportEntry.put("balance", "-");
                                reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpensepermonth));
                                ammortizationArray.put(reportEntry);
                                startDate = getNextTerm(startDate, "monthly");

                            }
                            break;
                        } else if (i == yearsBetween && count != numberofinstallment) {
                            startDate = endDate;
                            endDate = getNextTerm(startDate, "yearly");
                            leasePayment = futureLeasePayments;
                            leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                    leasePayment);
                            interestExpense = calculateInterestExpense(initialLeaseLiability,
                                    discountRate, leasePayment, startDate, endDate);
                            balance = balance + interestExpense - futureLeasePayments;
                            interestExpensepermonth = interestExpense
                                    / Math.round(monthBetween(startDate, endDate));
                            for (int month = 1; month <= 12; month++) {
                                JSONObject reportEntry = new JSONObject();
                                reportEntry.put("year", getNextTerm(startDate, "monthly"));
                                reportEntry.put("balance", balance);
                                reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpense));
                                ammortizationArray.put(reportEntry);
                                startDate = getNextTerm(startDate, "monthly");

                            }
                        } else if (i == 1 && count == 1) {
                            if (getNextTerm(contractRegisteredDate, "yearly")
                                    .equals(getNextTerm(installmentDate, "yearly"))) {

                                LocalDate contractStartDate = startDate;
                                endDate = getNextTerm(contractRegisteredDate, "yearly");
                                interestExpense = calculateInterestExpense(leaseLiability,
                                        discountRate, leasePayment, contractStartDate, endDate);
                                balance += interestExpense;
                                interestExpensepermonth = interestExpense
                                        / monthBetween(startDate, endDate);
                                Double number_of_month = (double) Math.round(monthBetween(startDate, endDate));
                                for (int month = 1; month <= number_of_month; month++) {
                                    JSONObject reportEntry = new JSONObject();
                                    reportEntry.put("year", getNextTerm(startDate, "monthly"));
                                    reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                                    reportEntry.put("interestExpence",
                                            formatToTwoDecimalPlaces(interestExpensepermonth));
                                    ammortizationArray.put(reportEntry);
                                    startDate = getNextTerm(startDate, "monthly");
                                }

                                leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                        leasePayment);

                            } else {
                                endDate = getNextTerm(contractRegisteredDate, "yearly");
                                Double number_of_month = (double) Math.round(monthBetween(startDate, endDate));

                                interestExpense = calculateInterestExpense(leaseLiability,
                                        discountRate, leasePayment, startDate, endDate);
                                balance += interestExpense;
                                interestExpensepermonth = interestExpense / monthBetween(startDate, endDate);

                                System.out.println("monthBetween(startDate, endDate): " + number_of_month);

                                for (int month = 1; month <= number_of_month; month++) {

                                    JSONObject reportEntry = new JSONObject();
                                    reportEntry.put("year", getNextTerm(startDate, "monthly"));
                                    reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                                    reportEntry.put("interestExpence",
                                            formatToTwoDecimalPlaces(interestExpensepermonth));
                                    ammortizationArray.put(reportEntry);
                                    startDate = getNextTerm(startDate, "monthly");

                                }
                                leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                        leasePayment);
                            }

                        } else if (count < numberofinstallment) {
                            startDate = endDate;
                            endDate = getNextTerm(startDate, "yearly");
                            interestExpense = calculateInterestExpense(leaseLiability,
                                    discountRate, leasePayment, startDate, endDate);
                            balance += interestExpense;
                            interestExpensepermonth = interestExpense / monthBetween(startDate, endDate);

                            int number_of_month = (int) monthBetween(startDate, endDate);
                            for (int month = 1; month <= number_of_month; month++) {

                                JSONObject reportEntry = new JSONObject();
                                reportEntry.put("year", getNextTerm(startDate, "monthly"));
                                reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                                reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpensepermonth));
                                ammortizationArray.put(reportEntry);

                                startDate = getNextTerm(startDate, "monthly");

                            }
                            leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                    leasePayment);
                        } else if (numberofinstallment == 1) {
                            startDate = endDate;
                            endDate = getNextTerm(startDate, "yearly");

                            interestExpense = calculateInterestExpense(leaseLiability,
                                    discountRate, leasePayment, startDate, endDate);
                            interestExpensepermonth = interestExpense / monthBetween(startDate, endDate);

                            balance += interestExpense;
                            int number_of_month = (int) monthBetween(startDate, endDate);
                            for (int month = 1; month <= number_of_month; month++) {
                                JSONObject reportEntry = new JSONObject();
                                reportEntry.put("year", getNextTerm(startDate, "monthly"));
                                reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                                reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpensepermonth));
                                ammortizationArray.put(reportEntry);

                                startDate = getNextTerm(startDate, "monthly");

                            }
                            leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                    leasePayment);
                        }

                    }
                    count += 1;
                }

            }

        }


        for (int i = 0; i < amountOfDepreciation; i++) {
            if (i == 0) {
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", startDate.toString());
                finalYear.put("deprecationExp", 0);
                reportArray.put(finalYear);
            } else if (i == 1) {
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(contractRegisteredDate, "monthly").toString());
                finalYear.put("deprecationExp", formatToTwoDecimalPlaces(depreciationFirst));
                reportArray.put(finalYear);
                startDate = getNextTerm(contractRegisteredDate, "monthly");
            } else if (i == (amountOfDepreciation - 1) || (startDate == getNextTerm(endDate, "yearly"))) {
                double depreciationLast = constDepreciationPerM - depreciationFirst1;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "monthly").toString());
                finalYear.put("deprecationExp", formatToTwoDecimalPlaces(depreciationLast));
                reportArray.put(finalYear);
                break;
            } else {
                JSONObject reportEntry = new JSONObject();
                reportEntry.put("year", getNextTerm(startDate, "monthly").toString());
                reportEntry.put("deprecationExp", formatToTwoDecimalPlaces(constDepreciationPerM));
                reportArray.put(reportEntry);
                startDate = getNextTerm(startDate, "monthly");
            }
        }
        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
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

    ///////////////////////////////////////////////////////////////////////////////////////
    public static String calculateReportY(long Id, LocalDate startDate, LocalDate endDate, double rightOfUse,
            double depreciationPerMonth, String type, double totalPayment, double advancePayment,
            double discountRate, double leaseLiability, LocalDate contractRegisteredDate, String installmentDetails,
            long BranchId, String contractType, LocalDate contractStartDates) {

        double firstRemainingMonth = 0;
        double firstsMonth = 0;
        double constDepreciationPerY = 0;
        double constDepreciationPerM = 0;
        if (totalPayment == advancePayment) {
            firstRemainingMonth = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));
            firstsMonth = monthBetween(startDate, getNextTerm(startDate, "yearly"));
            constDepreciationPerY = (totalPayment / calculateContractMonths(startDate, endDate)) * 12;
            constDepreciationPerM = (totalPayment / calculateContractMonths(startDate, endDate));

        } else {
            firstRemainingMonth = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));
            firstsMonth = monthBetween(startDate, getNextTerm(startDate, "yearly"));

            constDepreciationPerY = depreciationPerMonth * 12;
            constDepreciationPerM = constDepreciationPerY / 12;

        }
        // check

        /////////////// CONDITIONAL////////////////
        LocalDate targetDate = LocalDate.of(contractRegisteredDate.getYear(), 6, 30);
        int amountOfDepreciation = 0;
        if (isBefore(contractRegisteredDate, targetDate) == -1) {
            amountOfDepreciation = 2;
        } else if (isBefore(contractRegisteredDate, targetDate) == 1
                & (contractRegisteredDate.getYear() == startDate.getYear())) {
            amountOfDepreciation = 1;
        } else if (isBefore(contractRegisteredDate, targetDate) == 1) {
            amountOfDepreciation = 2;
        } else if (isBefore(contractRegisteredDate, targetDate) == 0) {
            amountOfDepreciation = 1;
        }

        amountOfDepreciation += yearsBetween(contractRegisteredDate, endDate) + 1;
        System.out.println(
                "yearsBetween(contractRegisteredDate, endDate) " + yearsBetween(contractRegisteredDate, endDate));
        System.out.println(
                "---------------------------------------------------year amount of depreciation--------- "
                        + amountOfDepreciation);
        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();

        JSONObject detail = new JSONObject();
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", formatToTwoDecimalPlaces(depreciationPerMonth));
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detail.put("contractYear", yearsBetween(startDate, endDate));

        detailArray.put(detail);
        // reportArray.put(initialBalance);
        double depreciationF = 0;
        // double depreciationFirst = 0;
        double depreciationBeforeLast = 0;
        double depreciationFirstPeriod = 0;
        double balance = 0;

        if (startDate.getDayOfMonth() == startDate.lengthOfMonth()) {
            // Handle the case where lease starts at the end of a month
            depreciationFirstPeriod = constDepreciationPerY;
        }

        else {
            // Handle the case where lease starts at the beginning of a month
            LocalDate currentDate = startDate;
            if (currentDate.isBefore(contractRegisteredDate) ||
                    currentDate.isEqual(contractRegisteredDate)) {

                depreciationF = formatToTwoDecimalPlaces((monthBetween(startDate,
                        getNextTerm(contractRegisteredDate, "yearly"))));

                depreciationFirstPeriod = depreciationF * constDepreciationPerM;

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
                Period period = Period.between(startDate, getNextTerm(installmentDate, "yearly"));
                double futureLeasePayments = installmentAmount.doubleValue();
                int yearsBetween = period.getYears() + 1;
                double initialLeaseLiability = leaseLiability;
                System.out.println("getNextTerm(contractRegisteredDate, 'yearly' "
                        + getNextTerm(contractRegisteredDate, "yearly"));
                System.out.println("getNextTerm(contractRegisteredDate, 'yearly' "
                        + getNextTerm(installmentDate, "yearly"));
                System.out.println("numberofinstallment " + numberofinstallment);
                if ((numberofinstallment == 1)
                        && getNextTerm(contractRegisteredDate, "yearly")
                                .equals(getNextTerm(installmentDate, "yearly"))) {
                    break;
                }

                for (int i = 0; i <= yearsBetween; i++) {

                    // double principalRepayment = futureLeasePayments - interestExpense;
                    // leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                    // principalRepayment, leasePayment);
                    System.out.println("count: " + count);
                    if (i == 0 && count == 1) {
                        balance = leaseLiability;
                        JSONObject finalYear = new JSONObject();
                        finalYear.put("year", "-");
                        finalYear.put("interestExpence", "-");
                        finalYear.put("leasePayment", leasePayment);
                        finalYear.put("balance", formatToTwoDecimalPlaces(balance));
                        ammortizationArray.put(finalYear);
                        endDate = getNextTerm(contractRegisteredDate, "yearly");
                    } else if (i == yearsBetween && count == numberofinstallment) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        leasePayment = futureLeasePayments;
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        interestExpense = futureLeasePayments - balance;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", getNextTerm(startDate, "yearly"));
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("balance", "-");
                        reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpense));
                        ammortizationArray.put(reportEntry);
                        break;
                    }

                    else if (i == yearsBetween && count != numberofinstallment) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        leasePayment = futureLeasePayments;
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                        interestExpense = calculateInterestExpense(initialLeaseLiability,
                                discountRate, leasePayment, startDate, endDate);
                        balance = balance + interestExpense - futureLeasePayments;

                        System.out.println("leaseLiability: " + leaseLiability);
                        System.out.println("futureLeasePayments: " + futureLeasePayments);


                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", getNextTerm(startDate, "yearly"));
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpense));
                        ammortizationArray.put(reportEntry);
                    } else if (i == 1 && count == 1) {
                        LocalDate contractStartDate = startDate;
                        interestExpense = calculateInterestExpense(initialLeaseLiability,
                                discountRate, leasePayment, contractStartDate, endDate);
                        balance += interestExpense;
                        System.out.println("leaseLiability: " + leaseLiability);

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", getNextTerm(startDate, "yearly"));
                        reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                        reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpense));
                        ammortizationArray.put(reportEntry);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                    } else if (count < numberofinstallment) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        System.out.println("startDate: " + startDate);
                        System.out.println("endDate: " + endDate);
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, startDate, endDate);
                        System.out.println("leaseLiability: " + leaseLiability);
                        balance += interestExpense;
                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", getNextTerm(startDate, "yearly"));
                        reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpense));
                        ammortizationArray.put(reportEntry);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);

                    } else if (numberofinstallment == 1 && (endDate.getYear() <= installmentDate.getYear())) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        System.out.println("startDate: " + startDate);
                        System.out.println("endDate: " + endDate);
                        interestExpense = calculateInterestExpense(leaseLiability,
                                discountRate, leasePayment, startDate, endDate);
                        System.out.println("leaseLiability: " + leaseLiability);
                        balance += interestExpense;
                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", getNextTerm(startDate, "yearly"));
                        reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("interestExpence", formatToTwoDecimalPlaces(interestExpense));
                        ammortizationArray.put(reportEntry);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
                                leasePayment);
                    }

                    System.out.println("interestExpense " + interestExpense);

                    System.out.println("count: " + count);

                }
                count += 1;

            }

        }


        for (int i = 0; i < amountOfDepreciation; i++) {
            if (i == 0) {
                balance = rightOfUse;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", contractStartDates.toString());
                finalYear.put("balance", formatToTwoDecimalPlaces(balance));
                finalYear.put("deprecationExp", 0);
                finalYear.put("months", "-");
                reportArray.put(finalYear);
            } else if (i == 1) {
                balance = rightOfUse - depreciationFirstPeriod;
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(contractRegisteredDate, "yearly").toString());
                finalYear.put("balance", formatToTwoDecimalPlaces(balance));
                finalYear.put("deprecationExp", formatToTwoDecimalPlaces(depreciationFirstPeriod));
                finalYear.put("months", firstRemainingMonth);
                reportArray.put(finalYear);
                startDate = getNextTerm(contractRegisteredDate, "yearly");
            } else if (i == (amountOfDepreciation - 1) || (startDate == getNextTerm(endDate, "yearly"))) {
                // balance -= constDepreciationPerY;
                depreciationBeforeLast = formatToTwoDecimalPlaces(balance);

                // double depreciationLast = depreciationPerMonth * (12 - firstRemainingMonth);
                JSONObject finalYear = new JSONObject();
                finalYear.put("year", getNextTerm(startDate, "yearly").toString());
                finalYear.put("balance", 0);
                // finalYear.put("deprecationExp", formatToTwoDecimalPlaces(depreciationLast));
                finalYear.put("deprecationExp", depreciationBeforeLast);
                finalYear.put("months", 12 - firstsMonth);

                reportArray.put(finalYear);
                break;
            } else {
                balance -= constDepreciationPerY;
                JSONObject reportEntry = new JSONObject();
                reportEntry.put("year", getNextTerm(startDate, "yearly").toString());
                reportEntry.put("balance", formatToTwoDecimalPlaces(balance));
                // if (i == (amountOfDepreciation - 2)) {
                // depreciationBeforeLast = formatToTwoDecimalPlaces(balance);
                // }
                reportEntry.put("deprecationExp", formatToTwoDecimalPlaces(constDepreciationPerY));
                reportEntry.put("months", "12");
                reportArray.put(reportEntry);
                startDate = getNextTerm(startDate, "yearly");
            }
        }
        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        return reportObject.toString();
    }

    private static Double yearsBetween(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        int yearbetween = period.getYears() + 1;
        return (double) yearbetween;

    }

    static double monthBetween(LocalDate startDate, LocalDate endDate) {
        double averageDaysInMonth = 365.25 / 12; // Approximate average number of

        return ChronoUnit.DAYS.between(startDate, endDate) / averageDaysInMonth;
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

    public static double formatToTwoDecimalPlaces(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        String formattedValue = decimalFormat.format(value);
        return Double.parseDouble(formattedValue);
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
            BigDecimal leaseLiability = BigDecimal.ZERO;
            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();

                if (numberofinstallment == 1
                        && (getNextTerm(contractRegisteredDate, "yearly") == getNextTerm(installmentDate, "yearly"))) {
                    leaseLiability = BigDecimal.ZERO;
                    break;
                } else {
                    double leaseTermTillPaid = (double) ChronoUnit.DAYS.between(contractStartDate, installmentDate)
                            / 365.0;

                BigDecimal totalLeaseLiability = installmentAmount.divide(
                        BigDecimal.valueOf(Math.pow(1 + discountRate, leaseTermTillPaid)),
                        MathContext.DECIMAL128);

                // double totalLeaseLiability = installmentAmount
                // / Math.pow(dis, leaseTermTillPaid);

                leaseLiability = leaseLiability.add(totalLeaseLiability);
                leaseLiability = leaseLiability.setScale(2, RoundingMode.HALF_UP);

            }

        }

            return leaseLiability.doubleValue();
        }

        else

        {
            int leaseTermTillPaid = (int) ChronoUnit.DAYS.between(contractStartDate,
                    contractStartDate);

            double leaseLiability = futureLeasePayments / Math.pow((1 + discountRate),
                    (leaseTermTillPaid / 365));
            return Math.round(leaseLiability * 100.0) / 100.0;

        }

    }

    public double calculateRightOfUseAsset(double advancePayments, double leaseLiability,
            double leaseIncentives, double initialDirectCosts) {
        double rightOfUseAsset = (advancePayments + leaseLiability + initialDirectCosts) - leaseIncentives;
        return Math.round(rightOfUseAsset * 100.0) / 100.0;
    }

    public double calculateDepreciationPerMonth(double rightOfUseAsset, double period) {
        period = Math.round(period);
        double depreciationPerMonth = rightOfUseAsset / period;

        return Math.round(depreciationPerMonth * 100.0) / 100.0;
    }

    public static String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(amount);
    }

    public static BigDecimal calculatePower(BigDecimal base, int exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Exponent must be non-negative");
        }

        return base.pow(exponent, MathContext.DECIMAL128);
    }

    private static double calculateInterestExpense(double outstandingLeaseLiability, double discountRate,
            double leasePayment, LocalDate sartDate, LocalDate endDate) {
        double interestExpences = (outstandingLeaseLiability - leasePayment) * discountRate
                * monthBetween(sartDate, endDate) / 12;

        // System.out.println("monthbetweenn" + monthBetween(sartDate, endDate));

        return interestExpences;
    }

    private static double updateLeaseLiability(double outstandingLeaseLiability, double interestExpense,
            double leasePayment) {

        Double leaseLiability = outstandingLeaseLiability - leasePayment + interestExpense;

        return leaseLiability;
    }

}

package com.cbe.lms.lease.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONObject;

public class ReportUtility {
    
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

    public static double calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    public static int yearsBetween(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        int yearbetween;
        if (monthBetween(startDate, endDate) <= 12) {
            yearbetween = period.getYears();

        } else {
            yearbetween = period.getYears() + 1;
        }
        return yearbetween;

    }

    public static double monthBetween(LocalDate startDate, LocalDate endDate) {
        double averageDaysInMonth = 365.00000000000000000000001 / 12; // Approximate average number of

        // return ChronoUnit.DAYS.between(startDate, endDate) + 1 / 365;
        return (ChronoUnit.DAYS.between(startDate, endDate)) / averageDaysInMonth;
    }

    public static double calculateLeaseLiability(Double totalContractPrice, double advancePayment, double discountRate,
            LocalDate contractStartDate, LocalDate contractEndDate, String installmentDetails,
            LocalDate contractRegisteredDate) {

        double futureLeasePayments = totalContractPrice - advancePayment;

        if (installmentDetails != null) {


            Entry<Map<LocalDate, BigDecimal>, Integer> result = parseInstallmentDetails(installmentDetails);
            Map<LocalDate, BigDecimal> installmentMap = result.getKey();

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

            Entry<Map<LocalDate, BigDecimal>, Integer> result = parseInstallmentDetails(installmentDetails);
            Map<LocalDate, BigDecimal> installmentMap = result.getKey();

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

    public static double calculateInterestExpense(double outstandingLeaseLiability, double discountRate,
            double leasePayment, double monthbetween) {
        return (outstandingLeaseLiability - leasePayment) * discountRate
                * monthbetween / 12;
    }

    public static double updateLeaseLiability(double outstandingLeaseLiability, double interestExpense,
            double leasePayment) {

        return outstandingLeaseLiability - leasePayment + interestExpense;
    }
    
    public static Entry<Map<LocalDate, BigDecimal>, Integer> parseInstallmentDetails(String installmentDetails) {

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

}

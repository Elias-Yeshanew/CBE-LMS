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
        int monthBetweens = (int) monthBetween(contractRegisteredDate, contractEndDates);
        double advancePayments = advancePayment;

        if (installmentDetails != null) {

            Entry<Map<LocalDate, BigDecimal>, Integer> result = parseInstallmentDetails(installmentDetails);
            Map<LocalDate, BigDecimal> installmentMap = result.getKey();

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
        detail.put("contractRegisteredDate", contractRegisteredDate);
        detail.put("startDate", startDate);
        detail.put("endDate", endDate);
        detailArray.put(detail);

        double depreciationF;
        //  monthBetween(startDate, getNextTerm(contractRegisteredDate, "monthly"));
        // depreciationF = Math.round(depreciationF * 100.0) / 100.0;
        double depreciationFirst;
        // double depreciationFirst1 = 0;
        double balance = 0;

        if (getNextTerm(startDate, "monthly").equals(getNextTerm(contractRegisteredDate, "monthly"))) {
            depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"))
                    - (int) monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));

        } else {
            depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, "monthly"));

        }
        
        depreciationFirst = depreciationF * constDepreciationPerM;

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
            String contractTerm = "monthly";
            
            processInstallments(installmentMap,  leaseLiability,
                discountRate, ammortizationArray,  reportArray,  startDate,
                endDate, numberofinstallment,  contractRegisteredDate,  contractTerm);

            // for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
            //     LocalDate installmentDate = entry.getKey();
            //     BigDecimal installmentAmount = entry.getValue();
            //     double futureLeasePayments = installmentAmount.doubleValue();

            //     // Calculate years between contract start date and the current installment date
            //     int yearsBetween = yearsBetween(contractStartDates, getNextTerm(installmentDate, "yearly"));
            //     double initialLeaseLiability = leaseLiability;
            //     double interestExpensepermonth;
            //     double firstMonthInterestExpence;
            //     double numberOfmonths;
            //     // Check if the current installment is the last one
            //     boolean isFinalInstallment = count == numberofinstallment;

            //     for (int i = 0; i <= yearsBetween; i++) {

            //         // Handle first-year entry
            //         if (i == 0 && count == 1) {
            //             balance = leaseLiability;
            //             JSONObject finalYear = new JSONObject();
            //             finalYear.put("year", contractRegisteredDate);
            //             finalYear.put("interestExpence", "-");
            //             finalYear.put("leasePayment", leasePayment);
            //             finalYear.put("balance", balance);
            //             ammortizationArray.put(finalYear);
            //         }
            //         // Handle final year entry for the last installment
            //         else if (i == yearsBetween && isFinalInstallment) {
            //             // After the last installment, stop calculating further interest
            //             startDate = getNextTerm(installmentDate, "yearly").minusYears(1);
            //             endDate = getNextTerm(startDate, "yearly");
            //             leasePayment = futureLeasePayments;
            //             leaseLiability = updateLeaseLiability(leaseLiability, interestExpense,
            //                     leasePayment);
            //             interestExpense = futureLeasePayments - balance;
            //             interestExpensepermonth = interestExpense / 12;
            //             for (int month = 1; month <= 12; month++) {
            //                 startDate = getNextTerm(startDate, "monthly");

            //                 JSONObject reportEntry = new JSONObject();
            //                 reportEntry.put("year", startDate);
            //                 reportEntry.put("leasePayment", leasePayment);
            //                 reportEntry.put("balance", "-");
            //                 reportEntry.put("interestExpence",
            //                         interestExpensepermonth);
            //                 ammortizationArray.put(reportEntry);
            //             }
            //             break; // Exit loop after final installment

            //         } else if (i == yearsBetween && !isFinalInstallment) {
            //             // Other years' logic
            //             LocalDate constarDate = getNextTerm(installmentDate, "yearly").minusYears(1);
            //             startDate = constarDate;
            //             endDate = getNextTerm(startDate, "yearly");
            //             numberOfmonths = monthBetween(startDate, endDate);
            //             leasePayment = futureLeasePayments;
            //             interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment,
            //                     numberMONTHS);
            //             balance = balance + interestExpense - leasePayment;
            //             interestExpensepermonth = interestExpense / 12;

            //             // Process the monthly entries for the current year
            //             for (int month = 0; month < 12; month++) {
            //                 startDate = getNextTerm(startDate, "monthly");
            //                 JSONObject reportEntry = new JSONObject();
            //                 reportEntry.put("year", startDate);
            //                 reportEntry.put("leasePayment", leasePayment);
            //                 reportEntry.put("balance", balance);
            //                 reportEntry.put("interestExpence", interestExpensepermonth);
            //                 ammortizationArray.put(reportEntry);
            //             }
            //             leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //             endDate = getNextTerm(startDate, "monthly");
            //         }

            //         // Additional condition for skipped calculations
            //         else if (i == 1 && count == 1 && getNextTerm(contractRegisteredDate, "yearly")
            //                 .isEqual(getNextTerm(installmentDate, "yearly"))) {
            //             continue;
            //         }

            //         // Handle entries for installments before the final installment
            //         else if (i == yearsBetween && count != numberofinstallment) {
            //             LocalDate constarDate = getNextTerm(installmentDate, "yearly").minusYears(1);
            //             startDate = constarDate;
            //             endDate = getNextTerm(startDate, "yearly");
            //             numberOfmonths = monthBetween(constarDate, endDate);
            //             leasePayment = futureLeasePayments;
            //             interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment,
            //                     numberOfmonths);
            //             balance = balance + interestExpense - leasePayment;
            //             interestExpensepermonth = interestExpense / 12;

            //             for (int month = 0; month < 12; month++) {
            //                 startDate = getNextTerm(startDate, "monthly");
            //                 JSONObject reportEntry = new JSONObject();
            //                 reportEntry.put("year", startDate);
            //                 reportEntry.put("leasePayment", leasePayment);
            //                 reportEntry.put("balance", balance);
            //                 reportEntry.put("interestExpence", interestExpensepermonth);
            //                 ammortizationArray.put(reportEntry);
            //             }

            //             leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //             endDate = getNextTerm(startDate, "monthly");
            //         }

            //         // Handle first month after registration
            //         else if (i == 1 && count == 1) {
            //             startDate = getNextTerm(contractRegisteredDate, "monthly");
            //             numberOfmonths = monthBetween(contractStartDates,
            //                     getNextTerm(contractRegisteredDate, "yearly"));
            //             double expenceMonths = monthBetween(contractRegisteredDate,
            //                     getNextTerm(contractRegisteredDate, "yearly"));
            //             interestExpense = calculateInterestExpense(initialLeaseLiability, discountRate, leasePayment,
            //                     numberOfmonths);

            //             balance += interestExpense;
            //             if (numberOfmonths < 1) {
            //                 interestExpensepermonth = interestExpense;
            //             } else {
            //                 interestExpensepermonth = interestExpense / numberOfmonths;
            //             }

            //             for (int month = 0; month < expenceMonths; month++) {
            //                 JSONObject reportEntry = new JSONObject();
            //                 if (month == 0) {
            //                     firstMonthInterestExpence = interestExpensepermonth * ((int) depreciationF + 1);
            //                     reportEntry.put("year", startDate);
            //                     reportEntry.put("balance", balance);
            //                     reportEntry.put("interestExpence", firstMonthInterestExpence);
            //                     ammortizationArray.put(reportEntry);
            //                     startDate = getNextTerm(startDate, "monthly");
            //                 } else {
            //                     reportEntry.put("year", startDate);
            //                     reportEntry.put("balance", balance);
            //                     reportEntry.put("interestExpence", interestExpensepermonth);
            //                     ammortizationArray.put(reportEntry);
            //                     startDate = getNextTerm(startDate, "monthly");
            //                 }
            //             }

            //             leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //             endDate = startDate;
            //         }

            //         // Handle final installments with no further interest
            //         else if (leasePayment == 0 && count == 1 && !getNextTerm(endDate, "yearly")
            //                 .isEqual(getNextTerm(installmentDate, "yearly"))) {
            //             startDate = endDate;
            //             endDate = getNextTerm(startDate, "yearly");
            //             leasePayment = 0;
            //             interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment,
            //                     monthBetween(startDate, endDate));
            //             balance += interestExpense;
            //             interestExpensepermonth = interestExpense / 12;

            //             for (int month = 1; month <= 12; month++) {
            //                 JSONObject reportEntry = new JSONObject();
            //                 reportEntry.put("year", startDate);
            //                 reportEntry.put("balance", balance);
            //                 reportEntry.put("interestExpence", interestExpensepermonth);
            //                 ammortizationArray.put(reportEntry);
            //                 startDate = getNextTerm(startDate, "monthly");
            //             }

            //             leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //         }
            //     }

            //     count += 1;

            //     // Add a condition to break the outer loop if the final installment is processed
            //     if (isFinalInstallment) {
            //         break; // Exit the outer loop after processing the final installment
            //     }
            // }

        }

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
        // for (int i = 0; i < ammortizationArray.length(); i++) {
        //     JSONObject ammortizationEntry = ammortizationArray.getJSONObject(i);
        //     // Convert to LocalDate
        //     String year = ammortizationEntry.get("year").toString();
        //     boolean matched = false;

        //     // Find corresponding entry in reportArray
        //     for (int j = 0; j < reportArray.length(); j++) {
        //         JSONObject reportEntry = reportArray.getJSONObject(j);
        //         // LocalDate reportYear = LocalDate.parse(reportEntry.getString("year")); // Convert to LocalDate
        //         if (reportEntry.getString("year").equals(year)) {
        //             // Combine the information
        //             JSONObject combinedEntry = new JSONObject();
        //             combinedEntry.put("year", year); // Convert back to string
        //             combinedEntry.put("deprecationExp", reportEntry.getDouble("deprecationExp"));
        //             combinedEntry.put("interestExpence", ammortizationEntry.get("interestExpence"));

        //             combinedArray.put(combinedEntry);
        //             matched = true;
        //             break;
        //         }
        //     }

        //     // If no match was found, add the entry from ammortizationArray
        //     if (!matched) {
        //         JSONObject combinedEntry = new JSONObject();
        //         combinedEntry.put("year", year); // Convert back to string
        //         combinedEntry.put("deprecationExp", 0); // Set default value for deprecationExp
        //         combinedEntry.put("interestExpence", ammortizationEntry.get("interestExpence"));

        //         // Add additional fields if needed
        //         // combinedEntry.put("additionalField", "your_additional_value_here");

        //         combinedArray.put(combinedEntry);
        //     }
        // }

        // Loop through the years in reportArray
        // for (int i = 0; i < reportArray.length(); i++) {
        //     JSONObject reportEntry = reportArray.getJSONObject(i);
        //     LocalDate year = LocalDate.parse(reportEntry.getString("year")); // Convert to LocalDate

        //     boolean matched = false;

        //     // Check if the year already exists in combinedArray
        //     for (int j = 0; j < combinedArray.length(); j++) {
        //         JSONObject combinedEntry = combinedArray.getJSONObject(j);
        //         LocalDate combinedYear = LocalDate.parse(combinedEntry.getString("year")); // Convert to LocalDate
        //         if (combinedYear.equals(year)) {
        //             matched = true;
        //             break;
        //         }
        //     }

        //     // If no match was found, add the entry from reportArray
        //     if (!matched) {
        //         JSONObject combinedEntry = new JSONObject();
        //         combinedEntry.put("year", year.toString()); // Convert back to string
        //         combinedEntry.put("deprecationExp", reportEntry.getDouble("deprecationExp"));
        //         combinedEntry.put("interestExpence", 0); // Set default value for interestExpence

        //         // Add additional fields if needed
        //         // combinedEntry.put("additionalField", "your_additional_value_here");

        //         combinedArray.put(combinedEntry);
        //     }
        // }

        // Now combinedArray contains entries from both arrays for all years

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("combinedArray", combinedArray);
        return reportObject.toString();

    }
   
    private static void calculateReport(Map<LocalDate, BigDecimal> installmentMap, double leaseLiability,
        double discountRate, JSONArray amortizationArray, JSONArray summaryArray, LocalDate startDate,
        LocalDate endDate, int numberOfInstallment, LocalDate contractRegisteredDate, String reportType) {

            double balance = leaseLiability;
            double leasePayment = 0;
            double interestExpense = 0;
            int count = 1;
            
            // Determine the period (yearly or monthly) based on reportType
            String period = reportType.equalsIgnoreCase("yearly") ? "yearly" : "monthly";
            double depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, period));

            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();
                
                int timeBetween = reportType.equalsIgnoreCase("yearly") ? 
                                yearsBetween(startDate, getNextTerm(installmentDate, period)) : 
                                (int) monthBetween(startDate, getNextTerm(installmentDate, period));

                double initialLeaseLiability = leaseLiability;
                boolean isFinalInstallment = count + 1 == numberOfInstallment + 1;

                for (int i = 0; i <= timeBetween; i++) {

                    if (i == 0 && count == 1) {
                        balance = leaseLiability;
                        JSONObject firstEntry = createReportEntry(contractRegisteredDate.toString(), leasePayment, balance, 0.0);
                        amortizationArray.put(firstEntry);
                        endDate = getNextTerm(contractRegisteredDate, period);
                    }
                    else if (i == timeBetween && isFinalInstallment) {
                        startDate = getNextTerm(installmentDate, period).minus(1, reportType.equals("yearly") ? ChronoUnit.YEARS : ChronoUnit.MONTHS);
                        endDate = getNextTerm(startDate, period);
                        leasePayment = futureLeasePayments;
                        interestExpense = futureLeasePayments - balance;
                        JSONObject finalEntry = createReportEntry(endDate.toString(), leasePayment, 0.0, interestExpense);
                        amortizationArray.put(finalEntry);
                        summaryArray.put(finalEntry);
                        break;
                    }
                    else if (i == 1 && count == 1 && getNextTerm(contractRegisteredDate, period).isEqual(getNextTerm(installmentDate, period))) {
                        continue;
                    }
                    else if (i == timeBetween && !isFinalInstallment) {
                        startDate = getNextTerm(installmentDate, period).minus(1, reportType.equals("yearly") ? ChronoUnit.YEARS : ChronoUnit.MONTHS);
                        endDate = getNextTerm(startDate, period);
                        double numberOfMonths = monthBetween(startDate, endDate);
                        leasePayment = futureLeasePayments;
                        interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment, numberOfMonths);
                        balance = balance + interestExpense - futureLeasePayments;

                        JSONObject reportEntry = createReportEntry(endDate.toString(), leasePayment, balance, interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    }
                    else if (i == 1 && count == 1) {
                        startDate = endDate;
                        interestExpense = calculateInterestExpense(initialLeaseLiability, discountRate, leasePayment, depreciationF);
                        balance += interestExpense;

                        JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance, interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    }
                    else if (isFinalInstallment && !endDate.equals(getNextTerm(installmentDate, period))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, period);
                        double numberOfMonths = monthBetween(startDate, endDate);
                        leasePayment = 0;

                        interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment, numberOfMonths);
                        balance += interestExpense;

                        JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance, interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    }
                    else if (i > 1 && count > 0 && !isFinalInstallment && !endDate.equals(getNextTerm(installmentDate, period))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, period);
                        double numberOfMonths = monthBetween(startDate, endDate);
                        interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment, numberOfMonths);
                        balance += interestExpense;

                        JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance, interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    }
                }

                count += 1;
                if (isFinalInstallment) break;
            }
    }

    private static JSONObject createReportEntry(String date, double leasePayment, double balance, double interestExpense) {
        JSONObject entry = new JSONObject();
        entry.put("year", date);
        entry.put("leasePayment", leasePayment);
        entry.put("balance", balance);
        entry.put("interestExpence", interestExpense);
        return entry;
    }

    public static double calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static double[] contractDuration(LocalDate contractStartDates, LocalDate contractEndDates,
            LocalDate contractRegisteredDate) {
        double totalnumberofmonths = monthBetween(contractStartDates, contractEndDates);
        double yearbetween = yearsBetween(contractRegisteredDate, contractEndDates);
        double yearbetweens = 0;
        if (contractRegisteredDate.isEqual(getNextTerm(contractRegisteredDate, "yearly").minusYears(1))) {
            yearbetween -= 1;
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

        return new double[] { yearbetween + yearbetweens };
    }

    private static void generateDepreciationReport(LocalDate startDate, LocalDate endDate, double rightOfUse,
            double constDepreciationPerY, double constDepreciationPerM, JSONArray reportArray,
            JSONArray summaryArray, LocalDate contractRegisteredDate) {
        double balance = rightOfUse;

        double depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));
        double depreciationBeforeLast = 0;
        double depreciationFirstPeriod = depreciationF * constDepreciationPerM;
        double firstRemainingMonth = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));
        double yearbetween = contractDuration(startDate, endDate, contractRegisteredDate)[0] + 1;
        double numberofmonths = 0;
        double totalnumberofmonths = monthBetween(startDate, endDate);

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
    }

    private static void processInstallments(Map<LocalDate, BigDecimal> installmentMap, double leaseLiability,
            double discountRate, JSONArray amortizationArray, JSONArray summaryArray, LocalDate startDate,
            LocalDate endDate, int numberofinstallment, LocalDate contractRegisteredDate, String contractTerm) {

        calculateReport(installmentMap, leaseLiability, discountRate, amortizationArray,
                summaryArray, startDate, endDate, numberofinstallment, contractRegisteredDate, contractTerm);

    }

    private static void calculateYearlyReport( Map<LocalDate, BigDecimal> installmentMap, double leaseLiability,
            double discountRate, JSONArray amortizationArray, JSONArray summaryArray, LocalDate startDate,
            LocalDate endDate, int numberofinstallment,LocalDate contractRegisteredDate) {

                // Custom logic for calculating the yearly report
                double balance = leaseLiability;
                double leasePayment = 0;
                double interestExpense = 0;
                int count = 1;
                double depreciationF = monthBetween(startDate, getNextTerm(contractRegisteredDate, "yearly"));
                

            for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
                LocalDate installmentDate = entry.getKey();
                BigDecimal installmentAmount = entry.getValue();
                double futureLeasePayments = installmentAmount.doubleValue();
                int yearsBetween = yearsBetween(startDate, getNextTerm(installmentDate, "yearly"));

                double initialLeaseLiability = leaseLiability;

                // Check if this is the final installment
                boolean isFinalInstallment = count + 1 == numberofinstallment + 1;

                for (int i = 0; i <= yearsBetween; i++) {

                    if (i == 0 && count == 1) {
                        balance = leaseLiability;
                        JSONObject finalYear = new JSONObject();
                        finalYear.put("year", contractRegisteredDate.toString());
                        finalYear.put("interestExpence", "-");
                        finalYear.put("leasePayment", leasePayment);
                        finalYear.put("balance", balance);
                        amortizationArray.put(finalYear);
                        endDate = getNextTerm(contractRegisteredDate, "yearly");
                    }

                    // Handle the calculation for the last installment date and amount
                    else if (i == yearsBetween && isFinalInstallment) {
                        startDate = getNextTerm(installmentDate, "yearly").minusYears(1);
                        endDate = getNextTerm(startDate, "yearly");
                        leasePayment = futureLeasePayments;
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                        interestExpense = futureLeasePayments - balance;
                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", endDate.toString());
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("balance", "-");
                        reportEntry.put("interestExpence", interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        // Break the loop to prevent further interest calculation after the final installment
                        break;
                    }

                    // Skip for the first iteration if the dates match
                    else if (i == 1 && count == 1 && getNextTerm(contractRegisteredDate, "yearly")
                            .isEqual(getNextTerm(installmentDate, "yearly"))) {
                        continue;
                    }

                    // Calculate for the rest of the installments (excluding the last one)
                    else if (i == yearsBetween && !isFinalInstallment) {
                        startDate = getNextTerm(installmentDate, "yearly").minusYears(1);
                        endDate = getNextTerm(startDate, "yearly");
                        double numberOfMonths = monthBetween(startDate, endDate);
                        leasePayment = futureLeasePayments;
                        interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                                numberOfMonths);
                        balance = balance + interestExpense - futureLeasePayments;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", endDate.toString());
                        reportEntry.put("leasePayment", leasePayment);
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence", interestExpense);
                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                        amortizationArray.put(reportEntry);
                        endDate = getNextTerm(endDate, "yearly");
                        summaryArray.put(reportEntry);
                    }

                    // Handle the calculation for the first installment period
                    else if (i == 1 && count == 1) {
                        startDate = endDate;
                        interestExpense = calculateInterestExpense(initialLeaseLiability, discountRate, leasePayment,
                                depreciationF);
                        balance += interestExpense;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", startDate.toString());
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence", interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                        endDate = getNextTerm(startDate, "yearly");
                    }

                    // Process final interest calculations if no future payments remain
                    else if (isFinalInstallment && !endDate.equals(getNextTerm(installmentDate, "yearly"))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        double numberOfMonths = monthBetween(startDate, endDate);
                        leasePayment = 0;

                        interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                                numberOfMonths);
                        balance += interestExpense;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", startDate.toString());
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence", interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    }

                    // Handle intermediate years' interest calculations
                    else if (i > 1 && count > 0 && !isFinalInstallment
                            && !endDate.equals(getNextTerm(installmentDate, "yearly"))) {
                        startDate = endDate;
                        endDate = getNextTerm(startDate, "yearly");
                        double numberOfMonths = monthBetween(startDate, endDate);
                        interestExpense = calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                                numberOfMonths);
                        balance += interestExpense;

                        JSONObject reportEntry = new JSONObject();
                        reportEntry.put("year", startDate.toString());
                        reportEntry.put("balance", balance);
                        reportEntry.put("interestExpence", interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);

                        leaseLiability = updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    }
                }

                    count += 1;

                    // Exit the outer loop after processing the final installment
                    if (isFinalInstallment) {
                        break;
                }
            }
    }

    private static double[] calculateDepreciation(double totalPayment, double advancePayment,
            double depreciationPerMonth,
            LocalDate startDate, LocalDate endDate) {
        double constDepreciationPerM;
        double constDepreciationPerY;
        double totalMonths = monthBetween(startDate, endDate);

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
        // Map<LocalDate, BigDecimal> installmentMap = parseInstallmentDetails(installmentDetails);
        double[] depreciation = calculateDepreciation(totalPayment, advancePayment, depreciationPerMonth, startDate,
                endDate);
        double constDepreciationPerM = depreciation[0];
        double constDepreciationPerY = depreciation[1];

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray summaryArray = new JSONArray();
        JSONArray detailArray = new JSONArray();
        JSONObject detail = new JSONObject();
        detail.put("contractYear", yearsBetween(startDate, endDate));
        detail.put("id", Id);
        detail.put("BranchId", BranchId);
        detail.put("branchName", branchName);
        detail.put("totalPayment", totalPayment);
        detail.put("advancePayment", advancePayment);
        detail.put("rightOfUse", rightOfUse);
        detail.put("depreciationPerMonth", constDepreciationPerY);
        detail.put("leaseLiability", leaseLiability);
        detail.put("contractType", contractType);
        detail.put("contractRegisteredDate", contractRegisteredDate);
        detail.put("startDate", startDate);
        detail.put("endDate", endDate);
        detailArray.put(detail);

        generateDepreciationReport(startDate, endDate, rightOfUse, constDepreciationPerY, constDepreciationPerM,
                reportArray, summaryArray, contractRegisteredDate);
        processInstallments(installmentMap, leaseLiability, discountRate, ammortizationArray, summaryArray,
                contractStartDates, contractEndDates,numberOfInstallments,contractRegisteredDate, contractTerm);

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("summaryArray", summaryArray);
        return reportObject.toString();

    }
    
    private static int yearsBetween(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        int yearbetween;
        if (monthBetween(startDate, endDate) <= 12) {
            yearbetween = period.getYears();

        } else {
            yearbetween = period.getYears() + 1;
        }
        return yearbetween;

    }

    static double monthBetween(LocalDate startDate, LocalDate endDate) {
        double averageDaysInMonth = 365.00000000000000000000001 / 12; // Approximate average number of

        // return ChronoUnit.DAYS.between(startDate, endDate) + 1 / 365;
        return (ChronoUnit.DAYS.between(startDate, endDate)) / averageDaysInMonth;
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

    public double calculateLeaseLiability(Double totalContractPrice, double advancePayment, double discountRate,
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

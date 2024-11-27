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

        JSONArray reportArray = new JSONArray();
        JSONArray ammortizationArray = new JSONArray();
        JSONArray detailArray = new JSONArray();

        JSONObject detail = new JSONObject();
        detail.put("contractMonth", ReportUtility.monthBetween(startDate, endDate));
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
        double depreciationFirst;

        if (ReportUtility.getNextTerm(startDate, "monthly")
                .equals(ReportUtility.getNextTerm(contractRegisteredDate, "monthly"))) {
            depreciationF = ReportUtility.monthBetween(startDate, ReportUtility.getNextTerm(contractRegisteredDate, "yearly"))
                    - (int) ReportUtility.monthBetween(startDate, ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));
        } else {
            depreciationF = ReportUtility.monthBetween(startDate, ReportUtility.getNextTerm(contractRegisteredDate, "monthly"));
        }

        depreciationFirst = depreciationF * constDepreciationPerM;

        if (installmentDetails != null) {

            JSONObject jsonObject = new JSONObject(installmentDetails);
            Map<LocalDate, BigDecimal> installmentMap = new TreeMap<>();

            int numberofinstallment = 0;
            for (String key : jsonObject.keySet()) {
                BigDecimal value = jsonObject.getBigDecimal(key);
                LocalDate date = LocalDate.parse(key);
                installmentMap.put(date, value);
                numberofinstallment += 1;

            }

            String contractTerm = "monthly";
            processInterestExpense(installmentMap, leaseLiability,
                    discountRate, ammortizationArray, reportArray, startDate,
                    endDate, numberofinstallment, contractRegisteredDate, contractTerm);

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
                finalYear.put("year", ReportUtility.getNextTerm(contractRegisteredDate,
                        "monthly").toString());
                finalYear.put("deprecationExp", depreciationFirst);
                reportArray.put(finalYear);
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

        JSONArray combinedArray = new JSONArray();
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
        double interestExpense = 0;
        int count = 1;
        int iteration = 0;

        // Determine the period (yearly or monthly) based on reportType
        String period = reportType.equalsIgnoreCase("yearly") ? "yearly" : "monthly";
        double depreciationF = ReportUtility.monthBetween(startDate,
                ReportUtility.getNextTerm(contractRegisteredDate, period));
        double depreciationFY = ReportUtility.monthBetween(startDate,
                ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));

        for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
            LocalDate installmentDate = entry.getKey();
            BigDecimal installmentAmount = entry.getValue();
            double futureLeasePayments = installmentAmount.doubleValue();

            // int timeBetween = reportType.equalsIgnoreCase("yearly")
            //         ? ReportUtility.yearsBetween(startDate, ReportUtility.getNextTerm(installmentDate, period))
            //         : (int) ReportUtility.monthBetween(startDate, ReportUtility.getNextTerm(installmentDate, period));
            int timeBetween = ReportUtility.yearsBetween(startDate,
                    ReportUtility.getNextTerm(installmentDate, "yearly"));

            double initialLeaseLiability = leaseLiability;
            boolean isFinalInstallment = count + 1 == numberOfInstallment + 1;
            for (int i = 0; i <= timeBetween; i++) {

                if (i == 0 && count == 1) {
                    balance = leaseLiability;
                    JSONObject firstEntry = createReportEntry(contractRegisteredDate.toString(), leasePayment, balance,
                            0.0);
                    amortizationArray.put(firstEntry);
                    endDate = ReportUtility.getNextTerm(contractRegisteredDate, "yearly");
                } else if (i == timeBetween && isFinalInstallment) {
                    startDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    leasePayment = futureLeasePayments;
                    interestExpense = futureLeasePayments - balance;
                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / 12;
                        startDate = ReportUtility.getNextTerm(startDate, "monthly");
                        for (int j = 1; j <= 12; j++) {
                            JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                    interestExpense);
                            amortizationArray.put(reportEntry);
                            summaryArray.put(reportEntry);
                            startDate = ReportUtility.getNextTerm(startDate, "monthly");
                        }
                    }
                    JSONObject finalEntry = createReportEntry(endDate.toString(), leasePayment, 0.0, interestExpense);
                    amortizationArray.put(finalEntry);
                    summaryArray.put(finalEntry);
                    break;
                } else if (i == timeBetween && !isFinalInstallment) {
                    startDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
                    leasePayment = futureLeasePayments;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            numberOfMonths);
                    balance = balance + interestExpense - futureLeasePayments;
                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / numberOfMonths;
                        startDate = ReportUtility.getNextTerm(startDate, "monthly");
                        for (int j = 1; j <= 12; j++) {
                            JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                    interestExpense);
                            amortizationArray.put(reportEntry);
                            summaryArray.put(reportEntry);
                            startDate = ReportUtility.getNextTerm(startDate, "monthly");
                        }
                    }
                    JSONObject reportEntry = createReportEntry(endDate.toString(), leasePayment, balance,
                            interestExpense);
                    amortizationArray.put(reportEntry);
                    summaryArray.put(reportEntry);

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                } else if (i == 1 && count == 1 && ReportUtility.getNextTerm(contractRegisteredDate, "yearly")
                        .isEqual(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
                    break;
                } else if (i == timeBetween && count != numberOfInstallment) {
                    LocalDate constarDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);

                    startDate = constarDate;
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    double numberOfmonths = ReportUtility.monthBetween(constarDate, endDate);
                    leasePayment = futureLeasePayments;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            numberOfmonths);
                    balance = balance + interestExpense - leasePayment;

                    for (int month = 0; month < 12; month++) {
                        interestExpense = interestExpense / 12;
                        startDate = ReportUtility.getNextTerm(startDate, "monthly");
                        JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);
                        startDate = ReportUtility.getNextTerm(startDate, "monthly");
                    }
                    JSONObject reportEntry = createReportEntry(endDate.toString(), leasePayment, balance,
                            interestExpense);
                    amortizationArray.put(reportEntry);
                    summaryArray.put(reportEntry);

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    endDate = ReportUtility.getNextTerm(startDate, "monthly");
                } else if (i == 1 && count == 1) {
                    startDate = endDate;
                    interestExpense = ReportUtility.calculateInterestExpense(initialLeaseLiability, discountRate,
                            leasePayment, depreciationF);
                    System.out.println("depreciationF" + depreciationF);
                    balance += interestExpense;
                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / 12;
                        startDate = ReportUtility.getNextTerm(contractRegisteredDate, "monthly");
                        for (int j = 1; j <= (int) depreciationFY; j++) {
                            JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                    interestExpense);
                            amortizationArray.put(reportEntry);
                            summaryArray.put(reportEntry);
                            startDate = ReportUtility.getNextTerm(startDate, "monthly");
                        }
                    }
                    JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                            interestExpense);
                    amortizationArray.put(reportEntry);
                    summaryArray.put(reportEntry);

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                } else if (isFinalInstallment
                        && !endDate.equals(ReportUtility.getNextTerm(installmentDate, "yearly"))) {

                    //     if (iteration == 0) {
                    //        startDate = endDate;
                    //    } 
                    startDate = endDate;
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
                    leasePayment = 0;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            numberOfMonths);
                    balance += interestExpense;
                    if (period.equals("monthly")) {
                        interestExpense = interestExpense / numberOfMonths;
                        System.out.println("startDate " + startDate);
                        for (int j = 1; j <= 12; j++) {

                            startDate = ReportUtility.getNextTerm(startDate, "monthly");
                            JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                                    interestExpense);
                            amortizationArray.put(reportEntry);
                            summaryArray.put(reportEntry);

                        }

                    }
                    iteration++;

                    JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance,
                            interestExpense);
                    amortizationArray.put(reportEntry);
                    summaryArray.put(reportEntry);

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                }
                // else if (i > 1 && count > 0 && !isFinalInstallment && !endDate.equals(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
                //     startDate = endDate;
                //     endDate = ReportUtility.getNextTerm(startDate, "yearly");
                //     double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
                //     interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment, numberOfMonths);
                //     balance += interestExpense;
                //     if (period.equals("monthly")) {
                //         interestExpense = interestExpense / numberOfMonths;

                //         for (int j = 1; j <= 12; j++) {
                //             startDate = ReportUtility.getNextTerm(startDate, "monthly");
                //             JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance, interestExpense);
                //             amortizationArray.put(reportEntry);
                //             summaryArray.put(reportEntry);
                //         }
                //     }

                //     JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance, interestExpense);
                //     amortizationArray.put(reportEntry);
                //     summaryArray.put(reportEntry);

                //     leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                // }
                else if (leasePayment == 0 && count == 1 && !ReportUtility.getNextTerm(endDate, "yearly")
                            .isEqual(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
                    startDate = endDate;
                    endDate = ReportUtility.getNextTerm(startDate, "yearly");
                    leasePayment = 0;
                    interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
                            ReportUtility.monthBetween(startDate, endDate));
                    balance += interestExpense;
                    

                    for (int month = 1; month <= 12; month++) {
                        interestExpense = interestExpense / 12;
                        JSONObject reportEntry = createReportEntry(startDate.toString(), leasePayment, balance, interestExpense);
                        amortizationArray.put(reportEntry);
                        summaryArray.put(reportEntry);
                        startDate = ReportUtility.getNextTerm(startDate, "monthly");
                    }

                    leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
                }
            }

            count += 1;
            if (isFinalInstallment) {
                break;
            }
        }
            // for (Map.Entry<LocalDate, BigDecimal> entry : installmentMap.entrySet()) {
            //     LocalDate installmentDate = entry.getKey();
            //     BigDecimal installmentAmount = entry.getValue();
            //     double futureLeasePayments = installmentAmount.doubleValue();
            //     int yearsBetween = ReportUtility.yearsBetween(startDate, ReportUtility.getNextTerm(installmentDate, "yearly"));

            //     double initialLeaseLiability = leaseLiability;

            //     // Check if this is the final installment
            //     boolean isFinalInstallment = count + 1 == numberOfInstallment + 1;

            //     for (int i = 0; i <= yearsBetween; i++) {

            //         if (i == 0 && count == 1) {
            //             balance = leaseLiability;
            //             JSONObject finalYear = new JSONObject();
            //             finalYear.put("year", contractRegisteredDate.toString());
            //             finalYear.put("interestExpence", "-");
            //             finalYear.put("leasePayment", leasePayment);
            //             finalYear.put("balance", balance);
            //             amortizationArray.put(finalYear);
            //             endDate = ReportUtility.getNextTerm(contractRegisteredDate, "yearly");
            //         }

            //         // Handle the calculation for the last installment date and amount
            //         else if (i == yearsBetween && isFinalInstallment) {
            //             startDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);
            //             endDate = ReportUtility.getNextTerm(startDate, "yearly");
            //             leasePayment = futureLeasePayments;
            //             leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //             interestExpense = futureLeasePayments - balance;
            //             JSONObject reportEntry = new JSONObject();
            //             reportEntry.put("year", endDate.toString());
            //             reportEntry.put("leasePayment", leasePayment);
            //             reportEntry.put("balance", "-");
            //             reportEntry.put("interestExpence", interestExpense);
            //             amortizationArray.put(reportEntry);
            //             summaryArray.put(reportEntry);

            //             // Break the loop to prevent further interest calculation after the final installment
            //             break;
            //         }

            //         // Skip for the first iteration if the dates match
            //         else if (i == 1 && count == 1 && ReportUtility.getNextTerm(contractRegisteredDate, "yearly")
            //                 .isEqual(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
            //             continue;
            //         }

            //         // Calculate for the rest of the installments (excluding the last one)
            //         else if (i == yearsBetween && !isFinalInstallment) {
            //             startDate = ReportUtility.getNextTerm(installmentDate, "yearly").minusYears(1);
            //             endDate = ReportUtility.getNextTerm(startDate, "yearly");
            //             double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
            //             leasePayment = futureLeasePayments;
            //             interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
            //                     numberOfMonths);
            //             balance = balance + interestExpense - futureLeasePayments;

            //             JSONObject reportEntry = new JSONObject();
            //             reportEntry.put("year", endDate.toString());
            //             reportEntry.put("leasePayment", leasePayment);
            //             reportEntry.put("balance", balance);
            //             reportEntry.put("interestExpence", interestExpense);
            //             leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //             amortizationArray.put(reportEntry);
            //             endDate = ReportUtility.getNextTerm(endDate, "yearly");
            //             summaryArray.put(reportEntry);
            //         }

            //         // Handle the calculation for the first installment period
            //         else if (i == 1 && count == 1) {
            //             startDate = endDate;
            //             interestExpense = ReportUtility.calculateInterestExpense(initialLeaseLiability, discountRate, leasePayment,
            //                     depreciationF);
            //             balance += interestExpense;

            //             JSONObject reportEntry = new JSONObject();
            //             reportEntry.put("year", startDate.toString());
            //             reportEntry.put("balance", balance);
            //             reportEntry.put("interestExpence", interestExpense);
            //             amortizationArray.put(reportEntry);
            //             summaryArray.put(reportEntry);

            //             leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //             endDate = ReportUtility.getNextTerm(startDate, "yearly");
            //         }

            //         // Process final interest calculations if no future payments remain
            //         else if (isFinalInstallment && !endDate.equals(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
            //             startDate = endDate;
            //             endDate = ReportUtility.getNextTerm(startDate, "yearly");
            //             double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
            //             leasePayment = 0;

            //             interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
            //                     numberOfMonths);
            //             balance += interestExpense;

            //             JSONObject reportEntry = new JSONObject();
            //             reportEntry.put("year", startDate.toString());
            //             reportEntry.put("balance", balance);
            //             reportEntry.put("interestExpence", interestExpense);
            //             amortizationArray.put(reportEntry);
            //             summaryArray.put(reportEntry);

            //             leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //         }

            //         // Handle intermediate years' interest calculations
            //         else if (i > 1 && count > 0 && !isFinalInstallment
            //                 && !endDate.equals(ReportUtility.getNextTerm(installmentDate, "yearly"))) {
            //             startDate = endDate;
            //             endDate = ReportUtility.getNextTerm(startDate, "yearly");
            //             double numberOfMonths = ReportUtility.monthBetween(startDate, endDate);
            //             interestExpense = ReportUtility.calculateInterestExpense(leaseLiability, discountRate, leasePayment,
            //                     numberOfMonths);
            //             balance += interestExpense;

            //             JSONObject reportEntry = new JSONObject();
            //             reportEntry.put("year", startDate.toString());
            //             reportEntry.put("balance", balance);
            //             reportEntry.put("interestExpence", interestExpense);
            //             amortizationArray.put(reportEntry);
            //             summaryArray.put(reportEntry);

            //             leaseLiability = ReportUtility.updateLeaseLiability(leaseLiability, interestExpense, leasePayment);
            //         }
            //     }

            //         count += 1;

            //         // Exit the outer loop after processing the final installment
            //         if (isFinalInstallment) {
            //             break;
            //     }
            // }
    }

    private static JSONObject createReportEntry(String date, double leasePayment, double balance, double interestExpense) {
        JSONObject entry = new JSONObject();
        entry.put("year", date);
        entry.put("leasePayment", leasePayment);
        entry.put("balance", balance);
        entry.put("interestExpence", interestExpense);
        return entry;
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
        } else if (ReportUtility.getNextTerm(contractStartDates, "yearly").isBefore(ReportUtility.getNextTerm(contractRegisteredDate, "yearly"))) {

            yearbetweens = 1;

        } else if (ReportUtility.getNextTerm(contractStartDates, "yearly").isEqual(ReportUtility.getNextTerm(contractRegisteredDate, "yearly"))) {
            yearbetweens = 2;
        }

        return new double[]{yearbetween + yearbetweens};
    }

    private static void generateDepreciationReport(LocalDate startDate, LocalDate endDate, double rightOfUse,
            double constDepreciationPerY, double constDepreciationPerM, JSONArray reportArray,
            JSONArray summaryArray, LocalDate contractRegisteredDate) {
        double balance = rightOfUse;

        double depreciationF = ReportUtility.monthBetween(startDate, ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));
        double depreciationBeforeLast = 0;
        double depreciationFirstPeriod = depreciationF * constDepreciationPerM;
        double firstRemainingMonth = ReportUtility.monthBetween(startDate, ReportUtility.getNextTerm(contractRegisteredDate, "yearly"));
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

    private static void processInterestExpense(Map<LocalDate, BigDecimal> installmentMap, double leaseLiability,
            double discountRate, JSONArray amortizationArray, JSONArray summaryArray, LocalDate startDate,
            LocalDate endDate, int numberofinstallment, LocalDate contractRegisteredDate, String contractTerm) {

        calculateInterestExpense(installmentMap, leaseLiability, discountRate, amortizationArray,
                summaryArray, startDate, endDate, numberofinstallment, contractRegisteredDate, contractTerm);

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

        return new double[]{constDepreciationPerM, constDepreciationPerY};
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
        detail.put("contractYear", ReportUtility.yearsBetween(startDate, endDate));
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
        processInterestExpense(installmentMap, leaseLiability, discountRate, ammortizationArray, summaryArray,
                contractStartDates, contractEndDates, numberOfInstallments, contractRegisteredDate, contractTerm);

        JSONObject reportObject = new JSONObject();
        reportObject.put("detail", detailArray);
        reportObject.put("report", reportArray);
        reportObject.put("ammortization", ammortizationArray);
        reportObject.put("summaryArray", summaryArray);
        return reportObject.toString();

    }

}

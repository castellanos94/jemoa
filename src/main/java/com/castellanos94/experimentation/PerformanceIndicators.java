package com.castellanos94.experimentation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.castellanos94.utils.StacClient;

import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class PerformanceIndicators {
        public static void main(String[] args) throws IOException {
                String path = "experiments/metricas_dtlz.csv";
                summary(Table.read().csv(path), "Problema");
        }

        @SuppressWarnings("unchecked")
        private static void summary(Table table, String key) throws IOException {

                List<String> group_by = (List<String>) table.column(key).unique().asList();
                Table resume = null;
                DoubleColumn min, minp, max, maxp, avg, avgp, dom, domp, hsat, hsatp, sat, satp;
                for (String labelString : group_by) {
                        Table result = table.where(t -> t.stringColumn(key).isEqualTo(labelString));
                        min = result.doubleColumn(ExperimentationDTLZPreferences.euclidean.name()).setName("NSGA-III");
                        minp = result.doubleColumn(ExperimentationDTLZPreferences.euclidean_p.name())
                                        .setName("NSGA-III-P");

                        if (resume == null)
                                resume = doSummary(table, min, minp, labelString, "min euclidean");
                        else {
                                resume.addRow(doSummary(table, min, minp, labelString, "min euclidean").row(0));
                        }
                        max = result.doubleColumn(ExperimentationDTLZPreferences.max_euclidean.name())
                                        .setName("NSGA-III");
                        maxp = result.doubleColumn(ExperimentationDTLZPreferences.max_euclidean_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, max, maxp, labelString, "max euclidean").row(0));

                        avg = result.doubleColumn(ExperimentationDTLZPreferences.avg_euclidean.name())
                                        .setName("NSGA-III");
                        avgp = result.doubleColumn(ExperimentationDTLZPreferences.avg_euclidean_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, avg, avgp, labelString, "avg euclidean").row(0));

                        max = result.doubleColumn(ExperimentationDTLZPreferences.max_chebyshev.name())
                                        .setName("NSGA-III");
                        maxp = result.doubleColumn(ExperimentationDTLZPreferences.max_chebyshev_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, max, maxp, labelString, "max chebyshev").row(0));

                        avg = result.doubleColumn(ExperimentationDTLZPreferences.avg_chebyshev.name())
                                        .setName("NSGA-III");
                        avgp = result.doubleColumn(ExperimentationDTLZPreferences.avg_chebyshev_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, avg, avgp, labelString, "avg chebyshev").row(0));

                        min = result.doubleColumn(ExperimentationDTLZPreferences.chebyshev.name()).setName("NSGA-III");
                        minp = result.doubleColumn(ExperimentationDTLZPreferences.chebyshev_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, min, minp, labelString, "min chebyshev").row(0));
                        dom = result.doubleColumn(ExperimentationDTLZPreferences.rate_dom.name()).setName("NSGA-III");
                        domp = result.doubleColumn(ExperimentationDTLZPreferences.rate_dom_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, dom, domp, labelString, "dominance").row(0));

                        hsat = result.doubleColumn(ExperimentationDTLZPreferences.rate_hsat.name()).setName("NSGA-III");
                        hsatp = result.doubleColumn(ExperimentationDTLZPreferences.rate_hsat_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, hsat, hsatp, labelString, "hsat").row(0));

                        sat = result.doubleColumn(ExperimentationDTLZPreferences.rate_sat.name()).setName("NSGA-III");
                        satp = result.doubleColumn(ExperimentationDTLZPreferences.rate_sat_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, sat, satp, labelString, "sat").row(0));

                }
                min = table.doubleColumn(ExperimentationDTLZPreferences.euclidean.name()).setName("NSGA-III");
                minp = table.doubleColumn(ExperimentationDTLZPreferences.euclidean_p.name()).setName("NSGA-III-P");

                if (resume == null)
                        resume = doSummary(table, min, minp, "dtlz", "min euclidean");
                else {
                        resume.addRow(doSummary(table, min, minp, "dtlz", "min euclidean").row(0));
                }
                max = table.doubleColumn(ExperimentationDTLZPreferences.max_euclidean.name()).setName("NSGA-III");
                maxp = table.doubleColumn(ExperimentationDTLZPreferences.max_euclidean_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, max, maxp, "dtlz", "max euclidean").row(0));

                avg = table.doubleColumn(ExperimentationDTLZPreferences.avg_euclidean.name()).setName("NSGA-III");
                avgp = table.doubleColumn(ExperimentationDTLZPreferences.avg_euclidean_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, avg, avgp, "dtlz", "avg euclidean").row(0));

                max = table.doubleColumn(ExperimentationDTLZPreferences.max_chebyshev.name()).setName("NSGA-III");
                maxp = table.doubleColumn(ExperimentationDTLZPreferences.max_chebyshev_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, max, maxp, "dtlz", "max chebyshev").row(0));

                avg = table.doubleColumn(ExperimentationDTLZPreferences.avg_chebyshev.name()).setName("NSGA-III");
                avgp = table.doubleColumn(ExperimentationDTLZPreferences.avg_chebyshev_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, avg, avgp, "dtlz", "avg chebyshev").row(0));

                min = table.doubleColumn(ExperimentationDTLZPreferences.chebyshev.name()).setName("NSGA-III");
                minp = table.doubleColumn(ExperimentationDTLZPreferences.chebyshev_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, min, minp, "dtlz", "min chebyshev").row(0));
                dom = table.doubleColumn(ExperimentationDTLZPreferences.rate_dom.name()).setName("NSGA-III");
                domp = table.doubleColumn(ExperimentationDTLZPreferences.rate_dom_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, dom, domp, "dtlz", "dominance").row(0));

                hsat = table.doubleColumn(ExperimentationDTLZPreferences.rate_hsat.name()).setName("NSGA-III");
                hsatp = table.doubleColumn(ExperimentationDTLZPreferences.rate_hsat_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, hsat, hsatp, "dtlz", "hsat").row(0));

                sat = table.doubleColumn(ExperimentationDTLZPreferences.rate_sat.name()).setName("NSGA-III");
                satp = table.doubleColumn(ExperimentationDTLZPreferences.rate_sat_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, sat, satp, "dtlz", "sat").row(0));
                resume.write().csv(
                                ExperimentationDTLZPreferences.DIRECTORY_EXPERIMENTS + File.separator + "resume.csv");

        }

        private static Table doSummary(Table table, DoubleColumn a, DoubleColumn b, String key, String metric)
                        throws IOException {
                Table apply = table.summarize(a, b, AggregateFunctions.count, AggregateFunctions.min,
                                AggregateFunctions.mean, AggregateFunctions.max, AggregateFunctions.stdDev).apply();

                StringColumn name = StringColumn.create("Problem");
                name.append(key);

                Table tmpTable = Table.create(a, b);
                File file = File.createTempFile("data", ".csv");
                file.deleteOnExit();
                tmpTable.write().csv(file);
                Map<String, Object> wilcoxon = StacClient.WILCOXON(file.getAbsolutePath(), 0.05);
                Double rs = Double.parseDouble(wilcoxon.get("result").toString());
                StringColumn wilcoxon_ = StringColumn.create("Wilcoxon test");
                wilcoxon_.append((rs == 1) ? "H0 is rejected" : "H0 is accepted");

                StringColumn wilcoxon_technical = StringColumn.create("Wilcoxon technical");
                wilcoxon_technical.append(wilcoxon.toString());
                StringColumn indicator = StringColumn.create("Metric");
                indicator.append(metric);
                Table copy = apply.addColumns(name, wilcoxon_, wilcoxon_technical, indicator).copy();
                return copy;
        }
}

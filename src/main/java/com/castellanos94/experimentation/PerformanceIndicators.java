package com.castellanos94.experimentation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import client.StacConsumer;
import model.ParametricTestTwoGroups;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
/**
 * Version anterior para comparar las soluciones de un algoritmo NSGA3 vs alguna variante, emplea Wilcoxon para la prueba estadistica. 
 */
public class PerformanceIndicators {
        public static void main(String[] args) throws IOException {
                String path = "experiments/metricas_dtlz.csv";
                summary(Table.read().csv(path), "Problema");
        }

        @SuppressWarnings("unchecked")
        private static void summary(Table table, String key) throws IOException {

                List<String> group_by = (List<String>) table.column(key).unique().asList();
                Table resume = null;
                DoubleColumn min, minp, max, maxp, avg, avgp, dom, domp;
                Column<?> satp;
                Column<?> sat;
                Column<?> hsat;
                Column<?> hsatp;
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

                        hsat = result.numberColumn(ExperimentationDTLZPreferences.rate_hsat.name()).setName("NSGA-III");
                        hsatp = result.numberColumn(ExperimentationDTLZPreferences.rate_hsat_p.name())
                                        .setName("NSGA-III-P");
                        resume.addRow(doSummary(table, hsat, hsatp, labelString, "hsat").row(0));

                        sat = result.numberColumn(ExperimentationDTLZPreferences.rate_sat.name()).setName("NSGA-III");
                        satp = result.numberColumn(ExperimentationDTLZPreferences.rate_sat_p.name())
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

                hsat = table.numberColumn(ExperimentationDTLZPreferences.rate_hsat.name()).setName("NSGA-III");
                hsatp = table.numberColumn(ExperimentationDTLZPreferences.rate_hsat_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, hsat, hsatp, "dtlz", "hsat").row(0));

                sat = table.numberColumn(ExperimentationDTLZPreferences.rate_sat.name()).setName("NSGA-III");
                satp = table.numberColumn(ExperimentationDTLZPreferences.rate_sat_p.name()).setName("NSGA-III-P");
                resume.addRow(doSummary(table, sat, satp, "dtlz", "sat").row(0));
                resume.write().csv(
                                ExperimentationDTLZPreferences.DIRECTORY_EXPERIMENTS + File.separator + "resume.csv");

        }

        private static Table doSummary(Table table, Column<?> hsat, Column<?> hsatp, String key, String metric)
                        throws IOException {
                Table apply = table.summarize(hsat, hsatp, AggregateFunctions.count, AggregateFunctions.min,
                                AggregateFunctions.mean, AggregateFunctions.max, AggregateFunctions.stdDev).apply();

                StringColumn name = StringColumn.create("Problem");
                name.append(key);

                Table tmpTable = Table.create(hsat, hsatp);
                File file = File.createTempFile("data", ".csv");
                file.deleteOnExit();
                tmpTable.write().csv(file);
                
                ParametricTestTwoGroups wilcoxon = StacConsumer.WILCOXON(file.getAbsolutePath(), "NSGA-III","NSGA-III-P",0.05);
                Double rs;
                if (wilcoxon.getResult() != null)
                        rs = wilcoxon.getResult().doubleValue();
                else
                        rs = Double.NaN;
                StringColumn wilcoxon_ = StringColumn.create("Wilcoxon test");
                if (!Double.isNaN(rs))
                        wilcoxon_.append((rs == 1) ? "H0 is rejected" : "H0 is accepted");
                else
                        wilcoxon_.append("NaN");

                StringColumn wilcoxon_technical = StringColumn.create("Wilcoxon technical");
                if (!Double.isNaN(rs))
                        wilcoxon_technical.append(wilcoxon.toString());
                else
                        wilcoxon_technical.append("Error with data or server error");
                StringColumn indicator = StringColumn.create("Metric");
                indicator.append(metric);
                return apply.addColumns(name, wilcoxon_, wilcoxon_technical, indicator).copy();
        }
}

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Main <input.csv> <output.csv>");
            return;
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        if (!Files.exists(input)) {
            System.err.println("Input file not found: " + input);
            return;
        }

        Map<String, TreeMap<Integer, TreeMap<Integer, BigDecimal>>> store = new TreeMap<>();
        int startYr = Integer.MAX_VALUE;
        int endYr = Integer.MIN_VALUE;
        int endDevYr = Integer.MIN_VALUE;
        int badLines = 0;
        int goodLines = 0;

        try (BufferedReader br = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            boolean hdrFound = false;
            Map<String,Integer> hdrMap = null;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                if (!hdrFound) {
                    String[] hdr = CsvUtil.splitCsv(line);
                    hdrMap = HeaderUtil.detectHeader(hdr);
                    if (hdrMap == null) {
                        hdrMap = HeaderUtil.defaultMap();
                    } else {
                        hdrFound = true;
                        continue;
                    }
                }
                String[] parts = CsvUtil.splitCsv(line);
                Integer idxProduct = hdrMap.get("product");
                Integer idxOrigin = hdrMap.get("origin");
                Integer idxDev = hdrMap.get("development");
                Integer idxIncremental = hdrMap.get("incremental");
                
                if (idxProduct >= parts.length || idxOrigin >= parts.length || idxDev >= parts.length) {
                    System.err.println("Warning: skipping malformed line " + lineNo + " (missing columns): " + line);
                    badLines++;
                    continue;
                }

                String prodName = parts[idxProduct].trim();
                prodName = CsvUtil.unquote(prodName);
                Integer originYear = NumberUtil.parseIntSafe(parts[idxOrigin].trim());
                Integer devYear = NumberUtil.parseIntSafe(parts[idxDev].trim());
                BigDecimal incVal = BigDecimal.ZERO;

                if (idxIncremental < parts.length) {
                    String incS = parts[idxIncremental].trim();
                    if (!incS.isEmpty()) {
                        BigDecimal parsedVal = NumberUtil.parseBigDecimalFlexible(incS);
                        if (parsedVal == null) {
                            System.err.println("Warning: couldn't parse incremental value at line " + lineNo + " -> treating as 0: '" + incS + "'");
                        } else {
                            incVal = parsedVal;
                        }
                    }
                }
                if (originYear == null || devYear == null) {
                    System.err.println("Warning: skipping line with bad years at " + lineNo + ": " + line);
                    badLines++;
                    continue;
                }
                
                goodLines++;
                startYr = Math.min(startYr, originYear);
                endYr = Math.max(endYr, originYear);
                endDevYr = Math.max(endDevYr, devYear);
                store.computeIfAbsent(prodName, k -> new TreeMap<>())
                    .computeIfAbsent(originYear, k -> new TreeMap<>())
                    .merge(devYear, incVal, BigDecimal::add);
            }
        }

        if (startYr == Integer.MAX_VALUE) {
            System.err.println("No data found in input file.");
            return;
        }

        try (BufferedWriter bw = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            int numDevYears = endDevYr - startYr + 1;
            if (numDevYears < 1) numDevYears = 0;
            bw.write(startYr + ", " + numDevYears);
            bw.newLine();
            for (String product : store.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(product);
                TreeMap<Integer, TreeMap<Integer, BigDecimal>> prodMap = store.get(product);
                for (int origin = startYr; origin <= endYr; origin++) {
                    int maxK = endDevYr - origin + 1;
                    if (maxK <= 0) continue;
                    BigDecimal cumulative = BigDecimal.ZERO;
                    for (int k = 1; k <= maxK; k++) {
                        int calYear = origin + k - 1;
                        BigDecimal incr = BigDecimal.ZERO;
                        if (prodMap.containsKey(origin)) {
                            BigDecimal v = prodMap.get(origin).get(calYear);
                            if (v != null) incr = v;
                        }
                        cumulative = cumulative.add(incr);
                        sb.append(", ").append(NumberUtil.bigDecimalToString(cumulative));
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }

        System.out.println("Parsed lines: " + goodLines + ", malformed/skipped: " + badLines);
        System.out.println("Output written to: " + output.toAbsolutePath());
    }
}

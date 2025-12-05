import java.math.BigDecimal;

public class NumberUtil {
    
    public static Integer parseIntSafe(String s) {
        s = s.replaceAll("\"", "").trim();
        if (s.isEmpty()) return null;

        try {
            return Integer.parseInt(s);
        } 
        catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal parseBigDecimalFlexible(String s) {
        s = s.trim();
        if (s.isEmpty()) return BigDecimal.ZERO;

        s = s.replace("\uFEFF", "");
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        s = s.trim();

        try {
            return new BigDecimal(s);
        } 
        catch (Exception ignored) {}

        int lastComma = s.lastIndexOf(',');
        int lastDot = s.lastIndexOf('.');
        if (lastComma != -1 && lastDot != -1) {
            if (lastDot > lastComma) {
                String t = s.replace(",", "");
                try { return new BigDecimal(t); } catch (Exception ignored) {}
            } else {
                String t = s.replace(".", "").replace(',', '.');
                try { return new BigDecimal(t); } catch (Exception ignored) {}
            }
        }

        if (s.indexOf(',') != -1 && s.indexOf('.') == -1) {
            String t = s.replace(",", "");
            try { return new BigDecimal(t); } catch (Exception ignored) {}
            t = s.replace(',', '.');
            try { return new BigDecimal(t); } catch (Exception ignored) {}
        }

        if (s.indexOf('.') != -1 && s.indexOf(',') == -1) {
            int countDots = s.length() - s.replace(".", "").length();
            if (countDots > 1) {
                int last = s.lastIndexOf('.');
                String t = s.substring(0, last).replace(".", "") + s.substring(last);
                try { return new BigDecimal(t); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static String bigDecimalToString(BigDecimal v) {
        if (v == null) return "0";
        BigDecimal stripped = v.stripTrailingZeros();
        try {
            if (stripped.scale() <= 0) {
                return stripped.toPlainString();
            } else {
                return stripped.toPlainString();
            }
        } catch (ArithmeticException e) {
            return v.toPlainString();
        }
    }
}
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    public static String[] splitCsv(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i += 2;
                    continue;
                } else {
                    inQuotes = !inQuotes;
                    i++;
                    continue;
                }
            }

            if (c == ',' && !inQuotes) {
                parts.add(cur.toString());
                cur.setLength(0);
                i++;
            } 
            else {
                cur.append(c);
                i++;
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }

    public static String unquote(String s) {
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
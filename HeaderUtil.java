import java.util.HashMap;
import java.util.Map;

public class HeaderUtil {
    
    public static Map<String,Integer> detectHeader(String[] hdr) {
        if (hdr == null || hdr.length == 0) return null;
        Map<String,Integer> map = new HashMap<>();

        for (int i = 0; i < hdr.length; i++) {
            String h = hdr[i].toLowerCase().replaceAll("[^a-z0-9_ ]", "").trim();
            if (h.contains("product")) map.put("product", i);
            if (h.contains("origin")) map.put("origin", i);
            if (h.contains("development") || h.contains("dev") || h.contains("calendar")) map.put("development", i);
            if (h.contains("increment") || h.contains("paid") || h.contains("value") || h.contains("amount")) map.put("incremental", i);
        }
        if (map.containsKey("product") && map.containsKey("origin") && map.containsKey("development")) {
            map.putIfAbsent("incremental", hdr.length);
            return map;
        }
        return null;
    }

    public static Map<String,Integer> defaultMap() {
        Map<String,Integer> map = new HashMap<>();
        map.put("product", 0);
        map.put("origin", 1);
        map.put("development", 2);
        map.put("incremental", 3);
        return map;
    }
}
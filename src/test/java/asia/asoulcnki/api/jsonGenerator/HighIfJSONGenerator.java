package asia.asoulcnki.api.jsonGenerator;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.persistence.entity.Reply;
import com.alibaba.fastjson.JSONArray;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HighIfJSONGenerator {
    private HighIfJSONGenerator() {
    }

    static void highIf() {
        ComparisonDatabase db = ComparisonDatabase.getInstance();
        long start = System.currentTimeMillis();
        List<Reply> replies =
            db.getReplyMap().values().stream().filter(e -> e.getSimilarCount() > 3).sorted(Comparator.comparing(Reply::getSimilarCount).reversed()).collect(Collectors.toList());
        long end = System.currentTimeMillis();
        System.out.printf("cost %d ms ", end - start);
        String jsonString = JSONArray.toJSONString(replies, true);
        writeJson("high-if.json", jsonString);
    }

    static void highLikeCount() {
        ComparisonDatabase db = ComparisonDatabase.getInstance();
        long start = System.currentTimeMillis();
        long allCount = db.getReplyMap().values().stream().filter(r -> r.getLikeNum() > 50).count();
        System.out.println(allCount);
        List<Reply> replies =
            db.getReplyMap().values().stream().filter(e -> e.getSimilarCount() >= 1).sorted(Comparator.comparing(Reply::getSimilarLikeSum).reversed()).collect(Collectors.toList());
        System.out.println(replies.size());
        long count = replies.stream().filter(r -> r.getSimilarLikeSum() > 10).count();
        long count2 = replies.stream().filter(r -> r.getSimilarCount() > 1).count();
        System.out.println(count);
        System.out.println(count2);
        long end = System.currentTimeMillis();
        System.out.printf("cost %d ms ", end - start);
        String jsonString = JSONArray.toJSONString(replies, true);
        writeJson("high-if.json", jsonString);
    }


    public static void main(String[] args) {
        highLikeCount();
    }

    static void writeJson(String fileName, String data) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8);
            osw.write(data);
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

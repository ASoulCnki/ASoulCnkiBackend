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
		List<Reply> replies =
				db.getReplyMap().values().stream().filter(e -> e.getSimilarCount() >= 1).sorted(Comparator.comparing(Reply::getSimilarLikeSum).reversed()).collect(Collectors.toList());
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

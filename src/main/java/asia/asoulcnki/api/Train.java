package asia.asoulcnki.api;

import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.persistence.entity.Reply;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Train {
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		List<Reply> node = getJsonFile("data/bilibili_cnki_reply.json");
		if (node == null) {
			System.out.println("file do not exists");
			return;
		}
		ComparisonDatabase db = ComparisonDatabase.getInstance();
		for (Reply reply : node) {
			db.addReplyData(reply);
		}
		db.dumpToImage(ComparisonDatabase.DEFAULT_IMAGE_PATH);
		System.out.printf("train end cost %d ms", System.currentTimeMillis() - start);
	}

	public static List<Reply> getJsonFile(String path) {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			JavaType javaType = objMapper.getTypeFactory().constructCollectionType(List.class, Reply.class);
			return objMapper.readValue(new File(path), javaType);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}

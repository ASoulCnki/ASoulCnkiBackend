package asia.asoulcnki.api.service.impl;

import asia.asoulcnki.api.common.BizException;
import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.common.response.CnkiCommonEnum;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.vo.ControlResultVo;
import asia.asoulcnki.api.service.IDataService;
import asia.asoulcnki.api.service.IRankingService;
import asia.asoulcnki.api.service.IReplyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@CacheConfig(cacheNames = "caffeineCacheManager")
public class IDataServiceImpl implements IDataService {
	private final static Logger log = LoggerFactory.getLogger(IDataServiceImpl.class);
	@Autowired
	IReplyService replyService;

	@Autowired
	IRankingService rankingService;

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

	@Cacheable(key = "#startTime", value = "defaultCache")
	public long getStartRpid(int startTime) {
		QueryWrapper<Reply> queryWrapper = new QueryWrapper<>();
		queryWrapper.gt("ctime", startTime).select("min(rpid) as min_rpid");
		List<Object> r = replyService.getBaseMapper().selectObjs(queryWrapper);
		if (r == null) {
			return 0;
		} else {
			return (long) r.get(0);
		}
	}

	@Override
	@CacheEvict(value = "replyCache", allEntries = true)
	public ControlResultVo pull(int startTime) {
		long queryStartRpidStart = System.currentTimeMillis();
		long startRpid = getStartRpid(startTime);
		long queryStartRpidEnd = System.currentTimeMillis();
		log.info("query start rpid end, cost {} ms, start rpid : {}", queryStartRpidEnd - queryStartRpidStart,
				startRpid);

		ComparisonDatabase db = ComparisonDatabase.getInstance();

		int pageIndex = 1;
		int pageSize = 50000;


		int count = 0;
		while (true) {
			// construct query wrapper
			QueryWrapper<Reply> queryWrapper = new QueryWrapper<>();
			queryWrapper.gt("rpid", startRpid);
			String lastSql = String.format("limit %d,%d", (pageIndex - 1) * pageSize, pageSize);
			queryWrapper.last(lastSql);

			long start = System.currentTimeMillis();
			// query database
			List<Reply> replies = replyService.list(queryWrapper);
			if (replies == null || replies.isEmpty()) {
				break;
			}

			// add to comparison database
			db.writeLock();
			try {
				replies.forEach(db::addReplyData);
			} finally {
				db.writeUnLock();
			}

			count += replies.size();
			pageIndex++;
			log.info("add {} records to database, cost {} ms", replies.size(), System.currentTimeMillis() - start);
		}

		long pullDataEnd = System.currentTimeMillis();
		log.info("pull data cost {} ms, add {} records to comparison database in total",
				pullDataEnd - queryStartRpidEnd, count);
		return checkpoint();
	}

	@Override
	public ControlResultVo checkpoint() {
		long start = System.currentTimeMillis();
		ComparisonDatabase db = ComparisonDatabase.getInstance();
		db.readLock();
		try {
			db.dumpToImage(ComparisonDatabase.DEFAULT_DATA_DIR, ComparisonDatabase.DEFAULT_IMAGE_FILE_NAME);
			rankingService.refresh();
		} catch (Exception e) {
			throw new BizException(CnkiCommonEnum.INTERNAL_SERVER_ERROR, e);
		} finally {
			db.readUnLock();
		}
		log.info("checkpoint database finished, cost {} ms", System.currentTimeMillis() - start);
		return new ControlResultVo(db.getMinTime(), db.getMaxTime());
	}

	@Override
	@CacheEvict(value = "replyCache", allEntries = true)
	public ControlResultVo reset() {
		ComparisonDatabase db = ComparisonDatabase.getInstance();
		try {
			db.reset();
		} catch (Exception e) {
			throw new BizException(CnkiCommonEnum.INTERNAL_SERVER_ERROR, e);
		}
		return checkpoint();
	}

	@Override
	@CacheEvict(value = "replyCache", allEntries = true)
	public ControlResultVo train() {
		try {
			long start = System.currentTimeMillis();
			List<Reply> node = getJsonFile("data/bilibili_cnki_reply.json");
			if (node == null) {
				throw new BizException(CnkiCommonEnum.INTERNAL_SERVER_ERROR);
			}
			ComparisonDatabase db = ComparisonDatabase.getInstance();
			for (Reply reply : node) {
				db.addReplyData(reply);
			}
			log.info("train end cost {} ms", System.currentTimeMillis() - start);
		} catch (Exception e) {
			throw new BizException(CnkiCommonEnum.INTERNAL_SERVER_ERROR, e);
		}
		return checkpoint();
	}
}

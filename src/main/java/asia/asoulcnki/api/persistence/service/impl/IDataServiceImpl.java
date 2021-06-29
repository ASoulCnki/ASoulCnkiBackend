package asia.asoulcnki.api.persistence.service.impl;

import asia.asoulcnki.api.common.BizException;
import asia.asoulcnki.api.common.duplicationcheck.ComparisonDatabase;
import asia.asoulcnki.api.common.response.CnkiCommonEnum;
import asia.asoulcnki.api.persistence.entity.Reply;
import asia.asoulcnki.api.persistence.service.IDataService;
import asia.asoulcnki.api.persistence.service.IReplyService;
import asia.asoulcnki.api.persistence.vo.ControlResultVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "caffeineCacheManager")
public class IDataServiceImpl implements IDataService {
	private final static Logger log = LoggerFactory.getLogger(IDataServiceImpl.class);
	@Autowired
	IReplyService IReplyService;


	@Cacheable(key = "#startTime", value = "defaultCache")
	public long getStartRpid(int startTime) {
		QueryWrapper<Reply> queryWrapper = new QueryWrapper<>();
		queryWrapper.gt("ctime", startTime).select("min(rpid) as min_rpid");
		List<Object> r = IReplyService.getBaseMapper().selectObjs(queryWrapper);
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
		int pageSize = 20000;


		int count = 0;
		while (true) {
			// construct query wrapper
			QueryWrapper<Reply> queryWrapper = new QueryWrapper<>();
			queryWrapper.gt("rpid", startRpid);
			String lastSql = String.format("limit %d,%d", (pageIndex - 1) * pageSize, pageSize);
			queryWrapper.last(lastSql);

			long start = System.currentTimeMillis();
			// query database
			List<Reply> replies = IReplyService.list(queryWrapper);
			if (replies == null || replies.isEmpty()) {
				break;
			}

			// add to comparison database
			db.writeLock();
			try {
				replies.forEach(db::addReplyData);
			} catch (Exception e) {
				break;
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
		try {
			db.dumpToImage(ComparisonDatabase.DEFAULT_IMAGE_PATH);
		} catch (Exception e) {
			throw new BizException(CnkiCommonEnum.INTERNAL_SERVER_ERROR, e);
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
}

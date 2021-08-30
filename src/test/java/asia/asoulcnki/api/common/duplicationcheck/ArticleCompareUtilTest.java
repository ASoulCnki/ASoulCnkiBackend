package asia.asoulcnki.api.common.duplicationcheck;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class ArticleCompareUtilTest {

    @Test
    public void testTrim(){
        String s = "7月9日，认识了一个男人，他跟我聊叔本华，聊弗洛伊，聊庄子妻死，聊伽罗瓦和近世代数，聊彭罗斯和宇宙督察假说\n" + "\n" + "7月10日，我让他b站关注嘉然今天吃什么";
        int trimmedLength = ArticleCompareUtil.textLength(ArticleCompareUtil.trim(s));
        assert trimmedLength == 66;
    }
}

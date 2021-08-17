package asia.asoulcnki.api;

import asia.asoulcnki.api.service.IDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

@SpringBootTest
public class StartTrainTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private IDataService dataService;

    @Test
    public void testStartTrain() {
        // 非构建环境下需要训练结果文件
        if (!"true".equals(System.getenv("GHA_BUILD_ENV"))) {
            File folder = new File("data/bilibili_cnki_reply.json");
            assertTrue(folder.exists(), "需要在项目根目录下创建 data 文件夹，然后放入 bilibili_cnki_reply.json 文件捏");
            File dataset = new File("data/database.dat");
            if (!dataset.exists()) {
                dataService.train();
            }
            assertTrue(dataset.exists(), "未检测到训练结果文件，可能是训练结果文件名改变，请检查代码捏");
            System.out.println("训练成功捏，训练结果已生成，接下来可以正常调取接口了捏");
        }
    }
}

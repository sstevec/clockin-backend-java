package backend.clockin;

import backend.clockin.service.RecognizeFaceService;
import com.xunsiya.modules.algorithm.AlgorithmConfig;
import com.xunsiya.modules.algorithm.AllXmlrpcClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ClockinApplication.class)
@WebAppConfiguration
@EnableWebMvc
public class ClockinApplicationTests {

    @Autowired
    private RecognizeFaceService recognizeFaceService;

    @Test
    public void test(){
    }
}

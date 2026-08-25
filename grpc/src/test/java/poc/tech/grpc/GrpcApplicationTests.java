package poc.tech.grpc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "grpc.server.port=0")
class GrpcApplicationTests {

	@Test
	void contextLoads() {
	}

}

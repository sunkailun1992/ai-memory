package com.kellen.memory.rpc;

import com.kellen.rpc.memory.MemoryRetrievalRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 提供方在 {@code @DubboService} 上声明的幂等方法：utils 2.1.1 起的 Dubbo 调用端统一重试只对声明了幂等的方法
 * 在请求发出后的连接类失败重试。声明经注册中心元数据传到调用端。
 */
class DubboIdempotentDeclarationTest {

    @Test
    void retrieveIsDeclaredIdempotent() {
        assertDeclaredIdempotent(MemoryRetrievalRpcServiceImpl.class, MemoryRetrievalRpcService.class, "retrieve");
    }

    /**
     * 其余三个服务含写操作，一个方法都不能声明幂等，只有第一层（请求未发出）保护：方法级与服务级都不能写 idempotent，
     * 服务级的 idempotent 等于全部方法都声明。它们可以有别的方法配置（如超时），护栏只看 idempotent。
     */
    @Test
    void writeServicesDeclareNoIdempotentMethod() {
        for (Class<?> implementation : List.of(MemoryShortMemoryRpcServiceImpl.class, MemoryGovernanceRpcServiceImpl.class,
                MemoryCandidateRpcServiceImpl.class)) {
            DubboService service = implementation.getAnnotation(DubboService.class);
            assertThat(service).as("%s 应标 @DubboService", implementation.getSimpleName()).isNotNull();
            assertThat(parameters(service.parameters())).as("%s 服务级不声明 idempotent", implementation.getSimpleName())
                    .doesNotContainKey("idempotent");
            for (Method method : service.methods()) {
                assertThat(parameters(method.parameters()))
                        .as("%s#%s 不声明 idempotent", implementation.getSimpleName(), method.name())
                        .doesNotContainKey("idempotent");
            }
        }
    }

    private static void assertDeclaredIdempotent(Class<?> implementation, Class<?> contract, String method) {
        assertThat(contract).isAssignableFrom(implementation);
        DubboService service = implementation.getAnnotation(DubboService.class);
        assertThat(service).as("%s 应标 @DubboService", implementation.getSimpleName()).isNotNull();
        assertThat(parameters(service.parameters()))
                .as("%s 不能在服务级写 idempotent：那等于全部方法（包括以后新增的写方法）都声明幂等", implementation.getSimpleName())
                .doesNotContainKey("idempotent");
        List<String> contractMethods = Arrays.stream(contract.getMethods()).map(java.lang.reflect.Method::getName).toList();
        assertThat(service.methods()).extracting(Method::name).allSatisfy(name -> assertThat(contractMethods).contains(name));
        List<Method> declared = Arrays.stream(service.methods()).filter(entry -> entry.name().equals(method)).toList();
        assertThat(declared).as("%s 的 %s 应恰好声明一次", implementation.getSimpleName(), method).hasSize(1);
        assertThat(parameters(declared.get(0).parameters())).containsEntry("idempotent", "true");
    }

    private static Map<String, String> parameters(String[] pairs) {
        Map<String, String> parameters = new HashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            parameters.put(pairs[index], pairs[index + 1]);
        }
        return parameters;
    }
}

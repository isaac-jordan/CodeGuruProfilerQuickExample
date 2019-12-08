import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Response;

@RequiredArgsConstructor
public class RateLimiterInterceptor implements Interceptor {
    private final RateLimiter rateLimiter;

    @Override
    public Response intercept(Chain chain) throws IOException {
        rateLimiter.acquire();
        return chain.proceed(chain.request());
    }
}

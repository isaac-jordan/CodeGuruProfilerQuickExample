import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

@RequiredArgsConstructor
public class GitHub {

    private static final String API_BASE_URL = "https://api.github.com/";
    private static final double requestsPerSecond = 5000 / (60 * 60);

    @Delegate
    private final GitHubService service;

    public static GitHub create(String accessToken) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new RateLimiterInterceptor(RateLimiter.create(requestsPerSecond)))
                .addInterceptor(new AccessTokenInterceptor(accessToken))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new GitHub(retrofit.create(GitHubService.class));
    }

    public List<GitHub.Issue> listRepoIssuesForUser(String user) {
        List<GitHub.Repo> repos = getResponse(service.listRepos(user));

        return repos.stream().map(repo -> getResponse(service.listIssuesForRepo(user, repo.getName())))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public <T> T getResponse(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.errorBody() != null) {
                System.err.println(String.format("Error: %s, Request: %s", response.errorBody().string(), call.request().toString()));
                return null;
            }

            return response.body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Data
    class Repo {
        String name;
        String description;
        URL url;
    }

    @Data
    class Issue {
        String title;
        String state;
    }
}

interface GitHubService {
    @GET("users/{user}/repos")
    Call<List<GitHub.Repo>> listRepos(@Path("user") String user);

    @GET("/repos/{user}/{repo}/issues")
    Call<List<GitHub.Issue>> listIssuesForRepo(@Path("user") String user, @Path("repo") String repo);
}

import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.codeguruprofilerjavaagent.Profiler;

public class Main {
    public static void main(String[] args) {

        String githubAccessToken = args[0];
        String awsAccessKeyId = args[1];
        String awsSecretAccessKey = args[2];

        AwsBasicCredentials basicCredentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);
        Profiler codeGuruProfiler = new Profiler.Builder()
                .profilingGroupName("Isaac-GitHubIssueLister-Dev")
                .awsCredentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                .awsRegionToReportTo(Region.EU_WEST_1)
                .build();
        codeGuruProfiler.start();

        GitHub github = GitHub.create(githubAccessToken);

        // Simulate an always-on application
        while (true) {
            printIssuesForUser(github, "octocat");
            printIssuesForUser(github, "google");
            sleepForASecond();
        }
    }

    protected static void printIssuesForUser(GitHub github, String user) {
        System.out.println(String.format("Printing GitHub issues for repos owned by user: %s", user));
        github.listRepoIssuesForUser(user).forEach(issue -> {
            System.out.println(issue.toString());
        });
    }

    @SneakyThrows
    protected static void sleepForASecond() {
        Thread.sleep(1000);
    }

}

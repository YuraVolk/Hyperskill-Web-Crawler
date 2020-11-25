package crawler.logic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class SearchThread implements Runnable {
    private final String url;
    private final ExecutorService threadExecutor;
    private final Map<String, Site> urls;

    public SearchThread(String url, ExecutorService threadExecutor, Map<String, Site> returnUrl) {
        this.url = url;
        this.threadExecutor = threadExecutor;
        this.urls = returnUrl;
    }

    private String getHTML() {
        try (Scanner scanner = new Scanner(new URL(url).openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (MalformedURLException | UnknownHostException ignore) {
            return "ERROR";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void run() {
        synchronized(urls) {
            Site resultSite = new Site(url);
            resultSite.setContent(getHTML());

            urls.put(url, resultSite);
            threadExecutor.shutdown();
        }
    }
}

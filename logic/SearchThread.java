package crawler.logic;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchThread extends Thread implements Runnable {
    private final String url;
    private final ExecutorService threadExecutor;
    private final Map<String, Site> urls;
    private static final List<String> visitedLinks = Collections.synchronizedList(new ArrayList<>());
    private volatile int depth;

    public SearchThread(String url, ExecutorService threadExecutor, Map<String, Site> returnUrl, int depth) {
        this.url = url;
        this.threadExecutor = threadExecutor;
        this.urls = returnUrl;
        this.depth = depth;
    }

    private String getHTML(String url) {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            if (urlConnection.getContentType() != null && urlConnection.getContentType()
                    .contains("text/html")) {
                InputStream inputStream = new URL(url).openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                final StringBuilder stringBuilder = new StringBuilder();
                String nextLine;
                while ((nextLine = reader.readLine()) != null) {
                    stringBuilder.append(nextLine);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
                return stringBuilder.toString();
            }
        } catch (MalformedURLException | UnknownHostException
                | FileNotFoundException | SocketException | SocketTimeoutException ignore) {
            ignore.printStackTrace();
            return "ERROR";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private List<String> getAllLinks(String html) {
        Pattern pattern = Pattern.compile("<a((.+?\\s+)|(\\s))href=(.+?)>(.+?)</a>");
        Matcher matcher = pattern.matcher(html);
        List<String> links = new ArrayList<>();

        String tempResult;
        while (matcher.find()) {
            tempResult = matcher.group(4).split("\\s")[0];
            if (tempResult.startsWith("\"") | tempResult.startsWith("'")) {
                tempResult = tempResult.substring(1);
            }
            if (tempResult.endsWith("\"") | tempResult.endsWith("'")) {
                tempResult = tempResult.substring(0, tempResult.length() - 1);
            }

            if (tempResult.startsWith("//")) {
                final String prefix = url.split("//")[0];
                tempResult = prefix + tempResult;
            } else if (!tempResult.contains("//")) {
                final String protocol = url.split("//")[0];
                String prefix = url.split("/")[2];
                if (!prefix.endsWith("/")) {
                    prefix = prefix + "/";
                }
                tempResult = protocol + "//" + prefix + tempResult;
            }

          //  if (!visitedLinks.contains(tempResult)) {
                visitedLinks.add(tempResult);
                links.add(tempResult);
          //  }
        }

        return links;
    }

    @Override
    public void run() {
        if (depth == 2) {
            threadExecutor.shutdown();
            return;
        }

        Site resultSite = new Site(url);
        final String html = getHTML(url);
        resultSite.setContent(html);

        List<String> links = getAllLinks(html);
        System.out.println(url);
        if (!resultSite.getContent().equals("")) {
            urls.put(url, resultSite);
        } else {
            threadExecutor.shutdown();
            return;
        }

        System.out.println(links.size());

        if (links.size() == 0) {
            threadExecutor.shutdown();
            return;
        }

        for (String link : links) {
            threadExecutor.submit(new SearchThread(link, threadExecutor, urls, depth + 1));
        }
    }
}

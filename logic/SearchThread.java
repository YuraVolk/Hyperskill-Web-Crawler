package crawler.logic;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchThread extends Thread implements Runnable {
    private static final List<String> visitedLinks = Collections.synchronizedList(new ArrayList<>());
    public static volatile boolean run = true;

    private String getHTML(String url) {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
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
                final String prefix = MultithreadedCrawler.getBaseUrl().split("//")[0];
                tempResult = prefix + tempResult;
            } else if (!tempResult.contains("//")) {
                final String protocol = MultithreadedCrawler.getBaseUrl().split("//")[0];
                String prefix = MultithreadedCrawler.getBaseUrl().split("/")[2];
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
        while (MultithreadedCrawler.isProcessRunning()) {
            Task task = MultithreadedCrawler.getTask();
            if (MultithreadedCrawler.urlVisited(task.getUrl())) {
                continue;
            }

            MultithreadedCrawler.visitUrl(task.getUrl());

            String html = getHTML(task.getUrl());

            if (html.equals("ERROR")) {
                continue;
            }

            Site site = new Site(task.getUrl());
            try {
                site.setContent(html);
            } catch (IllegalStateException ignore) {
                continue;
            }


            if (task.getDepth() + 1 < task.getMaxDepth()) {
                for (String link : getAllLinks(html)) {
                    MultithreadedCrawler.offerTask(link, task.getDepth() + 1);
                }
            }

        }


        /*
        if (depth >= 2 or !run) {

        }

        if (depth >= 2 || !run) {
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
            return;
        }

        if (links.size() == 0) {
            return;
        }

        for (String link : links) {
            if (!run) {
                threadExecutor.shutdown();
                return;
            }
            threadExecutor.submit(new SearchThread(link, threadExecutor, urls, depth + 1));
        }*/
    }
}

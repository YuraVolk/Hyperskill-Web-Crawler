package crawler.logic;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MultithreadedCrawler {
    private static final Queue<Task> urlQueue = new LinkedBlockingQueue<>();
    private static volatile boolean crawling = false;
    private static final List<String> visitedUrls = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, String> urls = new ConcurrentHashMap<>();
    protected static volatile int parsedPages = 0;
    private static String url;
    private static final ThreadGroup workerGroup = new ThreadGroup("crawlers");
    private static final Map<String, String> visitedSites = new ConcurrentHashMap<>();

    private static JLabel elapsedTimeInfo;
    private static JLabel parsedPagesInfo;
    private static JToggleButton button;

    protected static int maxDepth = 2;
    private static int timeLimit = -1;
    private static int workers = 5;

    private static String originalURL; // Primarily to not save the original site to file, as it does not count

    protected static String getBaseUrl() {
        return url;
    }

    protected static void visitUrl(String url) {
        if (!visitedUrls.contains(url)) {
            visitedUrls.add(url);
        }
    }

    protected static void addParsedPage(Site site) {
        System.out.println(site.getUrl());
        if (crawling) {
            parsedPages++;

            // Original page does not count
            parsedPagesInfo.setText(Integer.toString(parsedPages - 1));
            if (!site.getUrl().equals(originalURL)) {
                visitedSites.put(site.getUrl(), site.getTitle());
            }
        }
    }

    protected static boolean isProcessRunning() {
        return !urlQueue.isEmpty() && crawling;
    }

    protected static Task getTask() {
        return urlQueue.poll();
    }

    protected static boolean urlVisited(String url) {
        return visitedUrls.contains(url);
    }

    protected static void offerTask(String url, int depth) {
        if (crawling) {
            urlQueue.offer(new Task(depth, url));
        }
    }

    public static void setupScreenInfo(JLabel elapsedTime, JLabel parsedPages, JToggleButton startButton) {
        elapsedTimeInfo = elapsedTime;
        parsedPagesInfo = parsedPages;
        button = startButton;
    }

    public static void setupRunValues(int workers, int maxDepth, int timeLimit) {
        MultithreadedCrawler.workers = workers;
        MultithreadedCrawler.timeLimit = timeLimit;
        MultithreadedCrawler.maxDepth = maxDepth;
    }

    public static Map<String, String> getVisitedSites() {
        return visitedSites;
    }

    public static void crawl(String url) {
        MultithreadedCrawler.url = url;
        originalURL = url;
        visitedSites.clear();

        if (!crawling) {
            crawling = true;
            urlQueue.clear();
            offerTask(url, 0);
            visitedUrls.clear();

            int workersNum = workers;
            new SearchThread().start();
            Thread[] workers = new Thread[workersNum];
            for (int i = 0; i < workersNum; i++) {
                if (workers[i] == null || workers[i].getState() == Thread.State.TERMINATED) {
                    workers[i] = new Thread(workerGroup, new SearchThread());
                    workers[i].start();
                }
            }

            int prevParsed = 0;
            int prevParsedStreak = 0;
            int seconds = 0;
            do {
                if (!crawling) {
                    break;
                }

                try {
                    Thread.sleep(1000);
                    seconds++;
                } catch (InterruptedException ignore) { }

                if (parsedPages == prevParsed) {
                    prevParsedStreak++;
                } else {
                    prevParsed = parsedPages;
                    prevParsedStreak = 0;
                }

                elapsedTimeInfo.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                if (timeLimit == seconds) {
                    break;
                }

                for (int i = 0; i < workersNum; i++) {
                    if (workers[i] == null || workers[i].getState() == Thread.State.TERMINATED) {
                        workers[i] = new Thread(workerGroup, new SearchThread());
                        workers[i].start();
                    }
                }
            } while (prevParsedStreak <= 2 && crawling); // For real crawling, change this hysteresis to 3
        }

        button.setSelected(false);
        urlQueue.clear();
        parsedPages = 0;
        crawling = false;
    }
}

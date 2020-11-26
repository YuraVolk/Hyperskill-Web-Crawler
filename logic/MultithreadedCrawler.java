package crawler.logic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MultithreadedCrawler {
    private static final Queue<Task> urlQueue = new LinkedBlockingQueue<>();
    private static volatile boolean crawling = false;
    private static final List<String> visitedUrls = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, String> urls = new ConcurrentHashMap<>();
    private static int maxDepth = 4;
    private static String url;
    private Thread[] workers;
    private ThreadGroup workerGroup = new ThreadGroup("crawlers");

    protected static String getBaseUrl() {
        return url;
    }

    protected static void visitUrl(String url) {
        visitedUrls.add(url);
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
        System.out.println(url);
        urlQueue.offer(new Task(depth, maxDepth, url));
    }

    public void crawl(String url) {
        MultithreadedCrawler.url = url;
        if (!crawling) {
            crawling = true;
            urlQueue.clear();
            offerTask(url, 1);
            visitedUrls.clear();

            int workersNum = 5;
            new SearchThread().start();
            workers = new Thread[workersNum];
            for (int i = 0; i < workersNum; i++) {
                if (workers[i] == null || workers[i].getState() == Thread.State.TERMINATED) {
                    workers[i] = new Thread(workerGroup, new SearchThread());
                    workers[i].start();
                }
            }

            int h = 0;
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) { }

                if (urlQueue.size() > 0) {
                    if (h > 0) {
                        h--;
                    }
                } else {
                    h++;
                }

                for (int i = 0; i < workersNum; i++) {
                    if (workers[i] == null || workers[i].getState() == Thread.State.TERMINATED) {
                        workers[i] = new Thread(workerGroup, new SearchThread());
                        workers[i].start();
                    }
                }
            } while (h <= 3);
        }

        crawling = false;
        System.out.println(visitedUrls);
    }
}

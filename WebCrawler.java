package crawler;

import crawler.logic.MultithreadedCrawler;
import crawler.logic.Site;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.function.Predicate;

public class WebCrawler extends JFrame {
    private JTextField urlInputField;
    private JTextField workersInputField;
    private JTextField depthInputField;
    private JTextField timeLimitInputField;
    private JTextField exportFileName;
    private JCheckBox depthCheckbox;
    private JCheckBox timeLimitCheckbox;

    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 280);
        setTitle("Web Crawler");
        setBackground(Color.decode("#e2e2e2"));

        setLocationRelativeTo(null);
        initUserInterface();
        setLayout(null);
        setResizable(false);
        setVisible(true);
    }

    private void onButtonClick() {
        Predicate<String> isNumber = x -> x.chars().allMatch(Character::isDigit);

        int depth = 100;
        if (depthCheckbox.isSelected() && isNumber.test(depthInputField.getText())) {
            depth = Integer.parseInt(depthInputField.getText());
        }

        int timeLimit = -1;
        if (timeLimitCheckbox.isSelected() && isNumber.test(timeLimitInputField.getText())) {
            timeLimit = Integer.parseInt(timeLimitInputField.getText());
        }

        int workers = 5;
        if (isNumber.test(workersInputField.getText())) {
            workers = Integer.parseInt(workersInputField.getText());
        }

        MultithreadedCrawler.setupRunValues(workers, depth, timeLimit);
        MultithreadedCrawler.crawl(urlInputField.getText());
    }

    private void saveToFile() {
        System.out.println(MultithreadedCrawler.getVisitedSites());
        try (PrintWriter printWriter = new PrintWriter(exportFileName.getText())) {
            printWriter.print("");
            for (Map.Entry<String, String> entry : MultithreadedCrawler.getVisitedSites().entrySet()) {
                printWriter.println(entry.getKey());
                printWriter.println(entry.getValue());
            }
        } catch (FileNotFoundException ignore) { }
    }

    private void initUserInterface() {
        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setBounds(30,10,425,25);
        buttonsContainer.setLayout(new BorderLayout());
        add(buttonsContainer);

        JLabel urlInputLabel = new JLabel("Start URL: ");
        buttonsContainer.add(urlInputLabel, BorderLayout.WEST);
        urlInputField = new JTextField();
        urlInputField.setName("UrlTextField");
        buttonsContainer.add(urlInputField, BorderLayout.CENTER);

        JToggleButton runButton = new JToggleButton("Run");
        runButton.setName("RunButton");
        buttonsContainer.add(runButton, BorderLayout.EAST);
        runButton.addActionListener(e -> onButtonClick());


        JPanel workersContainer = new JPanel();
        workersContainer.setBounds(30,45,425,25);
        workersContainer.setLayout(new BorderLayout());
        add(workersContainer);

        JLabel workersInputLabel = new JLabel("Workers: ");
        workersContainer.add(workersInputLabel, BorderLayout.WEST);
        workersInputField = new JTextField("5");
        workersContainer.add(workersInputField, BorderLayout.CENTER);


        JPanel depthContainer = new JPanel();
        depthContainer.setBounds(30,80,425,25);
        depthContainer.setLayout(new BorderLayout());
        add(depthContainer);

        JLabel depthLabel = new JLabel("Maximum depth: ");
        depthContainer.add(depthLabel, BorderLayout.WEST);
        depthInputField = new JTextField("50");
        depthInputField.setName("DepthTextField");
        depthContainer.add(depthInputField, BorderLayout.CENTER);

        depthCheckbox = new JCheckBox("Enabled");
        depthCheckbox.setName("DepthCheckBox");
        depthContainer.add(depthCheckbox, BorderLayout.EAST);


        JPanel timeLimitContainer = new JPanel();
        timeLimitContainer.setBounds(30,115,425,25);
        timeLimitContainer.setLayout(new BorderLayout());
        add(timeLimitContainer);

        JLabel timeLimitLabel = new JLabel("Time limit: ");
        timeLimitContainer.add(timeLimitLabel, BorderLayout.WEST);

        JPanel timeLimitInputPanel = new JPanel(new BorderLayout());
        timeLimitInputField = new JTextField("120");
        timeLimitInputPanel.add(timeLimitInputField, BorderLayout.CENTER);
        JLabel timeLimitMeasure = new JLabel("seconds");
        timeLimitInputPanel.add(timeLimitMeasure, BorderLayout.EAST);
        timeLimitContainer.add(timeLimitInputPanel);

        timeLimitCheckbox = new JCheckBox("Enabled");
        timeLimitContainer.add(timeLimitCheckbox, BorderLayout.EAST);


        JPanel elapsedTimeContainer = new JPanel();
        elapsedTimeContainer.setBounds(30,150,425,25);
        elapsedTimeContainer.setLayout(new BorderLayout());
        add(elapsedTimeContainer);

        JLabel elapsedTimeLabel = new JLabel("Elapsed time: ");
        elapsedTimeContainer.add(elapsedTimeLabel, BorderLayout.WEST);
        JLabel elapsedTime = new JLabel("0:00");
        elapsedTimeContainer.add(elapsedTime, BorderLayout.CENTER);


        JPanel parsedPagesContainer = new JPanel();
        parsedPagesContainer.setBounds(30,175,425,25);
        parsedPagesContainer.setLayout(new BorderLayout());
        add(parsedPagesContainer);

        JLabel parsedPagesLabel = new JLabel("Parsed pages: ");
        parsedPagesContainer.add(parsedPagesLabel, BorderLayout.WEST);
        JLabel parsedPages = new JLabel("0");
        parsedPages.setName("ParsedLabel");
        parsedPagesContainer.add(parsedPages, BorderLayout.CENTER);


        JPanel exportContainer = new JPanel();
        exportContainer.setBounds(30,205,425,25);
        exportContainer.setLayout(new BorderLayout());
        add(exportContainer);

        JLabel exportLabel = new JLabel("Export: ");
        exportContainer.add(exportLabel, BorderLayout.WEST);
        exportFileName = new JTextField();
        exportFileName.setName("ExportUrlTextField");
        exportContainer.add(exportFileName, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save");
        saveButton.setName("ExportButton");
        exportContainer.add(saveButton, BorderLayout.EAST);
        saveButton.addActionListener(e -> saveToFile());

        MultithreadedCrawler.setupScreenInfo(elapsedTime, parsedPages, runButton);
    }
}
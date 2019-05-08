package ua.edu.sumdu.in71;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Logger LOGGER;
    private static double discretness = 1;
    private static final String positiveDoublePattern = "([0-9]*[.])?[0-9]+";
    static  {
        try {
            System.setProperty("logfile", new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath() + File.separatorChar + "log.log");
            PropertyConfigurator.configure(Main.class.getResourceAsStream("/log4j.properties"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        LOGGER = Logger.getLogger(Main.class);
    }

    /**
     *  The matrix we read must have must contain the information about how much profit we will get if invest nothing,
     *  i.e. the first number of the row is considered as the gain from 0.
     *  Also it must have the same number of elements in each row.
     * */
    public static void main(String[] args) {
        String inputFilepath = String.join(" ", args);
        File input = new File(inputFilepath);
        if (!validateFileContent(input)) {
            if (LOGGER.isEnabledFor(Level.INFO)) {
                LOGGER.info("File " + input.getAbsolutePath() + " has invalid content");
            }
            return;
        }
        double [][] matrix;
        try {
            matrix = input(inputFilepath);
            discretness = parseDiscretness(input);
        } catch (FileNotFoundException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("Unable to find the file specified while input", e);
            }
            return;
        } catch (IOException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error(e);
            }
            return;
        }
        if (!validate(matrix)) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("The input matrix is not valid");
            }
            printMatrix(matrix);
            return;
        }
        Node node = new Node(matrix[0].length - 1);
        node.setDiscretness(discretness);
        DynamicProgrammingSolution.buildMostProfitDistributionPlan(node, 0, new DiscreteDistribution() {
            private double [][] m = matrix;
            @Override
            public double getProfit(int whom, int portionOfInvestments) {
                try {
                    return m[whom][portionOfInvestments];
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException();
                }
            }
            @Override
            public double getAmOfUnits() {
                return m.length;
            }
        });
        printResult(node);
        String outputName = input.getName() + "[result]";
        File output = new File(input.getParentFile().getAbsolutePath() + File.separatorChar + outputName);
        try {
            if (output.createNewFile()) {
                if (LOGGER.isEnabledFor(Level.TRACE)) {
                    LOGGER.trace("The file " + output.getAbsolutePath() + " has been created.");
                }
            }
            printResult(node, output);
        } catch (IOException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("An error occurred. See log file for more information.", e);
            }
        }
    }

    /**
     *  Prints the result to the file representing as step by step advises
     *
     * @param           node the parent node, i.e. the one that has 0 {@code profit} and full {@code money} value
     * @param           file the file for output
     * */
    private static void printResult(Node node, File file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            String result = resultToString(node);
            fileWriter.append(result);
        }
    }

    /**
     *  Represents the result solution printing it to the console
     * */
    private static void printResult(Node node) {
        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info(resultToString(node));
        }
    }

    /**
     *  Represents the obtained result as a {@code String}
     * */
    private static String resultToString(Node node) {
        StringBuilder stringBuilder = new StringBuilder(System.lineSeparator());
        int unit = 0;
        double totalProfit = 0;
        while (node != null) {
            stringBuilder.append("Invest ")
                    .append(node.getInvestmentXdiscrentess())
                    .append(" to the ")
                    .append(unit++)
                    .append(" unit |=> +")
                    .append(node.getProfit())
                    .append(System.lineSeparator());
            totalProfit += node.getProfit();
            node = node.getNext();
        }
        stringBuilder.append("Total profit is ").append(totalProfit);
        return stringBuilder.toString();
    }

    /**
     *  Validates the input matrix
     *
     * @return          {@code true} if the matrix has rows of the same non-zero length, else otherwise
     * */
    private static boolean validate(double [][] matrix) {
        if (matrix.length == 0 || matrix[0].length == 0) {
            return false;
        }
        if (matrix.length == 1) {
            return true;
        }
        int length = matrix[0].length;
        for (double [] row : matrix) {
            if (row.length != length) {
                return false;
            }
        }
        return true;
    }

    /**
     *  Reads a matrix from the specified file
     *
     * @param           inputMatrixFile a file (usually .txt) having only a 2D-array of {@code doubles}
     *
     * @return          {@code double [][]} representation of the {@code inputMatrixFile}
     *                  file content (if it is a matrix)
     *
     *  NOTE! The method does not validate an input and will not throw an exception
     * if the matrix has rows of different lengths.
     * */
    private static double [][] input(String inputMatrixFile) throws IOException {
        File file = new File(inputMatrixFile);
        return readInputMatrix(file);
    }

    /**
     *  Prints passed matrix to the console
     * */
    private static void printMatrix(double [][] d) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                stringBuilder.append(d[i][j]).append(' ');
            }
            stringBuilder.append(System.lineSeparator());
        }
        if (LOGGER.isEnabledFor(Level.TRACE)) {
            LOGGER.trace(System.lineSeparator() + stringBuilder);
        }
    }

    /**
     *  Reads a matrix from the specified file
     *
     * @param           inputMatrixFile a file (usually .txt) having only a 2D-array of {@code doubles}
     *
     * @return          {@code double [][]} representation of the {@code inputMatrixFile}
     *                  file content (if it is a matrix)
     *
     *  NOTE! The method does not validate an input and will not throw an exception
     * if the matrix has rows of different lengths.
     * */
    private static double [][] readInputMatrix(File inputMatrixFile) throws IOException {
        Scanner scanner = new Scanner(inputMatrixFile);
        List<String> discreteDistributions = new LinkedList<>();
        while (scanner.hasNextLine()) {
            discreteDistributions.add(scanner.nextLine());
        }
        List<double[]> distributions = new LinkedList<>();
        for (int i = 0; i < discreteDistributions.size() - 1; i++) {
            String [] row = discreteDistributions.get(i).split(" ");
            double [] r = new double[row.length];
            for (int j = 0; j < row.length; j++) {
                r[j] = Double.valueOf(row[j]);
            }
            distributions.add(r);
        }
        double [][] result;
        try {
            result = new double[distributions.size()][distributions.get(0).length];
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Empty input");
        }
        for (int i = 0; i < distributions.size(); i++) {
            for (int j = 0; j < distributions.get(i).length; j++) {
                result[i][j] = distributions.get(i)[j];
            }
        }
        return result;
    }

    /**
     *  Returns the total profit
     * */
    private static double printSolution(Node node) {
        int step = 0;
        double totalProfit = 0;
        while (node != null) {
            System.out.println(step++
                    + " unit receives "
                    + node.getInvestmentXdiscrentess() +
                    ", income +" +
                    node.getProfit());
            totalProfit += node.getProfit();
            node = node.getNext();
        }
        return totalProfit;
    }

    /**
     *  Validates the file containing the input data
     *
     * @param           file the file containing input
     *
     * @return          {@code true} if and only if the file content has been successfully parsed
     *                  and in fits the requirements, {@code false} otherwise
     * */
    private static boolean validateFileContent(File file) {
        Pattern pattern = Pattern.compile("^("
                + positiveDoublePattern
                + "((\\r?\\n)| ))+discretness ("
                + positiveDoublePattern + "){1}?$");
        String defaultEncoding = null;
        try {
            String content = FileUtils.readFileToString(file, defaultEncoding);
            return pattern.matcher(content).matches();
        } catch (IOException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error(e);
            }
            return false;
        }
    }

    /**
     *  Parese discretness value
     * */
    private static double parseDiscretness(File file) throws IOException {
        String defaultEncoding = null;
        Pattern pattern = Pattern.compile("discretness (" + positiveDoublePattern + ")");
        Matcher matcher = pattern.matcher(FileUtils.readFileToString(file, defaultEncoding));
        if (!matcher.find()) {
            throw new IOException("Missed discretness value");
        }
        return Double.parseDouble(matcher.group(1));
    }
}

package cn.ios;

import cn.ios.casegen.config.Config;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.cmd.ProgressBar;
import org.apache.commons.cli.*;

/**
 * @author: wangmiaomiao
 * @create: 2022/10/25 15:48
 **/
public class Main {
    public static void main(String[] args) {
        GlobalCons.START_TIME = System.currentTimeMillis();

        Option op1 = new Option("jre", "jre", true, "local JRE path");
        op1.setRequired(false);

        Option op2 = new Option("input", "input", true, "compiled class path");
        op2.setRequired(false);

        Option op3 = new Option("output", "output", true, "output path folder");
        op3.setRequired(false);

        Option op4 = new Option("maxString", "maxString", true, "maxlengthOfString");
        op4.setRequired(false);

        Option op5 = new Option("minSet", "minSet", true, "minSizeOfSet");
        op5.setRequired(false);

        Option op6 = new Option("maxSet", "maxSet", true, "maxSizeOfSet");
        op6.setRequired(false);

        Option op7 = new Option("maxCase", "maxCase", true, "maxSizeOfCase");
        op7.setRequired(false);

        Option op8 = new Option("maxTime", "maxTime", true, "maxTimeOfClass");
        op8.setRequired(false);

        Option op9 = new Option("h", "help", false, "help");
        op9.setRequired(false);

        Options options = new Options();
        options.addOption(op1);
        options.addOption(op2);
        options.addOption(op3);
        options.addOption(op4);
        options.addOption(op5);
        options.addOption(op6);
        options.addOption(op7);
        options.addOption(op8);
        options.addOption(op9);

        CommandLine commandLine = null;
        CommandLineParser cliParser = new DefaultParser();

        try {
            commandLine = cliParser.parse(options, args);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
        }

        if (commandLine == null) {
            System.err.println("can not obtain commandline");
            System.exit(0);
        }

        if (commandLine.hasOption("help") || commandLine.hasOption("h")) {
            System.out.println("<options>      <description>");
            System.out.println("----------------------------");
            System.out.println("-h(--help)     help");
            System.out.println("--jre          local JRE path in JDK");
            System.out.println("--input        compiled class path under test");
            System.out.println("--output       output path folder");
            System.out.println("--maxString    maximum length of string");
            System.out.println("--minSet       minimum size of set/array");
            System.out.println("--maxSet       maximum size of set/array");
            System.out.println("--maxCase      maximum size of cases for each method");
            System.out.println("--maxTime      maximum generation time for each class");
        } else {
            if (!commandLine.hasOption("jre") || !commandLine.hasOption("output") || !commandLine.hasOption("input")) {
                System.err.println("The three parameters that must be set cannot be obtained");
                return;
            }

            try {
                String jrePath = commandLine.getOptionValue("jre");
                String inputPath = commandLine.getOptionValue("input");
                String outputPath = commandLine.getOptionValue("output");
                int maxString = commandLine.hasOption("maxString")? Integer.parseInt(commandLine.getOptionValue("maxString")) : -1;
                int minSet = commandLine.hasOption("minSet")? Integer.parseInt(commandLine.getOptionValue("minSet")) : -1;
                int maxSet = commandLine.hasOption("maxSet")? Integer.parseInt(commandLine.getOptionValue("maxSet")) : -1;
                int maxCase = commandLine.hasOption("maxCase")? Integer.parseInt(commandLine.getOptionValue("maxCase")) : -1;
                int maxTime = commandLine.hasOption("maxTime")? Integer.parseInt(commandLine.getOptionValue("maxTime")) : -1;

                Config.onceConfig(jrePath, outputPath, inputPath, maxString, minSet, maxSet, maxCase, maxTime);
                Thread threadOne = new Thread(new Runnable() {
                    public void run() {
                        ProgressBar.showProgress();
                    }
                });

                Thread threadTwo = new Thread(new Runnable() {
                    public void run() {
                        API.generateTestCaseAfterConfig();
                    }
                });

                threadOne.start();
                threadTwo.start();
            } catch (NumberFormatException numberFormatException) {
                System.err.println("NumberFormatException occurred while converting parameters");
            }
        }
    }
}
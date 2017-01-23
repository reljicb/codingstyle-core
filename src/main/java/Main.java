import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import org.apache.commons.exec.*;
import org.reljicb.usercheckstyle.beans.CSError;
import org.reljicb.usercheckstyle.beans.CheckStyle;
import org.reljicb.usercheckstyle.beans.TargetFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        Main app = new Main();
        List<TargetFile> ret = app.run();
        ret.forEach(targetFile ->
              {
                  System.out.println(targetFile.getName());
                  System.out.println("------------------------");
                  targetFile.getErrors().stream()
                        .map(CSError::toString)
                        .forEach(System.out::println);
              }
        );

    }

    private static String reformatPath(final String path)
    {
        if (File.separator.equals("\\"))
        {
            return path.replace("/C:/", "c:\\").replace("/", "\\");
        }
        return path;
    }

    public List<TargetFile> run() throws IOException, InterruptedException
    {
        final String CHECK_STYLE_PATH = reformatPath(this.getClass().getClassLoader().getResource("./lib").getPath());
        final String RULES_XML_PATH = reformatPath(this.getClass().getClassLoader().getResource("./rules").getPath());

        final String ACC_DETAILS_PATH = "C:\\Users\\ER266\\development\\workspace\\rbcone-ao-accdetail\\rbcone-ao-account-details-web\\src\\main\\java";
        final String CODINGSTYLE_GIT_PATH = "C:\\TEMP\\codingstyle-git\\src";
        final String FULL_STACK_VALIDATION_PATH = "/Users/bojanreljic/development/workspace/full-stack-validation/src/main/java/";
        final String JAVA_FILE_PATH = FULL_STACK_VALIDATION_PATH;

        //        final String OUTPUT_FILE_PATH = "/Users/bojanreljic/tmp/out.txt";

        final String javaHome = System.getProperty("java.home");

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout, stderr);

        Executor executor = new DaemonExecutor();
        executor.setStreamHandler(psh);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(15000);
        executor.setWatchdog(watchdog);
        //        executor.setExitValues(new int[] { 0, 255 });

        int exitValue = -1;
        try
        {
            exitValue = executor.execute(new CommandLine(javaHome + File.separator
                  + "bin" + File.separator + "java")
                  .addArgument("-jar")
                  .addArgument(String.format("%s/checkstyle-7.4-all.jar", CHECK_STYLE_PATH))
                  .addArgument("-c")
                  .addArgument(String.format("%s/google_checks.xml", RULES_XML_PATH))
                  .addArgument("-f").addArgument("xml")
                  .addArgument(JAVA_FILE_PATH)
            );
        }
        finally
        {
            System.out.println(String.format("exit code: %d", exitValue));
            System.out.println(String.format("out: %s", stdout.toString()));
            System.err.println(String.format("err: %s", stderr.toString()));
        }

        if (exitValue == 0)
        {
            JacksonXmlModule module = new JacksonXmlModule();
            module.setDefaultUseWrapper(false);

            XmlMapper xmlMapper = new XmlMapper(module);

            CheckStyle checkStyle = xmlMapper.readValue(stdout.toString(), CheckStyle.class);

            List<TargetFile> ret = checkStyle.getTargetFiles().stream()
                  .filter(file -> file.getName().endsWith(".java"))
                  .collect(Collectors.toList());

            return ret;
        }

        return Lists.newArrayList();
    }
}

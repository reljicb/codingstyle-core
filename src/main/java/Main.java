import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import org.apache.commons.exec.*;
import org.reljicb.usercheckstyle.beans.CheckStyle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Main
{

    public static void main(String[] args) throws IOException, InterruptedException
    {
        new Main().run(args);
    }

    public void run(String[] args) throws IOException, InterruptedException
    {
        final URL CHECK_STYLE_PATH = this.getClass().getClassLoader().getResource("./lib");
        final URL RULES_XML_PATH = this.getClass().getClassLoader().getResource("./rules");

        final String JAVA_FILE_PATH = "/Users/bojanreljic/development/workspace/full-stack-validation/src/main/java/com/reljicb/FullStackValidationApplication.java";
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
                  .addArgument(String.format("%s/checkstyle-7.4-all.jar", CHECK_STYLE_PATH.getPath()))
                  .addArgument("-c")
                  .addArgument(String.format("%s/google_checks.xml", RULES_XML_PATH.getPath()))
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
            XmlMapper mapper = new XmlMapper();
            CheckStyle checkStyle = mapper.readValue(stdout.toString(), CheckStyle.class);

            final List<CheckStyle.TargetFile.CSError> errors = checkStyle.getTargetFile().getErrors();

            Lists.newArrayList(errors).stream()
                  .map(CheckStyle.TargetFile.CSError::toString)
                  .forEach(System.out::println);

        }
    }
}
